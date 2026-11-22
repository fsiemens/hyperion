package de.fabiansiemens.hyperion.ui.roll;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.fabiansiemens.hyperion.core.features.roll.v2.RollCacheEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.node.FlatNode;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.CheckEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.CritEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.DiceEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.result.ModifierEntry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;

@Slf4j
@Component
public class RollDetailsPanel {

    // =========================================================================
    // Öffentlicher Einstiegspunkt
    // =========================================================================

    public Container build(Guild guild, RollCacheEntry roll, int rollIndex) {

    	ExecutionNode root = roll.getNode();
        List<FlatNode> flat = root.flatten();

        ExecutionNode result = flat.stream()
            .filter(n -> n.index() == rollIndex)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Kein Knoten mit Index " + rollIndex + " gefunden."))
            .node();

        List<ContainerChildComponent> components = new LinkedList<>();
        components.add(TextDisplay.of(
            "Detailliertes Ergebnis für \n```" + root.getPrompt() + "```von " + guild.getJDA().getUserById(roll.getUserId()).getAsMention()));

        // Dropdown nur wenn mehrere navigierbare Knoten vorhanden
        List<FlatNode> navigable = flat.stream()
            .filter(n -> !n.node().isConstant())
            .toList();
        if (navigable.size() > 1) {
            StringSelectMenu.Builder rollSelect = StringSelectMenu.create("ss.roll.details");
            for (FlatNode n : navigable) {
                ExecutionNode node = n.node();
                rollSelect.addOption(node.getPrompt(), String.valueOf(n.index()),
                    "→ " + node.getResolvedValue());
            }
            rollSelect.setDefaultValues(String.valueOf(rollIndex));
            rollSelect.setRequiredRange(1, 1);
            components.add(ActionRow.of(rollSelect.build()));
        }

        components.add(Separator.createDivider(Separator.Spacing.SMALL));

        if (result.isChain()) {
            log.info("Sending Chain Overview");
            addChainOverview(components, result);
        } else if (result.isRoot()) {
            log.info("Sending Root Overview");
            addRootOverview(components, result);
        } else {
            log.info("Sending Atomic Details");
            addAtomicComponents(components, result);
        }

        return Container.of(components);
    }

    // =========================================================================
    // Atomare Operation – volle Detailansicht
    // =========================================================================

    private void addAtomicComponents(List<ContainerChildComponent> components, ExecutionNode result) {
        List<ModifierEntry> perRollMods = perRollModifiers(result.getModifiers());

        addInnerRollComponents(components, result);
        components.add(TextDisplay.of(buildDiceList(result, perRollMods).toString()));

        StringBuilder calc = buildCalculation(result, perRollMods).append("`");
        StringBuilder summary = new StringBuilder("### Endergebnis:\n> # ")
            .append(result.getResolvedValue())
            .append("\n> ").append(calc);

        if (result.hasCrits())
            summary.append("\n").append(buildCritLine(result));
        if (result.hasCheck())
            summary.append("\n").append(buildCheckLine(result));

        components.add(TextDisplay.of(summary.toString()));
    }

    // =========================================================================
    // Chain-Übersicht  (d20 check15 ? 3d8 : 1d4)
    // =========================================================================

