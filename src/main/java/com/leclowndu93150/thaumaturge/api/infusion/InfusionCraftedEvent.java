package com.leclowndu93150.thaumaturge.api.infusion;

import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import org.jspecify.annotations.Nullable;

/**
 * Fired on the NeoForge game event bus when an infusion matrix finishes a craft, just before the
 * result replaces the catalyst on the central pedestal. Listeners may change the result in place
 * through {@link #getResult()} or swap it with {@link #setResult(ItemStack)}.
 *
 * <p>The event fires for every infusion kind: plain infusion, infusion enchantment and runic
 * augmentation. It runs on the logical server only. A craft that fails from instability never
 * reaches it.
 *
 * <p>{@link InfusionCraftingTransaction#inspect} reports the output before listeners run, so its
 * projection does not include changes made here.
 *
 * @since 1.0.0
 */
public final class InfusionCraftedEvent extends Event {
    private final ServerLevel level;
    private final BlockPos matrixPos;
    private final @Nullable ServerPlayer player;
    private final ItemStack catalyst;
    private ItemStack result;

    /**
     * Constructs the event. Fired by the implementation; addons receive it, they do not build it.
     *
     * @param level     the level the matrix is in
     * @param matrixPos the position of the infusion matrix
     * @param player    the player who started the craft, or null when they are offline or unknown
     * @param catalyst  a copy of the catalyst on the central pedestal
     * @param result    the item the pedestal will hold
     */
    public InfusionCraftedEvent(
            ServerLevel level,
            BlockPos matrixPos,
            @Nullable ServerPlayer player,
            ItemStack catalyst,
            ItemStack result) {
        this.level = level;
        this.matrixPos = matrixPos;
        this.player = player;
        this.catalyst = catalyst;
        this.result = result;
    }

    /**
     * The level the matrix is in.
     *
     * @return the level
     */
    public ServerLevel getLevel() {
        return level;
    }

    /**
     * The position of the infusion matrix. The central pedestal is two blocks below it.
     *
     * @return the matrix position
     */
    public BlockPos getMatrixPos() {
        return matrixPos;
    }

    /**
     * The player who started the craft.
     *
     * @return the player, or null when they are offline or the craft has no recorded crafter
     */
    public @Nullable ServerPlayer getPlayer() {
        return player;
    }

    /**
     * The catalyst consumed by the craft. Changes to this stack have no effect.
     *
     * @return a copy of the catalyst
     */
    public ItemStack getCatalyst() {
        return catalyst;
    }

    /**
     * The item the central pedestal will hold, reflecting any earlier listener's changes. The
     * stack may be modified in place.
     *
     * @return the current result
     */
    public ItemStack getResult() {
        return result;
    }

    /**
     * Replaces the item the central pedestal will hold.
     *
     * @param result the new result; an empty stack leaves the pedestal empty
     * @throws NullPointerException when {@code result} is null
     */
    public void setResult(ItemStack result) {
        this.result = Objects.requireNonNull(result, "result");
    }
}
