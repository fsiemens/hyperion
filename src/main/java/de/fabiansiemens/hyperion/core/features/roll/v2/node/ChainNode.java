package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/**
 * Bedingte Verkettung: {@code condition ? thenBranch [: elseBranch]}.
 *
 * <p>{@code condition} muss einen {@link CheckNode} enthalten. Bei bestandenem
 * Check wird {@code thenBranch} ausgewertet, andernfalls {@code elseBranch}
 * (wenn vorhanden), sonst ist der Gesamtwert 0.
 */
public record ChainNode(DiceNode condition, DiceNode thenBranch, DiceNode elseBranch)
        implements DiceNode {

    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitChain(this); }
}
