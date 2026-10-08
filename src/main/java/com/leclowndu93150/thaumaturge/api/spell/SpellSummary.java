package com.leclowndu93150.thaumaturge.api.spell;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;

/**
 * The costs, shape and problems of a spell, as computed by {@link Spells#analyze}.
 *
 * @param complexity    the total complexity
 * @param budget        the focus's complexity budget, 0 when analysed without a focus
 * @param vis           the vis drained per instant or charged cast, before caster discounts
 * @param xp            the experience levels needed to inscribe the spell
 * @param crystals      the essentia crystals needed to inscribe the spell, by aspect
 * @param depth         the most nodes on one path, origin excluded
 * @param branches      the number of branching flow nodes
 * @param repeats       the echo repeats summed over the spell
 * @param cooldown      the ticks an instant cast waits before the next
 * @param chargeTicks   the ticks a charged cast takes to reach full charge
 * @param pulseInterval the ticks between channeled pulses
 * @param problems      everything wrong with the spell, fatal first
 * @since 1.0.0
 */
public record SpellSummary(
        int complexity,
        int budget,
        float vis,
        int xp,
        Map<ResourceKey<IAspect>, Integer> crystals,
        int depth,
        int branches,
        int repeats,
        int cooldown,
        int chargeTicks,
        int pulseInterval,
        List<SpellProblem> problems) {
    /**
     * Canonicalizes the collections into immutable copies.
     */
    public SpellSummary {
        crystals = Map.copyOf(crystals);
        problems = List.copyOf(problems);
    }

    /**
     * Whether the spell may be inscribed and cast.
     *
     * @return true when no problem is fatal
     */
    public boolean valid() {
        for (SpellProblem problem : problems) {
            if (problem.fatal()) {
                return false;
            }
        }
        return true;
    }
}
