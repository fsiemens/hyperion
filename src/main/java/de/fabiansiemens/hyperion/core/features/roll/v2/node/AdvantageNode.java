package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/**
 * Vor- oder Nachteil beim Würfeln: zwei Würfe, höchster ({@code adv})
 * bzw. niedrigster ({@code disadv}) zählt.
 *
 * @param advantage {@code true} = Vorteil, {@code false} = Nachteil
 */
public record AdvantageNode(boolean advantage) implements DiceNode {
    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitAdvantage(this); }
}
