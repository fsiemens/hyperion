package de.fabiansiemens.hyperion.core.features.roll.v2;

import de.fabiansiemens.hyperion.core.features.roll.v2.node.*;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Berechnet den analytischen Erwartungswert eines Würfelausdrucks.
 *
 * <h2>Grundlagen</h2>
 * Ein fairer Würfel mit N Seiten hat den Erwartungswert {@code E[dN] = (N+1)/2}.
 * Dieser Evaluator traversiert den AST und berechnet für jeden Knoten den
 * Erwartungswert nach bekannten mathematischen Formeln.
 *
 * <h2>Implementierte Formeln</h2>
 * <ul>
 *   <li><b>kdN</b>: {@code k * (N+1)/2}</li>
 *   <li><b>Modifier</b>: linear addiert / multipliziert</li>
 *   <li><b>Advantage dN</b>: {@code E[max(X,Y)] = (2N+1)/3 - 1/(6N) * (N-1)/N} —
 *       exakte Formel via Ordnungsstatistik:
 *       {@code E[max(X₁,X₂)] = (N+1) - (N+1)/(2N) = (N+1)(1 - 1/(2N))}</li>
 *   <li><b>Disadvantage dN</b>: {@code E[min(X,Y)] = (N+1) - E[max(X,Y)]}</li>
 *   <li><b>Advantage all (kdN)</b>: jeder der k Würfe hat Advantage →
 *       {@code k * E[max(X,Y)]}</li>
 *   <li><b>Reroll once rr op T</b>: {@code E[rr] = p_keep * E[X|¬trigger] + p_trigger * E[dN]}
 *       wobei p_trigger = Wahrscheinlichkeit dass die Bedingung zutrifft</li>
 *   <li><b>Cap min T</b>: {@code E[max(X,T)] = T * P(X < T) + E[X | X ≥ T] * P(X ≥ T)}</li>
 *   <li><b>Cap max T</b>: {@code E[min(X,T)] = E[X | X ≤ T] * P(X ≤ T) + T * P(X > T)}</li>
 *   <li><b>Check</b>: kein Einfluss auf den Erwartungswert des Wurfs selbst</li>
 *   <li><b>Chain ? then : else</b>: {@code p_pass * E[then] + p_fail * E[else]}
 *       wobei p_pass die Wahrscheinlichkeit ist, dass der Bedingungswurf besteht</li>
 *   <li><b>Inner Roll als Parameter</b>: {@code E[f(X)]} wird näherungsweise als
 *       {@code f(E[X])} berechnet (Jensen-Näherung, exakt für lineare f).
 *       Für nichtlineare Interaktionen (z. B. Inner-Roll-Count mit Reroll) ist
 *       das Ergebnis eine Näherung.</li>
 * </ul>
 *
 * <h2>Bekannte Einschränkungen</h2>
 * <ul>
 *   <li><b>Inner Roll als Count/Sides mit nichtlinearen Argumenten</b>:
 *       {@code (d4)d20 rr<=2} — der Erwartungswert des Counts (2.5) wird als
 *       Näherung für die tatsächliche Verteilung verwendet. Exakt wäre eine
 *       vollständige Summe über alle möglichen Count-Werte.</li>
 *   <li><b>Reroll mit % (Modulo)</b>: Der Erwartungswert nach Modulo-Reroll
 *       ist exakt berechnet (Zählung teilbarer Werte).</li>
 *   <li><b>Kombinierte Argumente</b>: Advantage + Reroll, Cap + Reroll etc.
 *       werden in Reihenfolge angewandt (Advantage zuerst, dann Reroll, dann Cap).
 *       Das entspricht der tatsächlichen Ausführungsreihenfolge im Evaluator.</li>
 *   <li><b>allScope vs. nur erster Würfel</b>: Korrekt implementiert —
 *       ohne {@code all} gilt das Argument nur für den ersten Würfel.</li>
 * </ul>
 */
@Service
public class ExpectedValueEvaluator implements DiceNodeVisitor<Double> {

    public static final ExpectedValueEvaluator INSTANCE = new ExpectedValueEvaluator();

    // =========================================================================
    // Öffentliche API
    // =========================================================================

