package de.fabiansiemens.hyperion.core.features.roll.v2;

import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.execution.InnerRole;
import de.fabiansiemens.hyperion.core.features.roll.v2.execution.NodeKind;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.*;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Wertet einen Würfelausdruck aus und liefert einen vollständigen {@link ExecutionNode}-Baum.
 *
 * <p>Der Baum bildet die Ausführungsreihenfolge ab (nicht den AST): alle Einzelwürfe
 * inklusive verworfener, Inner Rolls, Chains und Additionen sind als Kindknoten sichtbar.
 *
 * <p><strong>Thread-Sicherheit:</strong> Jede Evaluator-Instanz ist zustandslos
 * zwischen Aufrufen – alle Zwischendaten werden lokal auf dem Stack gehalten.
 */
public class RollEvaluator implements DiceNodeVisitor<ExecutionNode> {

    // =========================================================================
    // Konfiguration
    // =========================================================================

    /**
     * Krit/Fail-Konfiguration.
     *
     * <p><strong>Erweiterungshinweis:</strong> Bewusst als separater Record gestaltet,
     * damit sie zukünftig pro Spielerprofil instanziiert und dem Evaluator übergeben
     * werden kann, ohne die Evaluator-API zu ändern.
     */
    public record Config(int critThreshold, int failThreshold, int critSides) {

        public static final Config DEFAULT = new Config(20, 1, 20);

        public static Config withCritRange(int fail, int crit) {
            return new Config(crit, fail, 20);
        }
    }

    // =========================================================================
    // Felder
    // =========================================================================

    private final Config      config;
    private final DiceBounds  bounds;
    private final DiceRoller  roller;

    public RollEvaluator() {
        this(new Random(), Config.DEFAULT, DiceBounds.DEFAULT);
    }

    public RollEvaluator(Random random, Config config, DiceBounds bounds) {
        this.config = config;
        this.bounds = bounds;
        this.roller = new DiceRoller(random);
    }

    // =========================================================================
    // Öffentliche API
    // =========================================================================

    public ExecutionNode evaluate(String expression) {
        String      stripped = expression.strip();
        List<Token> tokens   = new DiceLexer(stripped).tokenize();
        DiceNode    ast      = new DiceParser(tokens, bounds).parse();
        ExecutionNode root   = ast.accept(this);
        return root.getPrompt() == null || root.getPrompt().isBlank()
            ? rebuildWithPrompt(root, stripped)
            : root;
    }

    // =========================================================================
    // Visitor
    // =========================================================================

    @Override
    public ExecutionNode visitNumber(NumberNode node) {
        return ExecutionNode.builder()
            .kind(NodeKind.CONSTANT)
            .prompt(String.valueOf(node.value()))
            .resolvedValue(node.value())
            .build();
    }

    @Override
    public ExecutionNode visitGrouping(GroupingNode node) {
        return node.inner().accept(this);
    }

    @Override
    public ExecutionNode visitCounted(CountedNode node) {
        List<ExecutionNode> childList = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < node.count(); i++) {
            ExecutionNode child = node.inner().accept(this);
            childList.add(child);
            total += child.getResolvedValue();
        }
        String innerPrompt = childList.isEmpty() ? "?" : childList.get(0).getPrompt();
        String prompt      = node.count() + "(" + innerPrompt + ")";

