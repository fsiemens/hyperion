package de.fabiansiemens.hyperion.core.features.roll.v2.result;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Flaches Ergebnisobjekt einer Würfeloperation für externe Abnehmer (z. B. Discord-Bot).
 *
 * <p>Für vollständige Ausführungshistorie und Baumstruktur:
 * {@link de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode}.
 */
@Value
@Builder
public class RollResult {

    String             expression;
    String             diceSectionLabel;
    List<InnerRoll>    innerRolls;
    List<DiceEntry>    dice;
    List<ModifierEntry> modifiers;
    boolean            checkAllScope;
    int                baseTotal;
    int                modifierTotal;
    int                total;
    List<CheckEntry>   checkResults;
    List<CritEntry>    critResults;
    RollResult         chainedResult;

    // =========================================================================
    // Innere Würfe
    // =========================================================================

    /**
     * Gekapselter Würfelausdruck, der als Anzahl oder Seitenanzahl eines äußeren
     * Wurfs diente.
     *
     * @param label       Anzeige-Label (z. B. {@code "Anzahl-Wurf (d6)"})
     * @param dice        Erzeugte Würfe
     * @param result      Ergebnis der Kapseloperation
     * @param description Erklärung (z. B. {@code "d6 → 4 (Anzahl der d20-Würfe)"})
     */
    public record InnerRoll(String label, List<DiceEntry> dice, int result, String description) {}

    // =========================================================================
    // Abgeleitete Abfragen
    // =========================================================================

    public int successCount() {
        return (int) checkResults.stream().filter(CheckEntry::isPassed).count();
    }

    public int failCount() {
        return (int) checkResults.stream().filter(c -> !c.isPassed()).count();
    }

    public int critSuccessCount() {
        return (int) critResults.stream().filter(CritEntry::isCritSuccess).count();
    }

    public int critFailCount() {
        return (int) critResults.stream().filter(c -> !c.isCritSuccess()).count();
    }

    public long activeCount() {
        return dice.stream().filter(d -> !d.isIgnored()).count();
    }

    // =========================================================================
    // Textdarstellung
    // =========================================================================

    public String summary() {
        StringBuilder sb = new StringBuilder(expression).append(": ").append(total);
        if (!checkResults.isEmpty()) {
            sb.append("  [").append(successCount()).append("× SUCCESS / ")
              .append(failCount()).append("× FAIL]");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return summary();
    }
}
