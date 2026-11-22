package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;
import java.util.List;

/**
 * Summe mehrerer Teilausdrücke mit Vorzeichen:
 * {@code operands[i]} wird mit {@code signs[i]} (+1 oder -1) multipliziert.
 */
public record AdditionNode(List<DiceNode> operands, List<Integer> signs) implements DiceNode {
    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitAddition(this); }
}
