package de.fabiansiemens.hyperion.core.features.roll.v2.node;

import de.fabiansiemens.hyperion.core.features.roll.v2.DiceNodeVisitor;
import lombok.Builder;

import java.util.List;

/**
 * Würfelwurf-Ausdruck: {@code [count] d [sides] [argumente…]}.
 *
 * <p>{@code count} und {@code sides} können {@link NumberNode} oder {@link GroupingNode}
 * sein (Inner Rolls). Alle Modifier-/Schwellwert-Knoten sind ebenfalls {@link DiceNode},
 * sodass Inner Rolls überall erlaubt sind.
 *
 * @param count         Anzahl der Würfel (null → implizit 1)
 * @param sides         Seitenanzahl des Würfels
 * @param modifiers     Liste der Modifikatoren ({@code +}, {@code -}, {@code ++}, {@code --})
 * @param advantage     Vor-/Nachteil-Konfiguration (null = keiner)
 * @param advAllScope   Vorteil gilt für jeden Einzelwurf (statt nur den ersten)
 * @param reroll        Reroll-Bedingung (null = kein Reroll)
 * @param rerollAllScope Reroll gilt für jeden Einzelwurf
 * @param cap           Cap-Begrenzung (null = keine)
 * @param capAllScope   Cap gilt für jeden Einzelwurf
 * @param check         Prüf-Bedingung (null = keine)
 * @param checkAllScope Check gilt für jeden Einzelwurf
 */
public record DiceRollNode(
        DiceNode          count,
        DiceNode          sides,
        List<ModifierNode> modifiers,
        AdvantageNode     advantage,
        boolean           advAllScope,
        RerollNode        reroll,
        boolean           rerollAllScope,
        CapNode           cap,
        boolean           capAllScope,
        CheckNode         check,
        boolean           checkAllScope
) implements DiceNode {

    @Override
    public <T> T accept(DiceNodeVisitor<T> v) {
        return v.visitDiceRoll(this);
    }

    public boolean hasModifiers()        { return modifiers != null && !modifiers.isEmpty(); }
    public boolean hasPerRollModifiers() { return modifiers != null && modifiers.stream().anyMatch(ModifierNode::allScope); }
    public boolean hasSingleModifiers()  { return modifiers != null && modifiers.stream().anyMatch(m -> !m.allScope()); }
}
