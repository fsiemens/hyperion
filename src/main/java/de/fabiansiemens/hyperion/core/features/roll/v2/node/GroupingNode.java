package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/** Geklammerte Unteroperation: {@code ( inner )}. */
public record GroupingNode(DiceNode inner) implements DiceNode {
    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitGrouping(this); }
}
