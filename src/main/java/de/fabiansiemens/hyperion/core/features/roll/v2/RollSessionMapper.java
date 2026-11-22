package de.fabiansiemens.hyperion.core.features.roll.v2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.CheckEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.CritEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.DiceEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.ModifierEntry;
import de.fabiansiemens.hyperion.persistence.roll.CheckEntryEmbeddable;
import de.fabiansiemens.hyperion.persistence.roll.CritEntryEmbeddable;
import de.fabiansiemens.hyperion.persistence.roll.DiceEntryEmbeddable;
import de.fabiansiemens.hyperion.persistence.roll.ModifierEntryEmbeddable;
import de.fabiansiemens.hyperion.persistence.roll.RollNodeEntity;
import de.fabiansiemens.hyperion.persistence.roll.RollSessionEntity;

/**
 * Rekonstruiert einen {@link ExecutionNode}-Baum aus einer {@link RollSessionEntity}.
 *
 * <h2>Algorithmus</h2>
 * Die Knoten sind in Pre-Order mit {@code treeIndex} und {@code depth} gespeichert.
 * Pre-Order + Tiefe reichen aus um den Baum eindeutig zu rekonstruieren:
 *
 * <pre>
 * Beispiel (treeIndex / depth / prompt):
 *   0 / 0 / "d20 check15 ? 3d8 : 1d4"   ← Wurzel
 *   1 / 1 / "1d20 check15"               ← Kind von 0
 *   2 / 1 / "3d8"                        ← Kind von 0
 *
 * Stack-Zustand während Iteration:
 *   Nach Index 0: stack = [(depth=0, builder_0)]
 *   Bei Index 1:  depth=1 > prev=0  → builder_0 ist Elternteil
 *                 stack = [(0, b0), (1, b1)]
 *   Bei Index 2:  depth=1 == prev=1 → stack bis depth=0 leeren, b0 ist Elternteil
 *                 stack = [(0, b0), (1, b2)]
 * </pre>
 */
@Component
public class RollSessionMapper {

    // =========================================================================
    // Session → ExecutionNode
    // =========================================================================

    /**
     * Rekonstruiert den vollständigen {@link ExecutionNode}-Baum
     * aus einer {@link RollSessionEntity}.
     *
     * <p>Die Nodes müssen nach {@code treeIndex} aufsteigend sortiert sein
     * (wird durch {@code @OrderBy("tree_index ASC")} auf der Relation sichergestellt).
     *
     * @param session persistierte Würfelsession
     * @return Wurzelknoten des rekonstruierten Ausführungsbaums
     * @throws IllegalStateException wenn die gespeicherten Knoten inkonsistent sind
     */
    public ExecutionNode toExecutionNode(RollSessionEntity session) {
        List<RollNodeEntity> nodes = session.getNodes();
        if (nodes.isEmpty()) throw new IllegalStateException(
            "Session " + session.getId() + " enthält keine Knoten.");

        // Stack: (depth, builder) – erlaubt effizientes Finden des Elternknotens
        Deque<DepthAndBuilder> stack = new ArrayDeque<>();

        // Fertiger Baum: für jeden treeIndex den fertig gebauten Knoten
        // (wird benötigt damit Elternknoten die Kinder als fertige Instanzen erhalten)
        // Da ExecutionNode immutable ist, bauen wir von den Blättern hoch –
        // daher zwei Pässe: erst alle Builder anlegen, dann in umgekehrter Reihenfolge bauen.
        Map<Integer, ExecutionNode.ExecutionNodeBuilder> builders = new LinkedHashMap<>();
        Map<Integer, Integer> parentIndex = new HashMap<>(); // childIndex → parentIndex

        // --- Pass 1: Eltern-Kind-Beziehungen bestimmen ---
        for (RollNodeEntity node : nodes) {
            ExecutionNode.ExecutionNodeBuilder builder = toBuilder(node);
            builders.put(node.getTreeIndex(), builder);

            // Stack aufräumen bis der Elternteil gefunden ist
            while (!stack.isEmpty() && stack.peek().depth() >= node.getDepth()) {
                stack.pop();
            }

            if (!stack.isEmpty()) {
                // Elternteil ist der aktuelle Stack-Kopf
                parentIndex.put(node.getTreeIndex(), stack.peek().treeIndex());
            }

            stack.push(new DepthAndBuilder(node.getDepth(), node.getTreeIndex()));
        }

        // --- Pass 2: Kinder in umgekehrter Pre-Order-Reihenfolge einhängen ---
        // Umgekehrte Reihenfolge stellt sicher, dass Blätter zuerst gebaut werden
        List<RollNodeEntity> reversed = new ArrayList<>(nodes);
        Collections.reverse(reversed);

        Map<Integer, ExecutionNode> builtNodes = new LinkedHashMap<>();

        for (RollNodeEntity node : reversed) {
            ExecutionNode.ExecutionNodeBuilder builder = builders.get(node.getTreeIndex());
            // Kinder dieses Knotens sind bereits gebaut (da wir rückwärts gehen)
            nodes.stream()
                .filter(n -> Integer.valueOf(node.getTreeIndex())
                    .equals(parentIndex.get(n.getTreeIndex())))
                .sorted(Comparator.comparingInt(RollNodeEntity::getTreeIndex))
                .forEach(child -> builder.child(builtNodes.get(child.getTreeIndex())));

            builtNodes.put(node.getTreeIndex(), builder.build());
        }

        return builtNodes.get(0);
    }

    // =========================================================================
    // RollNodeEntity → ExecutionNode.Builder
    // =========================================================================

    private ExecutionNode.ExecutionNodeBuilder toBuilder(RollNodeEntity entity) {
        ExecutionNode.ExecutionNodeBuilder builder = ExecutionNode.builder()
            .kind(entity.getKind())
            .prompt(entity.getPrompt())
            .resolvedValue(entity.getResolvedValue())
            .innerRole(entity.getInnerRole())
            .chainPassed(Boolean.TRUE.equals(entity.getChainPassed()));

        entity.getDice().forEach(d ->
            builder.die(toDiceEntry(d)));
        entity.getModifiers().forEach(m ->
            builder.modifier(toModifierEntry(m)));
        entity.getChecks().forEach(c ->
            builder.check(toCheckEntry(c)));
        entity.getCrits().forEach(cr ->
            builder.crit(toCritEntry(cr)));

        return builder;
    }

    // =========================================================================
    // Embeddable → Result-Objekte
    // =========================================================================

    private DiceEntry toDiceEntry(DiceEntryEmbeddable e) {
        return e.isIgnored()
            ? DiceEntry.ignored(e.getSides(), e.getValue(), e.getIgnoreReason())
            : DiceEntry.active(e.getSides(), e.getValue());
    }

    private ModifierEntry toModifierEntry(ModifierEntryEmbeddable e) {
        return new ModifierEntry(e.getSignedValue(), e.isAppliedToAll());
    }

    private CheckEntry toCheckEntry(CheckEntryEmbeddable e) {
        return new CheckEntry(e.getKind(), e.getThreshold(), e.getAgainst(), e.isPassed());
    }

    private CritEntry toCritEntry(CritEntryEmbeddable e) {
        return new CritEntry(e.getDiceValue(), e.isCritSuccess());
    }

    // =========================================================================
    // Hilfstyp
    // =========================================================================

    private record DepthAndBuilder(int depth, int treeIndex) {}
}