    /**
     * Kompakte Übersicht einer Chain-Operation.
     * Details zu Teiloperationen sind über das Dropdown abrufbar.
     *
     * <p>Kindknoten-Layout:
     * <pre>
     *   children[0] = Bedingungswurf (ROLL)
     *   children[1] = ausgeführter Zweig (nur wenn vorhanden)
     * </pre>
     */
    private void addChainOverview(List<ContainerChildComponent> components, ExecutionNode result) {
        List<ExecutionNode> children = result.getChildren();
        ExecutionNode cond = children.get(0);

        // Check-Ergebnis des Bedingungswurfs
        String checkLine = "";
        if (!cond.getChecks().isEmpty()) {
            CheckEntry check = cond.getChecks().get(0);
            checkLine = "  " + check.conditionLabel() + "  "
                + (result.isChainPassed() ? "✅" : "❌");
        }

        // Ausgeführter Zweig
        String branchLine;
        if (children.size() < 2) {
            branchLine = "> `0` - Kein Alternativ-Zweig";
        } else {
            ExecutionNode branch = children.get(1);
            branchLine = "> `" + branch.getPrompt() + "`"
                + "  →  **" + branch.getResolvedValue() + "**";
        }

        StringBuilder sb = new StringBuilder("### Gesamtergebnis:\n> # ")
            .append(result.getResolvedValue()).append("\n\n")
            .append("**Teiloperationen:**\n")
            .append("> `").append(cond.getPrompt()).append("`")
            .append("  →  **").append(cond.getResolvedValue()).append("**")
            .append(checkLine).append("\n")
            .append(branchLine);

        components.add(TextDisplay.of(sb.toString()));
    }

    // =========================================================================
    // Root-Übersicht  (d20 +4, 2d6)
    // =========================================================================

    /**
     * Kompakte Übersicht einer Root-Operation (kommagetrennte Addition).
     * Details zu Teiloperationen sind über das Dropdown abrufbar.
     */
    private void addRootOverview(List<ContainerChildComponent> components, ExecutionNode result) {
        StringBuilder sb = new StringBuilder("### Gesamtergebnis:\n> # ")
            .append(result.getResolvedValue()).append("\n\n")
            .append("**Teiloperationen:**\n");

        for (ExecutionNode child : result.getChildren()) {
            if (child.isConstant()) continue;
            sb.append("> `").append(child.getPrompt()).append("`")
              .append("  →  **").append(child.getResolvedValue()).append("**\n");
        }

        components.add(TextDisplay.of(sb.toString()));
    }

    // =========================================================================
    // Inner Rolls  (z. B. (d4)d20)
    // =========================================================================

    private void addInnerRollComponents(List<ContainerChildComponent> components, ExecutionNode node) {
        List<ExecutionNode> inners = node.getChildren().stream()
            .filter(ExecutionNode::isInner)
            .toList();
        if (inners.isEmpty()) return;

        StringBuilder sb = new StringBuilder("### Parameter-Würfe:\n");
        for (ExecutionNode inner : inners) {
            String roleLabel = switch (inner.getInnerRole()) {
                case COUNT            -> "Anzahl";
                case SIDES            -> "Seiten";
                case MODIFIER         -> "Modifikator";
                case REROLL_THRESHOLD -> "Reroll-Grenzwert";
                case CHECK_THRESHOLD  -> "Check-Grenzwert";
                default               -> inner.getInnerRole().name();
            };
            sb.append("> **").append(roleLabel).append(":** `")
              .append(inner.getPrompt()).append("` → **")
              .append(inner.getResolvedValue()).append("**\n");

            if (!inner.getDice().isEmpty()) {
                sb.append(">  ");
                inner.getDice().forEach(d -> {
                    if (d.isIgnored()) sb.append("~~");
                    sb.append("`").append(d.getValue()).append("`");
                    if (d.isIgnored()) sb.append("~~");
                    sb.append(" ");
                });
                sb.append("\n");
            }
        }
        components.add(TextDisplay.of(sb.toString()));
    }

    // =========================================================================
    // Würfelliste
    // =========================================================================

