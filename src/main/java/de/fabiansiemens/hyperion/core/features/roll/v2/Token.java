package de.fabiansiemens.hyperion.core.features.roll.v2;

/**
 * Ein einzelnes Token mit Typ, optionalem Wert und Position im Eingabe-String.
 *
 * @param type     Token-Typ
 * @param value    Rohwert aus dem Eingabe-String (leer bei Operatoren)
 * @param position Zeichenposition im Eingabe-String (–1 in Tests)
 */
public record Token(TokenType type, String value, int position) {

    /** Hilfskonstruktor ohne Position (für Tests). */
    public Token(TokenType type, String value) {
        this(type, value, -1);
    }

    /** Numerischer Wert – nur für NUMBER-Tokens gültig. */
    public int intValue() {
        return Integer.parseInt(value);
    }

    @Override
    public String toString() {
        return type + (value.isEmpty() ? "" : "(" + value + ")");
    }
}
