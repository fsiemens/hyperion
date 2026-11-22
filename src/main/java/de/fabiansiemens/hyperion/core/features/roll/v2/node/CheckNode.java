package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;

/**
 * Prüft ein Würfelergebnis gegen einen Schwellwert.
 *
 * <p>{@code threshold} kann ein Inner Roll sein. Unterstützte Prüfarten:
 * <ul>
 *   <li>{@code check} – Ergebnis ≥ Schwellwert</li>
 *   <li>{@code beat}  – Ergebnis &gt; Schwellwert</li>
 *   <li>{@code nail}  – Ergebnis = Schwellwert</li>
 *   <li>{@code limit} – Ergebnis ≤ Schwellwert</li>
 *   <li>{@code cut}   – Ergebnis &lt; Schwellwert</li>
 * </ul>
 *
 * @param kind      Prüfart als Kleinbuchstaben-String
 * @param threshold Schwellwert-Ausdruck
 */
public record CheckNode(String kind, DiceNode threshold) implements DiceNode {

    @Override public <T> T accept(DiceNodeVisitor<T> v) { return v.visitCheck(this); }

    /** Wertet die Prüfbedingung für ein konkretes Ergebnis aus. */
    public boolean evaluate(int result, int resolvedThreshold) {
        return switch (kind) {
            case "check" -> result >= resolvedThreshold;
            case "beat"  -> result >  resolvedThreshold;
            case "nail"  -> result == resolvedThreshold;
            case "limit" -> result <= resolvedThreshold;
            case "cut"   -> result <  resolvedThreshold;
            default      -> throw new IllegalStateException("Unbekannte Prüfart: " + kind);
        };
    }
}
