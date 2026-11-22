package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/** Ganzzahliges Literal: {@code 4}, {@code 20}, … */
public record NumberNode(int value) implements DiceNode {
    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitNumber(this); }
}