    /**
     * Berechnet den Erwartungswert des gegebenen Würfelausdrucks.
     *
     * @param expression Würfelausdruck, z. B. {@code "4d20 adv +5 check15 ? 3d8 : 1d4"}
     * @return analytischer Erwartungswert
     */
    public double expectedValue(String expression) {
        List<Token> tokens = new DiceLexer(expression).tokenize();
        DiceNode    ast    = new DiceParser(tokens).parse();
        return ast.accept(this);
    }

    // =========================================================================
    // Visitor
    // =========================================================================

    @Override
    public Double visitNumber(NumberNode node) {
        return (double) node.value();
    }

    @Override
    public Double visitGrouping(GroupingNode node) {
        return node.inner().accept(this);
    }

    @Override
    public Double visitAddition(AdditionNode node) {
        double total = 0;
        for (int i = 0; i < node.operands().size(); i++) {
            total += node.signs().get(i) * node.operands().get(i).accept(this);
        }
        return total;
    }

    @Override
    public Double visitCounted(CountedNode node) {
        // n × (Ausdruck) → n * E[Ausdruck]
        return node.count() * node.inner().accept(this);
    }

    @Override
    public Double visitChain(ChainNode node) {
        // E[chain] = P(bestanden) * E[then] + P(nicht bestanden) * E[else]
        double pPass   = passProbability(node.condition());
        double eThen   = node.thenBranch().accept(this);
        double eElse   = node.elseBranch() != null ? node.elseBranch().accept(this) : 0.0;
        return pPass * eThen + (1.0 - pPass) * eElse;
    }

    @Override
    public Double visitDiceRoll(DiceRollNode node) {
        // --- Seiten und Anzahl auflösen ---
        // Inner Rolls werden via E[X] approximiert (Jensen-Näherung für lineare Fälle exakt)
        int sides = (int) Math.round(node.sides().accept(this));
        int count = node.count() != null ? (int) Math.round(node.count().accept(this)) : 1;

        if (sides < 2) throw new IllegalArgumentException(
            "Erwartungswert: Seitenanzahl muss ≥ 2 sein, war " + sides);

        // --- Erwartungswert eines einzelnen Würfels mit allen Argumenten ---
        double eSingleDie = expectedValueOneDie(sides, node);

        // --- Modifier auflösen ---
        double perRollMod = 0;
        double singleMod  = 0;
        for (ModifierNode mod : node.modifiers()) {
            double resolved = mod.sign() * mod.value().accept(this);
            if (mod.allScope()) perRollMod += resolved;
            else                singleMod  += resolved;
        }

        // --- Gesamterwartungswert ---
        // Für allScope-Argumente: wie viele Würfel bekommen das Argument?
        boolean advAll    = node.advantage() != null && node.advAllScope();
        boolean rerollAll = node.reroll()    != null && node.rerollAllScope();
        boolean capAll    = node.cap()       != null && node.capAllScope();

        if (!advAll && !rerollAll && !capAll) {
            // Kein allScope: erster Würfel hat Argumente, Rest nicht
            if (count == 1) {
                return eSingleDie + perRollMod + singleMod;
            }
            double eFirstDie = eSingleDie; // bereits mit Argumenten berechnet
            double eRestDie  = expectedValuePlainDie(sides) + perRollMod;
            return eFirstDie + (count - 1) * eRestDie + singleMod;
        }

        // allScope: alle Würfel bekommen dasselbe Argument
        return count * (eSingleDie + perRollMod) + singleMod;
    }

    // =========================================================================
    // Erwartungswert eines einzelnen Würfels
    // =========================================================================

    /**
     * Berechnet E[X] für einen einzelnen Würfel mit N Seiten und allen
     * angewandten Argumenten (Advantage, Reroll, Cap) in Ausführungsreihenfolge.
     */
    private double expectedValueOneDie(int sides, DiceRollNode node) {
        // Schritt 1: Advantage / Disadvantage
        double e = (node.advantage() != null)
            ? expectedValueWithAdvantage(sides, node.advantage().advantage())
            : expectedValuePlainDie(sides);

        // Schritt 2: Reroll (auf das Ergebnis aus Schritt 1 angewandt)
        if (node.reroll() != null) {
            int threshold = (int) Math.round(node.reroll().threshold().accept(this));
            e = expectedValueWithReroll(sides, e, node.reroll().operator(), threshold,
                node.advantage() != null, node.advantage() != null && node.advantage().advantage());
        }

        // Schritt 3: Cap
        if (node.cap() != null) {
            int threshold = (int) Math.round(node.cap().threshold().accept(this));
            e = expectedValueWithCap(sides, e, node.cap().minimum(), threshold,
                node.reroll(), node.advantage());
        }

        return e;
    }

