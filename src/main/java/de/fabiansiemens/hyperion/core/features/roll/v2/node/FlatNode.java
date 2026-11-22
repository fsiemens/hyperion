package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.execution.NodeKind;

/**
 * Ein Eintrag in der flachen Traversierungsliste.
 *
 * @param node  Der Knoten
 * @param depth Tiefe im Baum (0 = Wurzel)
 * @param index 0-basierter Index in der Flat-Liste (Pre-Order)
 */
public record FlatNode(ExecutionNode node, int depth, int index) {

    /** Kurzform für {@code node().getKind()}. */
    public NodeKind kind() { return node.getKind(); }
}
