package de.fabiansiemens.hyperion.persistence.roll;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RollSessionRepository extends JpaRepository<RollSessionEntity, Long> {

    // -------------------------------------------------------------------------
    // Grundabfragen
    // -------------------------------------------------------------------------

	Optional<RollSessionEntity> findByMessageId(Long messageId);
	
    /** Alle Sessions eines Spielers, neueste zuerst. */
    Page<RollSessionEntity> findByDiscordUserIdOrderByRolledAtDesc(
        Long discordUserId, Pageable pageable);

    /** Alle Sessions einer Guild, neueste zuerst. */
    Page<RollSessionEntity> findByDiscordGuildIdOrderByRolledAtDesc(
        Long discordGuildId, Pageable pageable);

    /** Sessions eines Spielers in einer bestimmten Guild. */
    Page<RollSessionEntity> findByDiscordUserIdAndDiscordGuildIdOrderByRolledAtDesc(
        Long discordUserId, Long discordGuildId, Pageable pageable);

    // -------------------------------------------------------------------------
    // Zeitraum-Abfragen
    // -------------------------------------------------------------------------

    List<RollSessionEntity> findByDiscordUserIdAndRolledAtBetween(
        Long discordUserId, Instant from, Instant to);

    // -------------------------------------------------------------------------
    // Statistik-Abfragen (auf Ebene der Session)
    // -------------------------------------------------------------------------

    /** Durchschnittliches Gesamtergebnis eines Spielers für einen bestimmten Prompt. */
    @Query("""
        SELECT AVG(s.totalResult)
        FROM RollSessionEntity s
        WHERE s.discordUserId = :userId
          AND s.prompt = :prompt
        """)
    Optional<Double> avgTotalResultByUserAndPrompt(
        @Param("userId") Long userId,
        @Param("prompt") String prompt);

    /** Anzahl Sessions eines Spielers. */
    long countByDiscordUserId(Long discordUserId);

    // -------------------------------------------------------------------------
    // Statistik-Abfragen (auf Ebene der Einzelwürfe – via JOIN auf Nodes/Dice)
    // -------------------------------------------------------------------------

    /**
     * Durchschnittlicher Einzelwurf-Wert eines Spielers für eine bestimmte Würfelgröße.
     * Nützlich um die Zufallsverteilung zu prüfen.
     */
    @Query("""
        SELECT AVG(d.value)
        FROM RollNodeEntity n
        JOIN n.session s
        JOIN n.dice d
        WHERE s.discordUserId = :userId
          AND d.sides = :sides
          AND d.ignored = false
        """)
    Optional<Double> avgDiceValueByUserAndSides(
        @Param("userId") Long userId,
        @Param("sides") int sides);

    /**
     * Häufigkeitsverteilung der Einzelwürfe eines Spielers für eine Würfelgröße.
     * Gibt Paare [wert, anzahl] zurück.
     */
    @Query("""
        SELECT d.value, COUNT(d)
        FROM RollNodeEntity n
        JOIN n.session s
        JOIN n.dice d
        WHERE s.discordUserId = :userId
          AND d.sides = :sides
          AND d.ignored = false
        GROUP BY d.value
        ORDER BY d.value ASC
        """)
    List<Object[]> diceValueDistributionByUserAndSides(
        @Param("userId") Long userId,
        @Param("sides") int sides);

    /**
     * Check-Quote eines Spielers: Anteil bestandener Checks.
     */
    @Query("""
        SELECT
            COUNT(CASE WHEN c.passed = true  THEN 1 END) AS passed,
            COUNT(CASE WHEN c.passed = false THEN 1 END) AS failed
        FROM RollNodeEntity n
        JOIN n.session s
        JOIN n.checks c
        WHERE s.discordUserId = :userId
        """)
    Object[] checkQuoteByUser(@Param("userId") Long userId);
}
