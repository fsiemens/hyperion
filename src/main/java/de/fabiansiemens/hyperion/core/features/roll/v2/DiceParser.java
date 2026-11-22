package de.fabiansiemens.hyperion.core.features.roll.v2;

import java.util.ArrayList;
import java.util.List;

import de.fabiansiemens.hyperion.core.features.roll.v2.node.AdditionNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.AdvantageNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.CapNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.ChainNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.CheckNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.CountedNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.DiceNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.DiceRollNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.GroupingNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.ModifierNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.NumberNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.RerollNode;

/**
 * Recursive-Descent-Parser für die Würfelgrammatik.
 *
 * <p>Grammatik-Übersicht:
 * <pre>
 *   expression      → chain (',' chain)*
 *   chain           → diceExpr ('?' diceExpr (':' diceExpr)?)?
 *   diceExpr        → [numericExpr] 'd' numericExpr argument*
 *   numericExpr     → NUMBER | '(' expression ')'
 *   argument        → perRollModifier | modifier | advantage | reroll | cap | check
 *   perRollModifier → ('++' | '--') numericExpr
 *                   | ('+' | '-') numericExpr 'all'
 *   modifier        → ('+' | '-') numericExpr        (kein 'all' dahinter)
 *   advantage       → ('adv' | 'disadv') ['all']
 *   reroll          → 'rr' cmpOp numericExpr ['all']
 *   cap             → ('min' | 'max') numericExpr ['all']
 *   check           → ('check'|'beat'|'nail'|'limit'|'cut') numericExpr ['all']
 *   cmpOp           → '<' | '<=' | '==' | '=' | '>=' | '>' | '%'
 * </pre>
 *
 * <p><strong>Designentscheidung – kein {@code +} als Additions-Operator:</strong><br>
 * {@code +} und {@code -} sind ausschließlich Modifier-Argumente eines Würfelausdrucks
 * ({@code d20 +4 -1}). Eigenständige Würfeloperationen werden mit {@code ,} verknüpft
 * ({@code d20 +4 check15 ? 3d8, 2d6}). Das eliminiert die strukturelle Ambiguität
 * zwischen Modifier und Addition.
 */
public class DiceParser {

    private final List<Token> tokens;
    private final DiceBounds  bounds;
    private int pos;

    public DiceParser(List<Token> tokens) {
        this(tokens, DiceBounds.DEFAULT);
    }

    public DiceParser(List<Token> tokens, DiceBounds bounds) {
        this.tokens = tokens;
        this.bounds = bounds;
        this.pos    = 0;
    }

    public DiceNode parse() {
        DiceNode node = parseExpression();
        expect(TokenType.EOF);
        return node;
    }

    // =========================================================================
    // Grammatik-Ebenen
    // =========================================================================

