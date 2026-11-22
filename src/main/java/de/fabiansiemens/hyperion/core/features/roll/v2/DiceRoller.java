package de.fabiansiemens.hyperion.core.features.roll.v2;

import de.fabiansiemens.hyperion.core.features.roll.IgnoreReason;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.DiceRollNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.DiceEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Kapselt die eigentliche Würfelmechanik: Advantage/Disadvantage, Reroll und Cap.
 *
 * <p>Ausgelagert aus {@link RollEvaluator}, damit die Mechanik isoliert testbar ist.
 */
class DiceRoller {

    private final Random random;

    DiceRoller(Random random) {
        this.random = random;
    }

    /**
     * Würfelt einen einzelnen Würfel gemäß den Argumenten des übergebenen Knotens.
     *
     * @param sides            Seitenanzahl des Würfels
     * @param node             Würfelknoten mit Advantage-/Reroll-/Cap-Konfiguration
     * @param dieIndex         0-basierter Index dieses Würfels innerhalb der Gruppe
     * @param rerollThreshold  Aufgelöster Reroll-Schwellwert
     * @param capThreshold     Aufgelöster Cap-Schwellwert
     * @return Liste aller erzeugten Einträge (aktiv + verworfen)
     */
    List<DiceEntry> roll(int sides, DiceRollNode node, int dieIndex,
                         int rerollThreshold, int capThreshold) {

        boolean applyAdv = node.advantage() != null && (node.advAllScope() || dieIndex == 0);
        boolean applyRr  = node.reroll()    != null && (node.rerollAllScope() || dieIndex == 0);
        boolean applyCap = node.cap()       != null && (node.capAllScope()   || dieIndex == 0);

        List<DiceEntry> entries = new ArrayList<>();
        int roll = rollDie(sides);

        // --- Advantage / Disadvantage ---
        if (applyAdv) {
            int roll2 = rollDie(sides);
            boolean takeHigher = node.advantage().advantage();
            boolean keepFirst  = takeHigher ? roll >= roll2 : roll <= roll2;

            if (keepFirst) {
                entries.add(DiceEntry.active(sides, roll));
                entries.add(DiceEntry.ignored(sides, roll2, advantageReason(takeHigher)));
            } else {
                entries.add(DiceEntry.ignored(sides, roll, advantageReason(takeHigher)));
                entries.add(DiceEntry.active(sides, roll = roll2));
            }
        } else {
            entries.add(DiceEntry.active(sides, roll));
        }

        // --- Reroll ---
        if (applyRr && node.reroll().matches(roll, rerollThreshold)) {
            DiceEntry original = activeEntry(entries);
            entries.remove(original);
            entries.add(DiceEntry.ignored(sides, original.getValue(), IgnoreReason.REROLL));
            roll = rollDie(sides);
            entries.add(DiceEntry.active(sides, roll));
        }

        // --- Cap ---
        if (applyCap) {
            boolean triggers = node.cap().minimum() ? roll < capThreshold : roll > capThreshold;
            if (triggers) {
                DiceEntry original = activeEntry(entries);
                entries.remove(original);
                IgnoreReason reason = node.cap().minimum() ? IgnoreReason.MINIMUM : IgnoreReason.MAXIMUM;
                entries.add(DiceEntry.ignored(sides, original.getValue(), reason));
                int capped = node.cap().minimum()
                    ? Math.max(roll, capThreshold)
                    : Math.min(roll, capThreshold);
                entries.add(DiceEntry.active(sides, capped));
            }
        }

        return entries;
    }

    // -------------------------------------------------------------------------

    private int rollDie(int sides) {
        return random.nextInt(sides) + 1;
    }

    private IgnoreReason advantageReason(boolean advantage) {
        return advantage ? IgnoreReason.ADVANTAGE : IgnoreReason.DISADVANTAGE;
    }

    private DiceEntry activeEntry(List<DiceEntry> entries) {
        return entries.stream()
            .filter(e -> !e.isIgnored())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Kein aktiver Würfeleintrag gefunden."));
    }
}