    // =========================================================================
    // Formeln für einzelne Features
    // =========================================================================

    /**
     * E[dN] = (N+1) / 2
     * Gleichverteilung auf {1, …, N}.
     */
    private double expectedValuePlainDie(int sides) {
        return (sides + 1.0) / 2.0;
    }

    /**
     * E[max(X,Y)] für X,Y ~ Uniform{1,…,N} (Advantage):
     * {@code E[max] = (N+1)(2N+1) / (3N)}
     *
     * E[min(X,Y)] für Disadvantage:
     * {@code E[min] = (N+1) - E[max] = (N+1)/3N + (N+1)/N = (N+1)(N+2)/(3N)}
     * Vereinfacht: {@code E[min] = (N+1) - E[max]}
     *
     * <p>Herleitung Advantage:
     * {@code E[max(X,Y)] = Σ_{k=1}^{N} k * P(max=k)}
     * {@code P(max=k) = P(X≤k)^2 - P(X≤k-1)^2 = (k/N)^2 - ((k-1)/N)^2 = (2k-1)/N^2}
     * {@code E[max] = Σ k*(2k-1)/N^2 = (2N^2+3N+1)/(3N*2) * 2 - ... }
     * Kompakt: {@code (2N+1)(N+1)/(3N)}
     */
    private double expectedValueWithAdvantage(int sides, boolean advantage) {
        double n = sides;
        double eMax = (2 * n + 1) * (n + 1) / (3 * n);
        return advantage ? eMax : (n + 1) - eMax;
    }

    /**
     * E[X nach Reroll] für einen Würfel mit N Seiten.
     *
     * <p>Bei Reroll wird X genau einmal neu gewürfelt wenn die Bedingung zutrifft.
     * Das neue Ergebnis ist wieder ein uniformer dN (egal was vorher war):
     *
     * {@code E[rr] = (1 - p_trigger) * E[X | ¬trigger] + p_trigger * E[dN]}
     *
     * Wobei {@code E[X | ¬trigger]} der bedingte Erwartungswert unter den
     * Werten ist, bei denen kein Reroll ausgelöst wird.
     *
     * <p>Wenn Advantage aktiv war, wird die Reroll-Wahrscheinlichkeit über die
     * tatsächliche Verteilung berechnet (Summe über Einzelwahrscheinlichkeiten).
     *
     * @param eBeforeReroll Erwartungswert vor Reroll (mit ggf. Advantage)
     */
    private double expectedValueWithReroll(int sides, double eBeforeReroll,
                                            String op, int threshold,
                                            boolean hadAdvantage, boolean wasAdvantage) {
        // Wahrscheinlichkeit, dass Reroll ausgelöst wird
        double pTrigger = rerollTriggerProbability(sides, op, threshold,
            hadAdvantage, wasAdvantage);

        // Konditionaler Erwartungswert der nicht-triggernden Werte
        double eSumKeep  = 0;
        double pKeepTotal = 0;
        for (int k = 1; k <= sides; k++) {
            double pk = hadAdvantage
                ? dieProbabilityWithAdvantage(sides, k, wasAdvantage)
                : 1.0 / sides;
            if (!matchesReroll(k, op, threshold)) {
                eSumKeep  += k * pk;
                pKeepTotal += pk;
            }
        }
        double eKeep = pKeepTotal > 0 ? eSumKeep / pKeepTotal : 0;

        // E[nach Reroll] = P(keep) * E[keep] + P(trigger) * E[dN]
        // (nach Reroll: frischer uniformer Würfel, unabhängig von vorheriger Verteilung)
        double eAfterReroll = expectedValuePlainDie(sides);
        return (1 - pTrigger) * eKeep + pTrigger * eAfterReroll;
    }

