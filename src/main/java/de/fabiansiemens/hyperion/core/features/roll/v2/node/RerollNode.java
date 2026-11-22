package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/**
 * Reroll-Bedingung: {@code rr[op][threshold]}.
 *
 * <p>{@code threshold} kann ein Inner Roll sein. Stimmt ein Würfelergebnis
 * mit der Bedingung überein, wird einmal neu gewürfelt.
 *
 * @param operator  Vergleichsoperator als String: {@code <}, {@code <=},
 *                  {@code ==}, {@code >=}, {@code >}, {@code %}
 * @param threshold Schwellwert-Ausdruck
 */
public record RerollNode(String operator, DiceNode threshold) implements DiceNode {

    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitReroll(this); }

    /** Gibt {@code true} zurück, wenn {@code result} die Reroll-Bedingung erfüllt. */
    public boolean matches(int result, int resolvedThreshold) {
        return switch (operator) {
            case "<"  -> result <  resolvedThreshold;
            case "<=" -> result <= resolvedThreshold;
            case "==" -> result == resolvedThreshold;
            case ">=" -> result >= resolvedThreshold;
            case ">"  -> result >  resolvedThreshold;
            case "%"  -> result %  resolvedThreshold == 0;
            default   -> throw new IllegalStateException("Unbekannter Operator: " + operator);
        };
    }
}
