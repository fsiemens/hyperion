package de.fabiansiemens.hyperion.core.features.roll.v2;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexer für die Würfel-Grammatik.
 *
 * <p>Leerzeichen werden überall ignoriert. Schlüsselwörter werden per Longest-Match
 * erkannt, sodass zusammengeschriebene Kombinationen wie {@code disadvall} oder
 * {@code advrr} korrekt als separate Tokens gelesen werden.
 *
 * <p>{@code ,} trennt eigenständige Würfeloperationen voneinander
 * (z. B. {@code d20 +4 check15 ? 3d8, 2d6 +1}) und ist der einzige
 * Verknüpfungsoperator auf oberster Ebene. {@code +} und {@code -} sind
 * ausschließlich Modifier-Argumente eines Würfelausdrucks.
 */
public class DiceLexer {

    private record Keyword(String text, TokenType type) {}

    private static final Keyword[] KEYWORDS = {
        new Keyword("disadv", TokenType.DISADV),
        new Keyword("check",  TokenType.CHECK),
        new Keyword("limit",  TokenType.LIMIT),
        new Keyword("beat",   TokenType.BEAT),
        new Keyword("nail",   TokenType.NAIL),
        new Keyword("adv",    TokenType.ADV),
        new Keyword("rr",     TokenType.RR),
        new Keyword("min",    TokenType.CAP_MIN),
        new Keyword("max",    TokenType.CAP_MAX),
        new Keyword("cut",    TokenType.CUT),
        new Keyword("all",    TokenType.ALL),
        new Keyword("d",      TokenType.D),
    };

    private final String input;
    private int pos;

    public DiceLexer(String input) {
        this.input = input.replaceAll("\\s+", "");
        this.pos   = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            tokens.add(nextToken());
        }
        tokens.add(new Token(TokenType.EOF, "", pos));
        return tokens;
    }

    // -------------------------------------------------------------------------

    private Token nextToken() {
        int start = pos;
        char c    = input.charAt(pos);

        if (Character.isDigit(c))  return readNumber(start);
        if (Character.isLetter(c)) return readKeyword(start);

        return switch (c) {
            case '+' -> {
                if (peek(1) == '+') { pos += 2; yield token(TokenType.PLUS_PLUS,   "++", start); }
                pos++; yield token(TokenType.PLUS, "+", start);
            }
            case '-' -> {
                if (peek(1) == '-') { pos += 2; yield token(TokenType.MINUS_MINUS, "--", start); }
                pos++; yield token(TokenType.MINUS, "-", start);
            }
            case '<' -> {
                if (peek(1) == '=') { pos += 2; yield token(TokenType.CMP_LTE, "<=", start); }
                pos++; yield token(TokenType.CMP_LT, "<", start);
            }
            case '>' -> {
                if (peek(1) == '=') { pos += 2; yield token(TokenType.CMP_GTE, ">=", start); }
                pos++; yield token(TokenType.CMP_GT, ">", start);
            }
            case '=' -> {
                if (peek(1) == '=') pos++;
                pos++; yield token(TokenType.CMP_EQ, "==", start);
            }
            case '%' -> { pos++; yield token(TokenType.CMP_MOD, "%",  start); }
            case '?' -> { pos++; yield token(TokenType.QUESTION, "?", start); }
            case ':' -> { pos++; yield token(TokenType.COLON,    ":", start); }
            case '(' -> { pos++; yield token(TokenType.LPAREN,   "(", start); }
            case ')' -> { pos++; yield token(TokenType.RPAREN,   ")", start); }
            case ',' -> { pos++; yield token(TokenType.COMMA,    ",", start); }
            default  -> throw new DiceParseException(
                "Unbekanntes Zeichen '" + c + "' an Position " + pos);
        };
    }

    private Token readNumber(int start) {
        int begin = pos;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
        return token(TokenType.NUMBER, input.substring(begin, pos), start);
    }

    private Token readKeyword(int start) {
        String rest = input.substring(pos).toLowerCase();

        for (Keyword kw : KEYWORDS) {
            if (!rest.startsWith(kw.text())) continue;

            int     afterPos    = pos + kw.text().length();
            boolean nextIsLetter = afterPos < input.length()
                && Character.isLetter(input.charAt(afterPos));

            if (kw.type() == TokenType.D && nextIsLetter) continue;

            pos += kw.text().length();
            return token(kw.type(), kw.text(), start);
        }

        int begin = pos;
        while (pos < input.length() && Character.isLetter(input.charAt(pos))) pos++;
        throw new DiceParseException(
            "Unbekanntes Schlüsselwort '" + input.substring(begin, pos) + "' an Position " + start);
    }

    // -------------------------------------------------------------------------

    private char peek(int offset) {
        int p = pos + offset;
        return p < input.length() ? input.charAt(p) : 0;
    }

    private Token token(TokenType type, String value, int position) {
        return new Token(type, value, position);
    }
}