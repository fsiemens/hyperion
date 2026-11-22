package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/**
 * Begrenzt Würfelergebnisse nach unten ({@code min}) oder oben ({@code max}).
 *
 * <p>{@code threshold} kann ein Inner Roll sein.
 *
 * @param minimum   {@code true} = untere Schranke (min), {@code false} = obere Schranke (max)
 * @param threshold Schwellwert-Ausdruck
 */
public record CapNode(boolean minimum, DiceNode threshold) implements DiceNode {
    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitCap(this); }
}
