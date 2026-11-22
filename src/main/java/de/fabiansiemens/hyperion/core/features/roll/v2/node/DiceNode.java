package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/**
 * Basistyp aller AST-Knoten der Würfel-Grammatik.
 *
 * <p>Jede Implementierung ist ein unveränderliches Record und akzeptiert
 * einen {@link DiceNodeVisitor} zur typsicheren Traversierung.
 *
 * @see NumberNode
 * @see DiceRollNode
 * @see GroupingNode
 * @see CountedNode
 * @see AdditionNode
 * @see ModifierNode
 * @see AdvantageNode
 * @see RerollNode
 * @see CapNode
 * @see CheckNode
 * @see ChainNode
 */
public sealed interface DiceNode
        permits NumberNode, DiceRollNode, GroupingNode, CountedNode,
                AdditionNode, ModifierNode, AdvantageNode, RerollNode,
                CapNode, CheckNode, ChainNode {

    <T> T accept(DiceNodeVisitor<T> visitor);
}
