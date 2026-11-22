package de.fabiansiemens.hyperion.core.features.roll.v2.execution;

/**
 * Art eines {@link ExecutionNode} im Ausführungsbaum.
 *
 * <p>Beim Traversieren des Baums kann der Typ eines Knotens über die
 * {@code is*()}-Hilfsmethoden geprüft werden, ohne {@code switch} auf dem Enum
 * zu benötigen:
 *
 * <pre>{@code
 * for (ExecutionNode.FlatNode flat : root.flatten()) {
 *     if (flat.node().getKind().isRoll()) { ... }
 *     if (flat.node().getKind().isInner()) { ... }
 * }
 * }</pre>
 */
public enum NodeKind {

    /**
     * Wurzel einer zusammengesetzten Operation (Addition mehrerer Teilwürfe
     * oder Wiederholungskapselung {@code n(expr)}).
     */
    ROOT,

    /**
     * Direkter Würfelwurf: {@code [n]d[s] [argumente…]}.
     * Enthält die eigenen Würfelergebnisse, Modifikatoren und Checks.
     */
    ROLL,

    /**
     * Innerer Würfelwurf, dessen Ergebnis als numerischer Parameter einer
     * äußeren Operation diente (Anzahl, Seiten, Modifier, Schwellwert).
     * Die {@link InnerRole} gibt an, welche Rolle er spielte.
     */
    INNER,

    /**
     * Verketteter Folgewurf nach {@code ?} / {@code :}.
     * Enthält die Daten des Bedingungswurfs (dice, checks) sowie den
     * ausgeführten Zweig als Kindknoten.
     */
    CHAIN,

    /** Konstante Zahl ohne Würfelwurf. */
    CONSTANT;

    // =========================================================================
    // Typ-Prüfhilfen für die Traversierung
    // =========================================================================

    /** {@code true} für {@link #ROLL} – ein direkt gewürfelter Ausdruck. */
    public boolean isRoll()     { return this == ROLL; }

    /**
     * {@code true} für {@link #INNER} – ein Inner Roll, der als Parameter
     * einer äußeren Operation diente.
     */
    public boolean isInner()    { return this == INNER; }

    /**
     * {@code true} für {@link #ROLL} oder {@link #INNER} – jeder Knoten,
     * der tatsächliche Würfelergebnisse enthält.
     */
    public boolean isAnyRoll()  { return this == ROLL || this == INNER; }

    /** {@code true} für {@link #CHAIN}. */
    public boolean isChain()    { return this == CHAIN; }

    /**
     * {@code true} für {@link #ROOT} – Wurzel einer Addition oder
     * Wiederholungskapselung ohne eigene Würfe.
     */
    public boolean isRoot()     { return this == ROOT; }

    /** {@code true} für {@link #CONSTANT}. */
    public boolean isConstant() { return this == CONSTANT; }
}
