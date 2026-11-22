package de.fabiansiemens.hyperion.persistence.roll;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Persistierte Würfelsession – eine vollständige Auswertung eines Würfelausdrucks.
 *
 * <p>Eine Session entspricht einem einzigen Aufruf von {@code !roll <prompt>} durch
 * einen Spieler. Sie enthält den Eingabe-String, das Gesamtergebnis und alle
 * Knoten des Ausführungsbaums als flache Liste ({@link RollNodeEntity}).
 *
 * <p>Beziehung zum Laufzeitsystem:
 * <pre>
 *   RollEvaluator.evaluate(prompt) → ExecutionNode (root)
 *   root.flatten()                 → List&lt;FlatNode&gt;
 *   RollSessionEntity.from(root)   → RollSessionEntity (persistierbar)
 * </pre>
 */
@Entity
@Table(name = "roll_session", indexes = {
    @Index(name = "idx_roll_session_user",      columnList = "discord_user_id"),
    @Index(name = "idx_roll_session_guild",     columnList = "discord_guild_id"),
    @Index(name = "idx_roll_session_timestamp", columnList = "rolled_at"),
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "nodes")
public class RollSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "message_id", nullable = false)
    private Long messageId;

    /** Discord-User-ID des würfelnden Spielers (Fremdschlüssel zu zukünftigem Spielerprofil). */
    @Column(name = "discord_user_id", nullable = false)
    private Long discordUserId;

    /** Discord-Guild-ID – für spätere Guild-spezifische Auswertungen. */
    @Column(name = "discord_guild_id", nullable = false)
    private Long discordGuildId;

    /** Originaler Eingabe-String des Spielers, z. B. {@code "d20 +4 check15 ? 3d8 : 1d4"}. */
    @Column(name = "prompt", nullable = false)
    private String prompt;

    /** Gesamtergebnis der Würfeloperation (resolvedValue des Wurzelknotens). */
    @Column(name = "total_result", nullable = false)
    private int totalResult;

    /** Zeitpunkt des Würfelns. */
    @CreationTimestamp
    @Column(name = "rolled_at", nullable = false, updatable = false)
    private Instant rolledAt;

    /**
     * Alle Knoten des Ausführungsbaums als flache Pre-Order-Liste.
     * Reihenfolge und Baumstruktur sind über {@code treeIndex} und {@code depth}
     * vollständig rekonstruierbar.
     */
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("tree_index ASC")
    private List<RollNodeEntity> nodes = new ArrayList<>();

    // =========================================================================
    // Factory
    // =========================================================================

    public static RollSessionEntity of(Long userId, Long guildId, Long messageId,
                                       String prompt,
                                       ExecutionNode root) {
        RollSessionEntity session = new RollSessionEntity();
        session.discordUserId = userId;
        session.discordGuildId = guildId;
        session.messageId = messageId;
        session.prompt = prompt;
        session.totalResult = root.getResolvedValue();

        // Direkt aus flatten() – keine weitere Transformation nötig
        root.flatten().forEach(flat ->
            session.nodes.add(RollNodeEntity.of(session, flat)));

        return session;
    }

    // =========================================================================
    // Abgeleitete Abfragen (für einfache Auswertungen ohne Join)
    // =========================================================================

    public RollNodeEntity rootNode() {
        return nodes.stream()
            .filter(n -> n.getTreeIndex() == 0)
            .findFirst()
            .orElseThrow();
    }
}
