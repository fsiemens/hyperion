package de.fabiansiemens.hyperion.core.features.roll.v2.execution;

import java.util.ArrayList;
import java.util.List;

import de.fabiansiemens.hyperion.core.features.roll.v2.node.FlatNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.CheckEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.CritEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.DiceEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.ModifierEntry;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * Ein Knoten im Ausführungsbaum einer Würfeloperation.
 *
 * <p>Der Baum spiegelt die tatsächliche Ausführungsreihenfolge wider – nicht den AST.
 * Jede Teiloperation (Inner Roll, Folgewurf, Additionsoperand) ist ein eigener Kindknoten.
 *
 * <p><strong>Typ-Prüfung beim Traversieren</strong> – über {@link NodeKind}:
 * <pre>{@code
 * for (FlatNode flat : root.flatten()) {
 *     NodeKind kind = flat.node().getKind();
 *     if (kind.isRoll())  { /* direkter Würfelwurf *​/ }
 *     if (kind.isInner()) { /* Inner Roll als Parameter *​/ }
 *     if (kind.isChain()) { /* Verkettung nach ? *​/ }
 * }
 * }</pre>
 *
 * <p><strong>Datentrennung:</strong> Jeder Knoten enthält <em>ausschließlich</em>
 * seine eigenen {@code dice}, {@code modifiers} und {@code checks} –
 * nicht die seiner Kindknoten. Für eine rekursive Gesamtliste: {@link #allDiceDeep()}.
 *
 * <pre>
 * Beispiel für "4(d20 check10 ? (d4)d8)":
 *
 *   ROOT   prompt="4(d20 check10 ? (d4)d8)"                        →42
 *   ├── CHAIN  prompt="d20 check10 ? (d4)d8"  chainPassed=true     →19
 *   │   ├── ROLL   prompt="1d20 check10"  dice=[17]  checks=[17≥10✓]  →17
 *   │   │   └── INNER[COUNT]  prompt="d4"  dice=[3]                →3
 *   │   └── ROLL   prompt="3d8"  dice=[5,8,6]                      →19
 *   ├── CHAIN  prompt="d20 check10 ? (d4)d8"  chainPassed=false    →0
 *   │   └── ROLL   prompt="1d20 check10"  dice=[4]  checks=[4≥10✗] →4
 *   └── CHAIN  prompt="d20 check10 ? (d4)d8"  chainPassed=true     →7
 *       ├── ROLL   prompt="1d20 check10"  dice=[13]  checks=[13≥10✓] →13
 *       │   └── INNER[COUNT]  prompt="d4"  dice=[2]                →2
 *       └── ROLL   prompt="2d8"  dice=[3,4]                        →7
 * </pre>
 */
@Getter
@Builder
public class ExecutionNode {

    private final NodeKind             kind;

    /**
     * Der Teilausdruck, der diesen Knoten erzeugt hat – z. B. {@code "4d20 ++4 advall"}.
     * Immer gesetzt; nie {@code null} oder leer.
     */
    private final String               prompt;

    /** Eigene Würfelergebnisse dieses Knotens (ohne Kinder). */
    @Singular("die") 
    private final List<DiceEntry> dice;

    /**
     * Einzeln aufgelistete Modifikatoren in der Reihenfolge ihres Auftretens
     * im Ausdruck – nicht vorab summiert.
     */
    @Singular private final List<ModifierEntry> modifiers;

    /** Check-Ergebnisse dieses Knotens. */
    @Singular private final List<CheckEntry>    checks;

    /** Kritische Würfelergebnisse dieses Knotens. */
    @Singular private final List<CritEntry>     crits;

    /**
     * Rolle dieses Knotens in der äußeren Operation.
     * Nur relevant wenn {@code kind == }{@link NodeKind#INNER}.
     */
    private final InnerRole            innerRole;

    /** Aufgelöstes Gesamtergebnis dieses Knotens. */
    private final int                  resolvedValue;

    /**
     * Ob die Chain-Bedingung bestanden wurde.
     * Nur relevant wenn {@code kind == }{@link NodeKind#CHAIN}.
     */
    private final boolean              chainPassed;

    /** Kindknoten in Ausführungsreihenfolge. */
    @Singular private final List<ExecutionNode> children;

    // =========================================================================
    // Typ-Prüfung (Delegation an NodeKind)
    // =========================================================================

    /** @see NodeKind#isRoll() */
    public boolean isRoll()     { return kind.isRoll(); }

    /** @see NodeKind#isInner() */
    public boolean isInner()    { return kind.isInner(); }

    /** @see NodeKind#isAnyRoll() */
    public boolean isAnyRoll()  { return kind.isAnyRoll(); }

    /** @see NodeKind#isChain() */
    public boolean isChain()    { return kind.isChain(); }

    /** @see NodeKind#isRoot() */
    public boolean isRoot()     { return kind.isRoot(); }

    /** @see NodeKind#isConstant() */
    public boolean isConstant() { return kind.isConstant(); }

    // =========================================================================
    // Abgeleitete Abfragen
    // =========================================================================

    public boolean hasCheck()    { return !checks.isEmpty(); }
    public boolean hasCrits()    { return !crits.isEmpty(); }
    public boolean hasChildren() { return !children.isEmpty(); }

    public int successCount() {
        return (int) checks.stream().filter(CheckEntry::isPassed).count();
    }

    public int failCount() {
        return checks.size() - successCount();
    }

    public int critSuccessCount() {
        return (int) crits.stream().filter(CritEntry::isCritSuccess).count();
    }

    public int critFailCount() {
        return crits.size() - critSuccessCount();
    }

    /** Alle aktiven (nicht verworfenen) Würfe dieses Knotens. */
    public List<DiceEntry> activeDice() {
        return dice.stream().filter(d -> !d.isIgnored()).toList();
    }

    /** Alle verworfenen Würfe dieses Knotens. */
    public List<DiceEntry> ignoredDice() {
        return dice.stream().filter(DiceEntry::isIgnored).toList();
    }

    /**
     * Alle Würfe dieses Knotens <em>und</em> aller Kindknoten (rekursiv, flach).
     * Nützlich für Gesamtstatistiken; für knotenspezifische Auswertung {@link #getDice()} verwenden.
     */
    public List<DiceEntry> allDiceDeep() {
        List<DiceEntry> all = new ArrayList<>(dice);
        for (ExecutionNode child : children) all.addAll(child.allDiceDeep());
        return all;
    }

    /** Summe der {@code resolvedValue}s aller direkten Kinder. */
    public int childrenTotal() {
        return children.stream().mapToInt(ExecutionNode::getResolvedValue).sum();
    }

    // =========================================================================
    // Flachstruktur für UI-Navigation
    // =========================================================================

    /**
     * Liefert alle Knoten des Baums in Pre-Order als flache Liste.
     *
     * <p>Geeignet für eine blätterbare Detail-Ansicht: Jeder Eintrag enthält
     * den Knoten, seine Tiefe und einen eindeutigen Index. Der Typ des Knotens
     * ist über {@link FlatNode#kind()} oder die {@code is*()}-Methoden abrufbar.
     */
    public List<FlatNode> flatten() {
        List<FlatNode> result = new ArrayList<>();
        flattenInto(this, 0, result);
        return result;
    }

    private static void flattenInto(ExecutionNode node, int depth, List<FlatNode> acc) {
        acc.add(new FlatNode(node, depth, acc.size()));
        for (ExecutionNode child : node.getChildren()) {
            flattenInto(child, depth + 1, acc);
        }
    }

    // =========================================================================
    // Debug-Ausgabe
    // =========================================================================

    public String toTree() {
        StringBuilder sb = new StringBuilder();
        appendTree(sb, "", true);
        return sb.toString();
    }

    private void appendTree(StringBuilder sb, String prefix, boolean isLast) {
        sb.append(prefix)
          .append(isLast ? "└── " : "├── ")
          .append("[").append(kind).append(innerRole != null && kind.isInner()
              ? "/" + innerRole : "").append("] ")
          .append(prompt);

        if (!dice.isEmpty()) {
            sb.append("  dice=").append(dice.stream()
                .map(d -> d.isIgnored() ? "~~" + d.getValue() + "~~" : String.valueOf(d.getValue()))
                .toList());
        }
        if (!modifiers.isEmpty()) {
            sb.append("  mods=").append(modifiers.stream().map(ModifierEntry::toString).toList());
        }
        if (!checks.isEmpty()) {
            sb.append("  checks=").append(checks.stream()
                .map(c -> c.getAgainst() + (c.isPassed() ? "✓" : "✗"))
                .toList());
        }
        sb.append("  →").append(resolvedValue).append("\n");

        String childPrefix = prefix + (isLast ? "    " : "│   ");
        for (int i = 0; i < children.size(); i++) {
            children.get(i).appendTree(sb, childPrefix, i == children.size() - 1);
        }
    }
}