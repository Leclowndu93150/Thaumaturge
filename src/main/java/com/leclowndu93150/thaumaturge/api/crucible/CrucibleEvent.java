package com.leclowndu93150.thaumaturge.api.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Fired on the NeoForge game event bus when an item thrown into a crucible is processed. Both subtypes run on the logical server only, once per item in the thrown stack.
 *
 * @see Crafted
 * @see Dissolve
 * @since 1.1.0
 */
public abstract class CrucibleEvent extends BlockEvent {
    private final Player player;
    private final IAspectContainer crucible;

    /**
     * Constructs the event. Fired by the implementation; addons receive it, they do not build it.
     *
     * @param player   the player credited with the thrown item
     * @param pos      the position of the crucible
     * @param state    the crucible's block state
     * @param crucible the crucible's aspect contents
     */
    protected CrucibleEvent(Player player, BlockPos pos, BlockState state, IAspectContainer crucible) {
        super(player.level(), pos, state);
        this.player = player;
        this.crucible = crucible;
    }

    /**
     * The player credited with the thrown item, used for research checks on recipes.
     *
     * @return the player; never null
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The crucible's aspect contents at the time the event fires.
     *
     * @return the crucible as an aspect container
     */
    public IAspectContainer getCrucible() {
        return crucible;
    }

    /**
     * Fired after a crucible recipe matched, its aspects were removed and the result was ejected.
     *
     * @since 1.1.0
     */
    public static final class Crafted extends CrucibleEvent {
        private final ItemStack result;
        private final AspectList consumed;

        /**
         * Constructs the event. Fired by the implementation; addons receive it, they do not build it.
         *
         * @param player   the player credited with the craft
         * @param pos      the position of the crucible
         * @param state    the crucible's block state
         * @param crucible the crucible's aspect contents after the craft
         * @param result   a copy of the ejected result
         * @param consumed the aspects the recipe consumed
         */
        public Crafted(
                Player player,
                BlockPos pos,
                BlockState state,
                IAspectContainer crucible,
                ItemStack result,
                AspectList consumed) {
            super(player, pos, state, crucible);
            this.result = result;
            this.consumed = consumed;
        }

        /**
         * The crafted item. The result has already been ejected, so changes to this stack have no effect.
         *
         * @return a copy of the result
         */
        public ItemStack getResult() {
            return result;
        }

        /**
         * The aspects the recipe took from the crucible.
         *
         * @return the consumed aspects
         */
        public AspectList getConsumed() {
            return consumed;
        }
    }

    /**
     * Fired when a thrown item matches no recipe and is about to dissolve into the crucible. Listeners may replace the aspects it adds with {@link #setAspects(AspectList)}. Cancelling the event,
     * or leaving it with an empty list, keeps the item out of the crucible.
     *
     * @since 1.1.0
     */
    public static final class Dissolve extends CrucibleEvent implements ICancellableEvent {
        private final ItemStack stack;
        private AspectList aspects;

        /**
         * Constructs the event. Fired by the implementation; addons receive it, they do not build it.
         *
         * @param player   the player credited with the thrown item
         * @param pos      the position of the crucible
         * @param state    the crucible's block state
         * @param crucible the crucible's aspect contents before the item dissolves
         * @param stack    the item being dissolved
         * @param aspects  the aspects the item would add
         */
        public Dissolve(
                Player player,
                BlockPos pos,
                BlockState state,
                IAspectContainer crucible,
                ItemStack stack,
                AspectList aspects) {
            super(player, pos, state, crucible);
            this.stack = stack;
            this.aspects = aspects;
        }

        /**
         * The item being dissolved.
         *
         * @return the thrown stack
         */
        public ItemStack getStack() {
            return stack;
        }

        /**
         * The aspects the item adds to the crucible.
         *
         * @return the current aspect list
         */
        public AspectList getAspects() {
            return aspects;
        }

        /**
         * Replaces the aspects the item adds.
         *
         * @param aspects the new aspect list; an empty list keeps the item out of the crucible
         */
        public void setAspects(AspectList aspects) {
            this.aspects = aspects;
        }
    }
}
