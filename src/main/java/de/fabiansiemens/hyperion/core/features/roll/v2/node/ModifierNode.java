package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/**
 * Additiver Modifikator auf einen Würfelwurf.
 *
 * <p>{@code sign} ist +1 oder -1. {@code value} kann ein Inner Roll sein.
 * <ul>
 *   <li>{@code allScope = true}  → {@code ++}/{@code --}: wird auf jeden Einzelwurf addiert</li>
 *   <li>{@code allScope = false} → {@code +}/{@code -}:  wird einmalig auf die Gesamtsumme addiert</li>
 * </ul>
 */
public record ModifierNode(int sign, DiceNode value, boolean allScope) implements DiceNode {
    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitModifier(this); }
}