        ExecutionNode.ExecutionNodeBuilder b = ExecutionNode.builder()
            .kind(NodeKind.ROOT).prompt(prompt).resolvedValue(total);
        childList.forEach(b::child);
        return b.build();
    }

    @Override
    public ExecutionNode visitAddition(AdditionNode node) {
        List<ExecutionNode> childList = new ArrayList<>();
        List<Integer>       signs     = node.signs();
        int total = 0;

        for (int i = 0; i < node.operands().size(); i++) {
            ExecutionNode child = node.operands().get(i).accept(this);
            childList.add(child);
            total += signs.get(i) * child.getResolvedValue();
        }

        StringBuilder prompt = new StringBuilder();
        for (int i = 0; i < childList.size(); i++) {
            if (i > 0) prompt.append(signs.get(i) >= 0 ? ", " : ", -");
            else if (signs.get(i) < 0) prompt.append("-");
            prompt.append(childList.get(i).getPrompt());
        }

        ExecutionNode.ExecutionNodeBuilder b = ExecutionNode.builder()
            .kind(NodeKind.ROOT).prompt(prompt.toString()).resolvedValue(total);
        childList.forEach(b::child);
        return b.build();
    }

    @Override
    public ExecutionNode visitChain(ChainNode node) {
        ExecutionNode condNode = node.condition().accept(this);
        ExecutionNode thenNode = node.thenBranch().accept(this);
        ExecutionNode elseNode = node.elseBranch() != null
            ? node.elseBranch().accept(this) : null;

        boolean passed = condNode.hasCheck() && condNode.getChecks().get(0).isPassed();

        // Vollstaendigen Prompt aus allen Zweigen zusammensetzen
        StringBuilder prompt = new StringBuilder(condNode.getPrompt())
            .append(" ? ").append(thenNode.getPrompt());
        if (elseNode != null)
            prompt.append(" : ").append(elseNode.getPrompt());

        // Chain-Knoten ist reiner Container:
        // condNode bleibt als erstes Kind erhalten statt seine Daten
        // direkt in den Chain-Knoten zu kopieren. Dadurch ist der
        // Bedingungswurf als eigenstaendiger Knoten navigierbar.
        ExecutionNode.ExecutionNodeBuilder chain = ExecutionNode.builder()
            .kind(NodeKind.CHAIN)
            .prompt(prompt.toString())
            .chainPassed(passed)
            .child(condNode);

        if (passed) {
            return chain.child(thenNode).resolvedValue(thenNode.getResolvedValue()).build();
        }
        if (elseNode != null) {
            return chain.child(elseNode).resolvedValue(elseNode.getResolvedValue()).build();
        }
        return chain.resolvedValue(0).build();
    }

    @Override
    public ExecutionNode visitDiceRoll(DiceRollNode node) {
        ExecutionNode.ExecutionNodeBuilder nb = ExecutionNode.builder().kind(NodeKind.ROLL);

        // --- Seiten auflösen ---
        ResolvedInner sides = resolveInnerFull(node.sides(), InnerRole.SIDES, nb);
        if (sides.value() < bounds.sidesMin() || sides.value() > bounds.sidesMax()) {
            throw new IllegalArgumentException(String.format(
                "Seitenanzahl %d liegt außerhalb des erlaubten Bereichs [%d, %d].",
                sides.value(), bounds.sidesMin(), bounds.sidesMax()));
        }

        // --- Anzahl auflösen ---
        ResolvedInner count = node.count() != null
            ? resolveInnerFull(node.count(), InnerRole.COUNT, nb)
            : new ResolvedInner(1, null);
        if (count.value() < bounds.countMin() || count.value() > bounds.countMax()) {
            throw new IllegalArgumentException(String.format(
                "Würfelanzahl %d liegt außerhalb des erlaubten Bereichs [%d, %d].",
                count.value(), bounds.countMin(), bounds.countMax()));
        }

        // --- Modifikatoren einzeln auflösen und speichern ---
        int perRollModTotal = 0;
        int singleModTotal  = 0;
        List<ResolvedModifier> resolvedMods = new ArrayList<>();
        for (ModifierNode mod : node.modifiers()) {
            int resolved = mod.sign() * resolveInner(mod.value(), InnerRole.MODIFIER, nb).value();
            nb.modifier(new ModifierEntry(resolved, mod.allScope()));
            resolvedMods.add(new ResolvedModifier(resolved, mod.allScope()));
            if (mod.allScope()) perRollModTotal += resolved;
            else                singleModTotal  += resolved;
        }

        // --- Schwellwerte auflösen ---
        int rerollThreshold = node.reroll() != null
            ? resolveInner(node.reroll().threshold(), InnerRole.REROLL_THRESHOLD, nb).value() : 0;
        int capThreshold    = node.cap()    != null
            ? resolveInner(node.cap().threshold(),    InnerRole.NONE,             nb).value() : 0;
        int checkThreshold  = node.check()  != null
            ? resolveInner(node.check().threshold(),  InnerRole.CHECK_THRESHOLD,  nb).value() : 0;

        // --- Prompt mit allen aufgelösten Werten setzen ---
        nb.prompt(buildPrompt(count, sides, node,
            resolvedMods, rerollThreshold, capThreshold, checkThreshold));

        // --- Modulo-Guard ---
        if (node.reroll() != null && "%".equals(node.reroll().operator()) && rerollThreshold == 0) {
            throw new IllegalArgumentException("Reroll-Bedingung '%0' ist ungültig: Division durch 0.");
        }

        final int fCount           = count.value();
        final int fSides           = sides.value();
        final int fPerRollMod      = perRollModTotal;
        final int fSingleMod       = singleModTotal;
        final int fRerollThreshold = rerollThreshold;
        final int fCapThreshold    = capThreshold;
        final int fCheckThreshold  = checkThreshold;

        // --- Würfeln ---
        List<DiceEntry> allDice = new ArrayList<>();
        for (int i = 0; i < fCount; i++) {
            allDice.addAll(roller.roll(fSides, node, i, fRerollThreshold, fCapThreshold));
        }
        allDice.forEach(nb::die);

        // --- Gesamtwert ---
        int baseTotal = allDice.stream()
            .filter(d -> !d.isIgnored())
            .mapToInt(d -> d.getValue() + fPerRollMod)
            .sum();
        int total = baseTotal + fSingleMod;

        // --- Crits ---
        if (fSides == config.critSides()) {
            allDice.stream().filter(d -> !d.isIgnored()).forEach(d -> {
                if      (d.getValue() >= config.critThreshold()) nb.crit(new CritEntry(d.getValue(), true));
                else if (d.getValue() <= config.failThreshold()) nb.crit(new CritEntry(d.getValue(), false));
            });
        }

        // --- Checks ---
        if (node.check() != null) {
            if (node.checkAllScope()) {
                allDice.stream().filter(d -> !d.isIgnored()).forEach(d -> {
                    int against = d.getValue() + fPerRollMod;
                    nb.check(new CheckEntry(node.check().kind(), fCheckThreshold, against,
                        node.check().evaluate(against, fCheckThreshold)));
                });
            } else {
                nb.check(new CheckEntry(node.check().kind(), fCheckThreshold, total,
                    node.check().evaluate(total, fCheckThreshold)));
            }
        }

        return nb.resolvedValue(total).build();
    }

    // =========================================================================
    // Hilfsmethoden
    // =========================================================================

    /**
     * Löst einen Parameterknoten auf und gibt Wert + Prompt zurück.
     * Für Inner Rolls (Gruppierung / Würfelwurf) wird der Knoten als
     * INNER-Kind registriert; sein Prompt ist der Würfelausdruck (z. B. "d4").
     * Für Konstanten ist der Prompt null.
     */
    private ResolvedInner resolveInnerFull(DiceNode node, InnerRole role,
                                           ExecutionNode.ExecutionNodeBuilder parent) {
        if (node instanceof GroupingNode || node instanceof DiceRollNode) {
            ExecutionNode inner = wrapAsInner(node.accept(this), role);
            parent.child(inner);
            return new ResolvedInner(inner.getResolvedValue(), inner.getPrompt());
        }
        return new ResolvedInner(node.accept(this).getResolvedValue(), null);
    }

    /** Wie {@link #resolveInnerFull}, gibt nur den Wert zurück. */
    private ResolvedInner resolveInner(DiceNode node, InnerRole role,
                                       ExecutionNode.ExecutionNodeBuilder parent) {
        return resolveInnerFull(node, role, parent);
    }

    private ExecutionNode wrapAsInner(ExecutionNode node, InnerRole role) {
        return ExecutionNode.builder()
            .kind(NodeKind.INNER)
            .prompt(node.getPrompt())
            .dice(node.getDice())
            .modifiers(node.getModifiers())
            .checks(node.getChecks())
            .crits(node.getCrits())
            .innerRole(role)
            .resolvedValue(node.getResolvedValue())
            .children(node.getChildren())
            .build();
    }

    private ExecutionNode rebuildWithPrompt(ExecutionNode root, String expression) {
        return ExecutionNode.builder()
            .kind(root.getKind())
            .prompt(expression)
            .dice(root.getDice())
            .modifiers(root.getModifiers())
            .checks(root.getChecks())
            .crits(root.getCrits())
            .innerRole(root.getInnerRole())
            .resolvedValue(root.getResolvedValue())
            .chainPassed(root.isChainPassed())
            .children(root.getChildren())
            .build();
    }

    // =========================================================================
    // Prompt-Aufbau
    // =========================================================================

    /** Interner Record: aufgelöster Modifikatorwert mit Scope-Info. */
    private record ResolvedModifier(int value, boolean allScope) {}
    private record ResolvedInner(int value, String prompt) {}

    /**
     * Baut den vollständigen Prompt-String eines Würfelknotens auf –
     * erst nachdem alle Werte aufgelöst wurden, damit keine Platzhalter nötig sind.
     */
    private String buildPrompt(ResolvedInner count, ResolvedInner sides,
                                DiceRollNode node,
                                List<ResolvedModifier> resolvedMods,
                                int rerollThreshold,
                                int capThreshold,
                                int checkThreshold) {
        // Anzahl und Seiten: Inner-Roll-Prompt wenn vorhanden, sonst Zahlwert
        String countStr = count.prompt() != null
            ? "(" + count.prompt() + ")" : String.valueOf(count.value());
        String sidesStr = sides.prompt() != null
            ? "(" + sides.prompt() + ")" : String.valueOf(sides.value());

        StringBuilder sb = new StringBuilder(countStr).append("d").append(sidesStr);

        // Modifikatoren mit aufgelösten Werten
        for (ResolvedModifier mod : resolvedMods) {
            String op  = mod.allScope() ? (mod.value() >= 0 ? " ++" : " --")
                                        : (mod.value() >= 0 ? " +"  : " -");
            sb.append(op).append(Math.abs(mod.value()));
        }

        // Advantage / Disadvantage
        if (node.advantage() != null)
            sb.append(" ").append(node.advantage().advantage() ? "adv" : "disadv")
              .append(node.advAllScope() ? "all" : "");

        // Reroll mit aufgelöstem Schwellwert
        if (node.reroll() != null)
            sb.append(" rr").append(node.reroll().operator()).append(rerollThreshold)
              .append(node.rerollAllScope() ? "all" : "");

        // Cap mit aufgelöstem Schwellwert
        if (node.cap() != null)
            sb.append(" ").append(node.cap().minimum() ? "min" : "max").append(capThreshold)
              .append(node.capAllScope() ? "all" : "");

        // Check mit aufgelöstem Schwellwert
        if (node.check() != null)
            sb.append(" ").append(node.check().kind()).append(checkThreshold)
              .append(node.checkAllScope() ? "all" : "");

        return sb.toString();
    }
}