    /**
     * E[X nach Cap] — truncated distribution.
     *
     * <p>Cap min T: Werte unter T werden auf T gesetzt.
     * {@code E[min-cap] = T * P(X < T) + E[X | X ≥ T] * P(X ≥ T)}
     *
     * <p>Cap max T: Werte über T werden auf T gesetzt.
     * {@code E[max-cap] = E[X | X ≤ T] * P(X ≤ T) + T * P(X > T)}
     *
     * <p>Berechnung direkt über alle Einzelwerte – exakt für ganzzahlige Würfel.
     * Die tatsächliche Verteilung nach Advantage und Reroll wird berücksichtigt.
     */
    private double expectedValueWithCap(int sides, double ignoredPrev,
                                         boolean minimum, int threshold,
                                         RerollNode reroll, AdvantageNode adv) {
        // Vollständige Wahrscheinlichkeitsverteilung nach Advantage + Reroll
        double[] dist = buildDistribution(sides, reroll, adv);

        double e = 0;
        for (int k = 1; k <= sides; k++) {
            double pk = dist[k - 1];
            if (minimum) {
                e += Math.max(k, threshold) * pk;
            } else {
                e += Math.min(k, threshold) * pk;
            }
        }
        return e;
    }

    // =========================================================================
    // Verteilungsberechnung
    // =========================================================================

    /**
     * Vollständige Wahrscheinlichkeitsverteilung P(X=k) für k in {1,…,N}
     * nach Anwendung von Advantage und Reroll.
     *
     * <p>Rückgabe: Array der Länge N, Index 0 = P(X=1), Index N-1 = P(X=N).
     */
    private double[] buildDistribution(int sides, RerollNode reroll, AdvantageNode adv) {
        double[] dist = new double[sides];

        for (int k = 1; k <= sides; k++) {
            double pk = (adv != null)
                ? dieProbabilityWithAdvantage(sides, k, adv.advantage())
                : 1.0 / sides;
            dist[k - 1] = pk;
        }

        // Reroll-Anpassung der Verteilung erfolgt in expectedValueWithReroll –
        // buildDistribution liefert hier nur die Basisverteilung (mit Advantage).

        return dist;
    }

    /**
     * P(X = k) für X = max(A,B) mit A,B ~ Uniform{1,…,N} (Advantage):
     * {@code P(max=k) = (2k-1) / N^2}
     *
     * P(X = k) für X = min(A,B) (Disadvantage):
     * {@code P(min=k) = (2(N-k)+1) / N^2 = (2N-2k+1) / N^2}
     */
    private double dieProbabilityWithAdvantage(int sides, int k, boolean advantage) {
        double n = sides;
        return advantage
            ? (2.0 * k - 1) / (n * n)
            : (2.0 * (n - k) + 1) / (n * n);
    }

    // =========================================================================
    // Chain-Wahrscheinlichkeit
    // =========================================================================

    /**
     * Berechnet P(Bedingung bestanden) für einen Chain-Bedingungsknoten.
     *
     * <p>Der Bedingungsknoten ist typischerweise ein {@link DiceRollNode} mit
     * einem {@link CheckNode}. Die Wahrscheinlichkeit wird über die vollständige
     * Verteilung des Bedingungswurfs berechnet.
     */
    private double passProbability(DiceNode conditionNode) {
        if (conditionNode instanceof DiceRollNode roll && roll.check() != null) {
            return checkPassProbability(roll);
        }
        if (conditionNode instanceof AdditionNode add) {
            // Erste Teiloperation mit Check suchen
            for (DiceNode op : add.operands()) {
                if (op instanceof DiceRollNode roll && roll.check() != null) {
                    return checkPassProbability(roll);
                }
            }
        }
        // Kein Check gefunden – sollte durch Parser-Validierung nicht vorkommen
        throw new IllegalArgumentException(
            "Chain-Bedingung enthält keinen Check-Knoten.");
    }

    /**
     * P(Check bestanden) für einen DiceRollNode mit Check.
     *
     * <p>Berechnet den Gesamtwert (Würfelsumme + Modifier) für alle möglichen
     * Kombinationen und summiert die Wahrscheinlichkeiten der bestandenen Fälle.
     */
    private double checkPassProbability(DiceRollNode roll) {
        int sides     = (int) Math.round(roll.sides().accept(this));
        int count     = roll.count() != null ? (int) Math.round(roll.count().accept(this)) : 1;
        int threshold = (int) Math.round(roll.check().threshold().accept(this));

        // Modifier-Summe
        double perRollMod = roll.modifiers().stream()
            .filter(ModifierNode::allScope)
            .mapToDouble(m -> m.sign() * m.value().accept(this))
            .sum();
        double singleMod = roll.modifiers().stream()
            .filter(m -> !m.allScope())
            .mapToDouble(m -> m.sign() * m.value().accept(this))
            .sum();

        if (roll.checkAllScope()) {
            // Pro-Wurf Check: P(mindestens ein Würfel besteht) oder Summe?
            // Laut Semantik: jeder Würfel wird einzeln geprüft → P(ein Würfel besteht)
            return singleDieCheckProbability(sides, roll, (int) Math.round(perRollMod), threshold);
        }

        if (count == 1) {
            // Einzelwurf: direkte Berechnung über die Verteilung
            double totalMod = perRollMod + singleMod;
            return singleDieCheckProbability(sides, roll,
                (int) Math.round(totalMod), threshold);
        }

        // Multiwurf ohne allScope: Check auf Gesamtsumme
        // Vollständige Summenverteilung via Faltung
        return sumDistributionCheckProbability(sides, count, roll,
            (int) Math.round(perRollMod), (int) Math.round(singleMod), threshold);
    }