    private StringBuilder buildDiceList(ExecutionNode result, List<ModifierEntry> perRollMods) {
        StringBuilder sb = new StringBuilder("### Würfelergebnisse:\n");
        List<DiceEntry> dice = result.getDice();
 
        if (dice.size() > 20) {
            sb.append("-# wegen Anzahl zusammengefasst\n");
            int cumMod = perRollMods.stream().mapToInt(ModifierEntry::getSignedValue).sum();
 
            List<Integer> active = dice.stream()
                .filter(d -> !d.isIgnored())
                .map(d -> d.getValue() + cumMod)
                .collect(Collectors.toList());
            appendStats(sb, active);
 
            if (anyIgnored(dice)) {
                sb.append("**Verhindert:**\n");
                List<Integer> ignored = dice.stream()
                    .filter(DiceEntry::isIgnored)
                    .map(d -> d.getValue() + cumMod)
                    .collect(Collectors.toList());
                appendStats(sb, ignored);
            }
        } else {
            boolean vertical = !perRollMods.isEmpty() || anyIgnored(dice);
            sb.append(">>> ");
            for (DiceEntry d : dice) {
                if (d.isIgnored()) sb.append("~~");
                sb.append("`").append(d.getValue()).append("`");
                for (ModifierEntry mod : perRollMods)
                    sb.append(" ").append(mod).append(" = ")
                      .append(d.getValue() + mod.getSignedValue());
                if (d.isIgnored())
                    sb.append("~~ (").append(d.getIgnoreReason().getLabel()).append(")");
                sb.append(vertical ? "\n " : " ");
            }
        }
        return sb;
    }

    // =========================================================================
    // Berechnung
    // =========================================================================

    private StringBuilder buildCalculation(ExecutionNode result, List<ModifierEntry> perRollMods) {
        StringBuilder sb = new StringBuilder("`=");
        List<DiceEntry> active = result.getDice().stream()
            .filter(d -> !d.isIgnored()).toList();

        if (result.getDice().size() <= 20) {
            for (DiceEntry d : active) {
                sb.append(" ").append(d);
                for (ModifierEntry mod : perRollMods)
                    sb.append(" ").append(mod);
            }
        } else {
            int diceSum = active.stream().mapToInt(DiceEntry::getValue).sum();
            sb.append(" ").append(diceSum);
            int perRollTotal = perRollMods.stream().mapToInt(ModifierEntry::getSignedValue).sum()
                * (int) active.size();
            if (perRollTotal != 0)
                sb.append(perRollTotal > 0 ? " +" : " ").append(perRollTotal);
        }

        result.getModifiers().stream()
            .filter(m -> !m.isAppliedToAll())
            .forEach(m -> sb.append(" ").append(m));

        return sb;
    }

    // =========================================================================
    // Crit- und Check-Zeilen
    // =========================================================================

    private String buildCritLine(ExecutionNode result) {
        long suc  = result.getCrits().stream().filter(CritEntry::isCritSuccess).count();
        long fail = result.getCrits().stream().filter(c -> !c.isCritSuccess()).count();
        return "**Kritische Würfe:**\n> "
            + suc  + "x <:critsuccess:1496283669952991292>, "
            + fail + "x <:critfail:1496283730732519626>";
    }

    private String buildCheckLine(ExecutionNode result) {
        long suc  = result.getChecks().stream().filter(CheckEntry::isPassed).count();
        long fail = result.getChecks().stream().filter(c -> !c.isPassed()).count();
        return "**Checks:**\n> " + suc + "x ✅, " + fail + "x ❌";
    }

    // =========================================================================
    // Hilfsmethoden
    // =========================================================================

    private List<ModifierEntry> perRollModifiers(List<ModifierEntry> modifiers) {
        return modifiers.stream()
            .filter(ModifierEntry::isAppliedToAll)
            .collect(Collectors.toList());
    }

    private boolean anyIgnored(List<DiceEntry> dice) {
        return dice.stream().anyMatch(DiceEntry::isIgnored);
    }

    private void appendStats(StringBuilder sb, List<Integer> values) {
        if (values.isEmpty()) return;
        int min = Collections.min(values);
        int max = Collections.max(values);
        int sum = values.stream().mapToInt(Integer::intValue).sum();
        double avg = (double) sum / values.size();
        sb.append("> **Min:** ").append(min)
          .append(", **Max:** ").append(max).append("\n")
          .append("> **∑:** ").append(sum)
          .append(", **Ø:** ").append(String.format("%.2f", avg)).append("\n");
    }
}