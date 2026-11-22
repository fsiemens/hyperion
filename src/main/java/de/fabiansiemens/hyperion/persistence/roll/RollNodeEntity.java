package de.fabiansiemens.hyperion.persistence.roll;

import java.util.ArrayList;
import java.util.List;

import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.execution.InnerRole;
import de.fabiansiemens.hyperion.core.features.roll.v2.execution.NodeKind;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.FlatNode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ein Knoten des Ausführungsbaums, persistiert als flache Tabellenzeile.
 *
 * <p>Die Baumstruktur wird über {@code treeIndex} (Pre-Order-Position) und
 * {@code depth} (Tiefe im Baum) kodiert. Der Wurzelknoten hat {@code treeIndex=0}
 * und {@code depth=0}. Damit kann der Baum ohne rekursive Queries vollständig
 * rekonstruiert werden.
 *
 * <p>Jede Art von Knoten ({@link NodeKind}) wird in derselben Tabelle gespeichert
 * (Single-Table-Strategie). Felder, die nur für bestimmte Knotenarten relevant sind,
 * sind nullable:
 * <ul>
 *   <li>{@code innerRole}   – nur für {@link NodeKind#INNER}</li>
 *   <li>{@code chainPassed} – nur für {@link NodeKind#CHAIN}</li>
 * </ul>
 *
 * <p>Einzelwürfe, Modifikatoren, Checks und Crits sind in eigenen
 * {@code @ElementCollection}-Tabellen gespeichert, da sie geordnete Listen sind
 * und für spätere Statistikabfragen einzeln zugänglich sein müssen.
 */
@Entity
@Table(name = "roll_node", indexes = {
    @Index(name = "idx_roll_node_session",    columnList = "session_id"),
    @Index(name = "idx_roll_node_tree_index", columnList = "session_id, tree_index"),
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RollNodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private RollSessionEntity session;

    // =========================================================================
    // Baumstruktur
    // =========================================================================

    /** Pre-Order-Index im Ausführungsbaum (0 = Wurzel). Entspricht dem Index aus {@code flatten()}. */
    @Column(name = "tree_index", nullable = false)
    private int treeIndex;

    /** Tiefe im Baum (0 = Wurzel, 1 = direkte Kinder, …). */
    @Column(name = "depth", nullable = false)
    private int depth;

    // =========================================================================
    // Knotendaten
    // =========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 16)
    private NodeKind kind;

    /** Würfelausdruck dieses Knotens, z. B. {@code "1d20 +4 check15"}. */
    @Column(name = "prompt", nullable = false)
    private String prompt;

    /** Aufgelöstes Gesamtergebnis dieses Knotens. */
    @Column(name = "resolved_value", nullable = false)
    private int resolvedValue;

    /** Rolle als Inner Roll – nur gesetzt wenn {@code kind == INNER}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "inner_role", length = 20)
    private InnerRole innerRole;

    /** Ob die Chain-Bedingung bestanden wurde – nur relevant wenn {@code kind == CHAIN}. */
    @Column(name = "chain_passed")
    private Boolean chainPassed;

    // =========================================================================
    // Einzelwürfe
    // =========================================================================

    /**
     * Alle Einzelwürfe dieses Knotens in Würfelreihenfolge.
     * Verworfene Würfe (Advantage, Reroll, Cap) sind inkludiert und über
     * {@code ignored} / {@code ignoreReason} gekennzeichnet.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "roll_dice_entry",
        joinColumns = @JoinColumn(name = "node_id"),
        indexes = @Index(name = "idx_dice_entry_node", columnList = "node_id"))
    @OrderColumn(name = "entry_order")
    private List<DiceEntryEmbeddable> dice = new ArrayList<>();

    // =========================================================================
    // Modifikatoren
    // =========================================================================

    /**
     * Modifikatoren in Ausdrucks-Reihenfolge – nicht vorab summiert.
     * Erlaubt spätere Auswertung welche Modifier am häufigsten genutzt werden.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "roll_modifier_entry",
        joinColumns = @JoinColumn(name = "node_id"),
        indexes = @Index(name = "idx_modifier_entry_node", columnList = "node_id"))
    @OrderColumn(name = "entry_order")
    private List<ModifierEntryEmbeddable> modifiers = new ArrayList<>();

    // =========================================================================
    // Checks
    // =========================================================================

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "roll_check_entry",
        joinColumns = @JoinColumn(name = "node_id"),
        indexes = @Index(name = "idx_check_entry_node", columnList = "node_id"))
    @OrderColumn(name = "entry_order")
    private List<CheckEntryEmbeddable> checks = new ArrayList<>();

    // =========================================================================
    // Crits
    // =========================================================================

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "roll_crit_entry",
        joinColumns = @JoinColumn(name = "node_id"),
        indexes = @Index(name = "idx_crit_entry_node", columnList = "node_id"))
    @OrderColumn(name = "entry_order")
    private List<CritEntryEmbeddable> crits = new ArrayList<>();

    // =========================================================================
    // Factory
    // =========================================================================

    static RollNodeEntity of(RollSessionEntity session, FlatNode flat) {
        RollNodeEntity entity = new RollNodeEntity();
        entity.session      = session;
        entity.treeIndex    = flat.index();
        entity.depth        = flat.depth();

        ExecutionNode node  = flat.node();
        entity.kind         = node.getKind();
        entity.prompt       = node.getPrompt();
        entity.resolvedValue = node.getResolvedValue();
        entity.innerRole    = node.getInnerRole();
        entity.chainPassed  = node.getKind().isChain() ? node.isChainPassed() : null;

        node.getDice().forEach(d ->
            entity.dice.add(DiceEntryEmbeddable.of(d)));
        node.getModifiers().forEach(m ->
            entity.modifiers.add(ModifierEntryEmbeddable.of(m)));
        node.getChecks().forEach(c ->
            entity.checks.add(CheckEntryEmbeddable.of(c)));
        node.getCrits().forEach(cr ->
            entity.crits.add(CritEntryEmbeddable.of(cr)));

        return entity;
    }

    // =========================================================================
    // Typ-Prüfung (analog zu ExecutionNode)
    // =========================================================================

    public boolean isRoll()     { return kind.isRoll(); }
    public boolean isInner()    { return kind.isInner(); }
    public boolean isChain()    { return kind.isChain(); }
    public boolean isRoot()     { return kind.isRoot(); }
    public boolean isConstant() { return kind.isConstant(); }
}