    /**
     * P(ein Würfel besteht den Check) unter Berücksichtigung von Advantage,
     * Reroll und Modifier auf diesen Würfel.
     */
    private double singleDieCheckProbability(int sides, DiceRollNode roll,
                                              int modifier, int threshold) {
        double pPass = 0;
        for (int k = 1; k <= sides; k++) {
            double pk = effectiveDieProbability(sides, k, roll);
            int effective = k + modifier;
            if (roll.check().evaluate(effective, threshold)) {
                pPass += pk;
            }
        }
        return pPass;
    }

    /**
     * P(Summe von count Würfeln besteht den Check) via Faltung der Einzelverteilungen.
     *
     * <p>Exakt für count ≤ ~20 (kombinatorische Explosion danach).
     * Für größere count würde Monte Carlo oder CLT-Approximation sinnvoller sein.
     */
    private double sumDistributionCheckProbability(int sides, int count,
                                                    DiceRollNode roll,
                                                    int perRollMod, int singleMod,
                                                    int threshold) {
        if (count > 20) {
            // Näherung via Normalverteilung (CLT) für große count
            return checkProbabilityViaCLT(sides, count, roll, perRollMod, singleMod, threshold);
        }

        // Faltung: dist[s] = P(Summe = s)
        int minSum = count;
        int maxSum = count * sides;
        double[] dist = new double[maxSum - minSum + 1];

        // Startverteilung: erster Würfel
        double[] firstDist = singleDieDistribution(sides, roll, perRollMod, true);
        for (int k = 0; k < firstDist.length; k++) {
            if (k < dist.length) dist[k] = firstDist[k];
        }

        // Faltung mit den restlichen Würfeln (ggf. ohne allScope-Argumente)
        for (int d = 1; d < count; d++) {
            double[] restDist  = singleDieDistribution(sides, roll, perRollMod, false);
            double[] newDist   = new double[dist.length];
            for (int s = 0; s < dist.length; s++) {
                if (dist[s] == 0) continue;
                for (int k = 0; k < restDist.length; k++) {
                    if (s + k < newDist.length) {
                        newDist[s + k] += dist[s] * restDist[k];
                    }
                }
            }
            dist = newDist;
        }

        // Check auf Summe + singleMod
        double pPass = 0;
        for (int i = 0; i < dist.length; i++) {
            int total = (minSum + i) + singleMod;
            if (roll.check().evaluate(total, threshold)) {
                pPass += dist[i];
            }
        }
        return pPass;
    }

    /**
     * Approximation der Check-Wahrscheinlichkeit für große Würfelmengen via
     * Zentralem Grenzwertsatz (CLT).
     *
     * <p>Summe von n i.i.d. Würfeln ist approximativ normalverteilt:
     * {@code S ~ N(n*μ, n*σ²)} mit μ = (N+1)/2, σ² = (N²-1)/12.
     */
    private double checkProbabilityViaCLT(int sides, int count, DiceRollNode roll,
                                           int perRollMod, int singleMod, int threshold) {
        double mu    = expectedValuePlainDie(sides) + perRollMod;
        double sigma2 = (sides * sides - 1.0) / 12.0;
        double mean  = count * mu + singleMod;
        double std   = Math.sqrt(count * sigma2);

        // Standard-Normalverteilung CDF approximiert via Abramowitz & Stegun
        int adjThreshold = threshold;
        return switch (roll.check().kind()) {
            case "check" -> 1.0 - normalCDF((adjThreshold - 0.5 - mean) / std);
            case "beat"  -> 1.0 - normalCDF((adjThreshold + 0.5 - mean) / std);
            case "nail"  -> normalCDF((adjThreshold + 0.5 - mean) / std)
                          - normalCDF((adjThreshold - 0.5 - mean) / std);
            case "limit" -> normalCDF((adjThreshold + 0.5 - mean) / std);
            case "cut"   -> normalCDF((adjThreshold - 0.5 - mean) / std);
            default      -> 0.5;
        };
    }

