package de.fabiansiemens.hyperion.core.features.roll.v2;

import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.DiceNode;

import java.util.List;

/**
 * Haupteinstiegspunkt des Würfelinterpreters.
 *
 * <p>Zwei Nutzungswege:
 * <ol>
 *   <li><b>Nur parsen</b> – {@link #parse(String)} für eigene Visitor-Implementierungen.</li>
 *   <li><b>Parsen + Auswerten</b> – {@link #evaluate(String)} für den vollständigen
 *       {@link ExecutionNode}-Baum.</li>
 * </ol>
 *
 * <pre>{@code
 * DiceInterpreter interpreter = new DiceInterpreter();
 *
 * ExecutionNode root = interpreter.evaluate("4d20 adv rr<=2 check16");
 * System.out.println(root.toTree());
 *
 * // Detail-Auswertung: durch alle Knoten navigieren
 * for (ExecutionNode.FlatNode flat : root.flatten()) {
 *     ExecutionNode node = flat.node();
 *     if (node.isRoll()) {
 *         System.out.println(node.getPrompt() + " → " + node.getResolvedValue());
 *         System.out.println("  Würfe:   " + node.getDice());
 *         System.out.println("  Mods:    " + node.getModifiers());
 *         System.out.println("  Checks:  " + node.getChecks());
 *     }
 *     if (node.isInner()) {
 *         System.out.println("Inner Roll (" + node.getInnerRole() + "): " + node.getPrompt());
 *     }
 * }
 * }</pre>
 */
public class DiceInterpreter {

    private final RollEvaluator evaluator;

    /** Erstellt einen Interpreter mit Standard-Konfiguration. */
    public DiceInterpreter() {
        this.evaluator = new RollEvaluator();
    }

    /** Erstellt einen Interpreter mit individuellem Evaluator (z. B. für Tests oder Profile). */
    public DiceInterpreter(RollEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    // -------------------------------------------------------------------------

    /**
     * Parst den Ausdruck und gibt den AST-Wurzelknoten zurück.
     *
     * @throws DiceParseException bei ungültiger Syntax
     */
    public DiceNode parse(String expression) {
        List<Token> tokens = new DiceLexer(expression).tokenize();
        return new DiceParser(tokens).parse();
    }

    /**
     * Parst und wertet den Ausdruck aus.
     *
     * @return Wurzelknoten des Ausführungsbaums
     * @throws DiceParseException       bei ungültiger Syntax oder Grenzwert-Verletzung
     * @throws IllegalArgumentException bei ungültigen Laufzeitwerten
     */
    public ExecutionNode evaluate(String expression) {
        return evaluator.evaluate(expression);
    }
}