    /**
     * Oberste Ebene: eine oder mehrere durch {@code ,} verknüpfte Würfeloperationen.
     *
     * <p>{@code d20 +4 check15 ? 3d8, 2d6 +1} → {@code AdditionNode([chain1, chain2])}
     */
    private DiceNode parseExpression() {
        List<DiceNode> operands = new ArrayList<>();
        List<Integer>  signs    = new ArrayList<>();

        operands.add(parseChain());
        signs.add(+1);

        while (at(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            operands.add(parseChain());
            signs.add(+1);
        }

        if (operands.size() == 1) return operands.get(0);
        return new AdditionNode(operands, signs);
    }

    /**
     * Chain-Ebene: optionale bedingte Verkettung mit {@code ?} / {@code :}.
     */
    private DiceNode parseChain() {
        DiceNode condition = parseAtomExpr();
        if (!at(TokenType.QUESTION)) return condition;

        validateChainCondition(condition);
        consume(TokenType.QUESTION);

        DiceNode thenBranch = parseAtomExpr();
        DiceNode elseBranch = null;
        if (at(TokenType.COLON)) {
            consume(TokenType.COLON);
            elseBranch = parseAtomExpr();
        }
        return new ChainNode(condition, thenBranch, elseBranch);
    }

    /**
     * Atom-Ebene: ein einzelner Würfelausdruck, Gruppierung, Wiederholung oder Literal.
     *
     * <p>Kein {@code +}/{@code -} als Additions-Operator auf dieser Ebene –
     * Modifier werden ausschließlich innerhalb von {@link #parseDiceExpr} konsumiert.
     */
    private DiceNode parseAtomExpr() {
        // NUMBER '(' → n×(expr)
        if (at(TokenType.NUMBER) && peekType(1) == TokenType.LPAREN) {
            int n = consume(TokenType.NUMBER).intValue();
            bounds.validateCount(n, current().position());
            consume(TokenType.LPAREN);
            DiceNode inner = parseExpression();
            expect(TokenType.RPAREN);
            return buildRepetition(n, inner);
        }

        // NUMBER 'd' → Würfelwurf mit numerischer Anzahl
        if (at(TokenType.NUMBER) && peekType(1) == TokenType.D) {
            int n = consume(TokenType.NUMBER).intValue();
            bounds.validateCount(n, current().position());
            return parseDiceExpr(new NumberNode(n));
        }

        // '(' → Gruppierung oder (expr)d<sides>
        if (at(TokenType.LPAREN)) {
            consume(TokenType.LPAREN);
            DiceNode inner = parseExpression();
            expect(TokenType.RPAREN);
            return at(TokenType.D)
                ? parseDiceExpr(new GroupingNode(inner))
                : new GroupingNode(inner);
        }

        // 'd' → Würfelwurf ohne explizite Anzahl (implizit 1)
        if (at(TokenType.D)) {
            return parseDiceExpr(null);
        }

        // Reines Zahlliteral (z. B. als Operand in einem Inner Roll)
        if (at(TokenType.NUMBER)) {
            return new NumberNode(consume(TokenType.NUMBER).intValue());
        }

        throw new DiceParseException(
            "Unerwartetes Token " + current() + " an Position " + current().position());
    }

    // =========================================================================
    // numericExpr
    // =========================================================================

    /** Liest eine Zahl oder einen geklammerten Ausdruck als numerischen Wert. */
    private DiceNode parseNumericExpr() {
        if (at(TokenType.LPAREN)) {
            consume(TokenType.LPAREN);
            DiceNode inner = parseExpression();
            expect(TokenType.RPAREN);
            return new GroupingNode(inner);
        }
        return new NumberNode(consume(TokenType.NUMBER).intValue());
    }

    // =========================================================================
    // Würfelausdruck mit allen Argumenten
    // =========================================================================

    private DiceNode parseDiceExpr(DiceNode count) {
        expect(TokenType.D);

        DiceNode sidesNode = parseNumericExpr();
        if (sidesNode instanceof NumberNode n) bounds.validateSides(n.value(), current().position());

        List<ModifierNode> modifiers = new ArrayList<>();
        AdvantageNode      advantage   = null;
        boolean            advAll      = false;
        RerollNode         reroll      = null;
        boolean            rerollAll   = false;
        CapNode            cap         = null;
        boolean            capAll      = false;
        CheckNode          check       = null;
        boolean            checkAll    = false;

        argLoop:
        while (true) {

            // ++ / -- → immer per-roll
            if (at(TokenType.PLUS_PLUS)) {
                consume(TokenType.PLUS_PLUS);
                modifiers.add(new ModifierNode(+1, parseNumericExpr(), true));
                continue;
            }
            if (at(TokenType.MINUS_MINUS)) {
                consume(TokenType.MINUS_MINUS);
                modifiers.add(new ModifierNode(-1, parseNumericExpr(), true));
                continue;
            }

            // + / - → per-roll wenn 'all' folgt, einmalig sonst.
            // Kein Konflikt mehr mit Additions-Ebene, da '+'/'-' nie als
            // Additions-Operator verwendet wird.
            if ((at(TokenType.PLUS) || at(TokenType.MINUS)) && isNumericExprNext()) {
                int sign = at(TokenType.PLUS) ? +1 : -1;
                advance();
                DiceNode val = parseNumericExpr();
                boolean allScope = consumeAllScope();
                modifiers.add(new ModifierNode(sign, val, allScope));
                continue;
            }

            if (at(TokenType.ADV) || at(TokenType.DISADV)) {
                if (advantage != null) throw new DiceParseException(
                    "Ungültige Würfeloperation: 'adv' und 'disadv' schließen sich aus "
                    + "und dürfen nur einmal vorkommen.");
                boolean isAdv = at(TokenType.ADV);
                advance();
                advantage = new AdvantageNode(isAdv);
                advAll    = consumeAllScope();
                continue;
            }

            if (at(TokenType.RR)) {
                consume(TokenType.RR);
                String   op        = parseCmpOperator();
                DiceNode threshold = parseNumericExpr();
                reroll    = new RerollNode(op, threshold);
                rerollAll = consumeAllScope();
                continue;
            }

            if (at(TokenType.CAP_MIN) || at(TokenType.CAP_MAX)) {
                boolean isMin = at(TokenType.CAP_MIN);
                advance();
                cap    = new CapNode(isMin, parseNumericExpr());
                capAll = consumeAllScope();
                continue;
            }

            if (isCheckKeyword()) {
                if (check != null) throw new DiceParseException(
                    "Ungültige Würfeloperation: Es darf nur ein Prüf-Argument "
                    + "(check/beat/nail/limit/cut) pro Würfeloperation geben.");
                String kind = current().value().toLowerCase();
                advance();
                check    = new CheckNode(kind, parseNumericExpr());
                checkAll = consumeAllScope();
                continue;
            }

            break argLoop;
        }

        return new DiceRollNode(count, sidesNode, modifiers,
            advantage, advAll,
            reroll,    rerollAll,
            cap,       capAll,
            check,     checkAll);
    }

    // =========================================================================
    // Wiederholungskapselung
    // =========================================================================

    private DiceNode buildRepetition(int n, DiceNode inner) {
        if (inner instanceof DiceRollNode roll && roll.count() == null) {
            List<ModifierNode> scopedMods = roll.modifiers().stream()
                .map(m -> new ModifierNode(m.sign(), m.value(), true))
                .toList();
            return new DiceRollNode(
                new NumberNode(n), roll.sides(), scopedMods,
                roll.advantage(),  roll.advantage() != null,
                roll.reroll(),     roll.reroll()    != null,
                roll.cap(),        roll.cap()       != null,
                roll.check(),      roll.check()     != null);
        }
        return new CountedNode(n, inner);
    }

    // =========================================================================
    // Hilfsmethoden
    // =========================================================================

    private boolean consumeAllScope() {
        if (at(TokenType.ALL)) { advance(); return true; }
        return false;
    }

    private String parseCmpOperator() {
        if (at(TokenType.CMP_LTE)) { consume(TokenType.CMP_LTE); return "<="; }
        if (at(TokenType.CMP_LT))  { consume(TokenType.CMP_LT);  return "<";  }
        if (at(TokenType.CMP_EQ))  { consume(TokenType.CMP_EQ);  return "=="; }
        if (at(TokenType.CMP_GTE)) { consume(TokenType.CMP_GTE); return ">="; }
        if (at(TokenType.CMP_GT))  { consume(TokenType.CMP_GT);  return ">";  }
        if (at(TokenType.CMP_MOD)) { consume(TokenType.CMP_MOD); return "%";  }
        throw new DiceParseException(
            "Vergleichsoperator erwartet, gefunden: " + current()
            + " an Position " + current().position());
    }

    private boolean isCheckKeyword() {
        TokenType t = current().type();
        return t == TokenType.CHECK || t == TokenType.BEAT
            || t == TokenType.NAIL  || t == TokenType.LIMIT || t == TokenType.CUT;
    }

    /**
     * Prüft ob das Token **nach** dem aktuellen (also nach dem bereits
     * gelesenen {@code +}/{@code -}) ein numerischer Ausdruck beginnt.
     * Muss mit {@code peekType(1)} arbeiten, weil {@code current()} noch
     * das Operator-Token selbst ist.
     */
    private boolean isNumericExprNext() {
        TokenType next = peekType(1);
        return next == TokenType.NUMBER || next == TokenType.LPAREN;
    }

    private void validateChainCondition(DiceNode node) {
        if (containsCheck(node)) return;
        throw new DiceParseException(
            "Verkettung (?) erfordert ein Prüf-Argument (check/beat/nail/limit/cut).");
    }

    private boolean containsCheck(DiceNode node) {
        if (node instanceof DiceRollNode roll && roll.check() != null) return true;
        if (node instanceof AdditionNode add)
            return add.operands().stream().anyMatch(this::containsCheck);
        return false;
    }

    private TokenType peekType(int offset) {
        int i = pos + offset;
        return i < tokens.size() ? tokens.get(i).type() : TokenType.EOF;
    }

    private boolean at(TokenType type) { return current().type() == type; }
    private boolean isAtEnd()          { return current().type() == TokenType.EOF; }
    private Token   current()          { return tokens.get(pos); }
    private Token   advance()          { return tokens.get(pos++); }

    private Token consume(TokenType type) {
        if (!at(type)) throw new DiceParseException(
            "Erwartet " + type.getLabel() + ", aber gefunden: "
            + current().type().getLabel() + " an Position " + current().position());
        return advance();
    }

    private void expect(TokenType type) { consume(type); }
}