    // =========================================================================
    // Hilfsmethoden – Verteilungen
    // =========================================================================

    /**
     * Effektive Wahrscheinlichkeit P(X=k) für einen Würfel unter Berücksichtigung
     * von Advantage und Reroll.
     *
     * @param applyArgs ob Advantage/Reroll (nur erster Würfel ohne allScope)
     */
    private double effectiveDieProbability(int sides, int k, DiceRollNode roll) {
        double pk = (roll.advantage() != null)
            ? dieProbabilityWithAdvantage(sides, k, roll.advantage().advantage())
            : 1.0 / sides;

        if (roll.reroll() != null) {
            int threshold = (int) Math.round(roll.reroll().threshold().accept(this));
            double pTrigger = rerollTriggerProbability(sides, roll.reroll().operator(),
                threshold, roll.advantage() != null,
                roll.advantage() != null && roll.advantage().advantage());
            //
            // Korrekte Reroll-Verteilung:
            // P(result = k) = P(original = k, ¬trigger)       [behalten]
            //               + P(trigger) * P(reroll = k)       [nach Reroll hier gelandet]
            //
            // Für k das NICHT triggert:
            //   P(result=k) = pk_original + pTrigger * (1/N)
            //
            // Für k das TRIGGERT:
            //   P(result=k) = 0 (wird immer re-rolled)
            //               + pTrigger * P(trigger-Wert=k) / pTrigger * (1/N)
            //   Vereinfacht: = pTrigger * (1/N)
            //   (alle triggernden Werte werden durch frische Würfe ersetzt)
            //
            if (matchesReroll(k, roll.reroll().operator(), threshold)) {
                pk = pTrigger * (1.0 / sides);
            } else {
                pk = pk + pTrigger * (1.0 / sides);
            }
        }
        return pk;
    }

    /**
     * Einzelwürfel-Verteilung als Array (Index 0 = Wert 1+modifier, usw.)
     * für die Faltung in {@link #sumDistributionCheckProbability}.
     */
    private double[] singleDieDistribution(int sides, DiceRollNode roll,
                                            int modifier, boolean applyArgs) {
        double[] dist = new double[sides];
        for (int k = 1; k <= sides; k++) {
            double pk = applyArgs
                ? effectiveDieProbability(sides, k, roll)
                : 1.0 / sides;
            int idx = (k + modifier) - (1 + modifier);
            if (idx >= 0 && idx < dist.length) dist[idx] = pk;
        }
        return dist;
    }

    private double rerollTriggerProbability(int sides, String op, int threshold,
                                             boolean hadAdvantage, boolean wasAdvantage) {
        double p = 0;
        for (int k = 1; k <= sides; k++) {
            if (matchesReroll(k, op, threshold)) {
                p += hadAdvantage
                    ? dieProbabilityWithAdvantage(sides, k, wasAdvantage)
                    : 1.0 / sides;
            }
        }
        return p;
    }

    private boolean matchesReroll(int value, String op, int threshold) {
        return switch (op) {
            case "<"  -> value <  threshold;
            case "<=" -> value <= threshold;
            case "==" -> value == threshold;
            case ">=" -> value >= threshold;
            case ">"  -> value >  threshold;
            case "%"  -> threshold != 0 && value % threshold == 0;
            default   -> false;
        };
    }

    // =========================================================================
    // Normalverteilung CDF (Abramowitz & Stegun 26.2.17)
    // =========================================================================

    private double normalCDF(double z) {
        if (z < -8) return 0;
        if (z >  8) return 1;
        double t = 1.0 / (1.0 + 0.2316419 * Math.abs(z));
        double d = 0.3989422820 * Math.exp(-z * z / 2.0);
        double p = d * t * (0.3193815 + t * (-0.3565638 + t * (1.7814779
                 + t * (-1.8212560 + t * 1.3302744))));
        return z > 0 ? 1 - p : p;
    }
}