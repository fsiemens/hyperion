package de.fabiansiemens.hyperion.core.features.roll.v2.result;

import lombok.Value;

/** Ergebnis einer einzelnen Prüfoperation. */
@Value
public class CheckEntry {

    String  kind;
    int     threshold;
    int     against;
    boolean passed;

    /** Lesbare Bedingungsdarstellung, z. B. {@code ≥16}, {@code >10}. */
    public String conditionLabel() {
        return switch (kind) {
            case "check" -> "≥" + threshold;
            case "beat"  -> ">" + threshold;
            case "nail"  -> "=" + threshold;
            case "limit" -> "≤" + threshold;
            case "cut"   -> "<" + threshold;
            default      -> kind + threshold;
        };
    }
}
