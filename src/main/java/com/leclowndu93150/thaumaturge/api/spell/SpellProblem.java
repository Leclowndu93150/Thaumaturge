package com.leclowndu93150.thaumaturge.api.spell;

import java.util.Optional;
import net.minecraft.network.chat.Component;

/**
 * One thing wrong with a spell, as reported by {@link Spells#analyze}.
 *
 * @param message the player-facing description
 * @param fatal   true when the spell cannot be inscribed or cast because of it
 * @param node    the node at fault, empty when the problem concerns the whole spell
 * @since 1.0.0
 */
public record SpellProblem(Component message, boolean fatal, Optional<SpellNode> node) {
    /**
     * A problem with the whole spell.
     *
     * @param message the description
     * @param fatal   whether it blocks the spell
     * @return the problem
     */
    public static SpellProblem of(Component message, boolean fatal) {
        return new SpellProblem(message, fatal, Optional.empty());
    }

    /**
     * A problem with one node.
     *
     * @param node    the node
     * @param message the description
     * @param fatal   whether it blocks the spell
     * @return the problem
     */
    public static SpellProblem at(SpellNode node, Component message, boolean fatal) {
        return new SpellProblem(message, fatal, Optional.of(node));
    }
}
