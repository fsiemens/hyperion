package de.fabiansiemens.hyperion.core.features.roll.v2;

/** Wird geworfen, wenn der Eingabe-String nicht der Würfel-Grammatik entspricht. */
public class DiceParseException extends RuntimeException {

    private static final long serialVersionUID = 1283729057121668613L;

    public DiceParseException(String message) {
        super(message);
    }
}
