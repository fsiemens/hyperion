package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/**
 * {@code n × (komplexer Ausdruck)} – entsteht bei z. B. {@code 4(d20 check10 ? (d4)d8)}.
 *
 * <p>Der Evaluator führt {@code inner} genau {@code count}-mal vollständig aus
 * und sammelt alle Einzelergebnisse separat.
 */
public record CountedNode(int count, DiceNode inner) implements DiceNode {
    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitCounted(this); }
}
