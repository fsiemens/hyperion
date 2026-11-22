package de.fabiansiemens.hyperion.core.features.roll.v2;

import de.fabiansiemens.hyperion.core.features.roll.v2.node.*;

/**
 * Visitor-Interface für den Würfelgrammatik-AST.
 *
 * <p>Implementiere dieses Interface, um eigene Auswertungslogik bereitzustellen –
 * z. B. einen Evaluator, Pretty-Printer oder Simulator.
 *
 * <p>Knoten, die ausschließlich innerhalb eines {@link node.DiceRollNode} aufgelöst
 * werden ({@link ModifierNode}, {@link AdvantageNode}, {@link RerollNode},
 * {@link CapNode}, {@link CheckNode}), erhalten eine Default-Implementierung,
 * die eine {@link UnsupportedOperationException} wirft. Evaluatoren, die diese
 * Knoten direkt traversieren möchten, überschreiben die Methoden explizit.
 *
 * @param <T> Rückgabetyp der Visitor-Methoden
 */
public interface DiceNodeVisitor<T> {

    T visitNumber(NumberNode node);
    T visitDiceRoll(DiceRollNode node);
    T visitGrouping(GroupingNode node);
    T visitAddition(AdditionNode node);
    T visitCounted(CountedNode node);
    T visitChain(ChainNode node);

    // Knoten, die der Standard-Evaluator intern in visitDiceRoll auflöst.
    // Werden sie doch direkt besucht, schlägt die Default-Impl. laut fehl.
    default T visitModifier(ModifierNode node)   { throw new UnsupportedOperationException("visitModifier"); }
    default T visitAdvantage(AdvantageNode node) { throw new UnsupportedOperationException("visitAdvantage"); }
    default T visitReroll(RerollNode node)       { throw new UnsupportedOperationException("visitReroll"); }
    default T visitCap(CapNode node)             { throw new UnsupportedOperationException("visitCap"); }
    default T visitCheck(CheckNode node)         { throw new UnsupportedOperationException("visitCheck"); }
}
