package com.leclowndu93150.thaumaturge.api.warp;

import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Static facade for reading and inflicting warp. Backed by the mod at init via
 * {@link #bind}; all calls before binding throw {@link IllegalStateException}.
 *
 * @since 1.0
 */
public final class WarpHelper {
    private static Bindings bindings;

    private WarpHelper() {}

    /**
     * Binds the implementation. Called once by the mod during construction.
     *
     * @param impl the implementation to delegate to
     */
    public static void bind(Bindings impl) {
        bindings = impl;
    }

    /**
     * @param player the player to read
     * @return the player's warp state; never null
     */
    public static IPlayerWarp getWarp(Player player) {
        return impl().getWarp(player);
    }

    /**
     * Adds or removes warp with full side effects: the event counter resets to
     * the player's new total on any gain, the client is notified, and the
     * first-warp research trigger fires. Does nothing when {@code amount} is
     * zero. Server side only.
     *
     * @param player the affected player
     * @param amount the warp to add; negative to remove
     * @param type the pool to modify
     */
    public static void addWarp(ServerPlayer player, int amount, WarpType type) {
        impl().addWarp(player, amount, type);
    }

    /**
     * @param player the player to read
     * @return permanent plus normal warp, excluding temporary
     */
    public static int getActualWarp(Player player) {
        return impl().getActualWarp(player);
    }

    /**
     * Computes the warp a single stack contributes while held or worn: the {@link IWarpingGear} value, plus the stack's
     * {@code thaumaturge:warp} data component, plus any {@code thaumaturge:warp} data-map entry on the item.
     *
     * @param stack the stack to evaluate; empty stacks contribute zero
     * @param wearer the entity holding or wearing the stack
     * @return the warp contribution, zero or greater
     */
    public static int getFinalWarp(ItemStack stack, LivingEntity wearer) {
        return impl().getFinalWarp(stack, wearer);
    }

    private static Bindings impl() {
        if (bindings == null) {
            throw new IllegalStateException("WarpHelper used before Thaumaturge bound its implementation");
        }
        return bindings;
    }

    /**
     * Implementation hook bound by the mod.
     *
     * @since 1.0
     */
    public interface Bindings {
        IPlayerWarp getWarp(Player player);

        void addWarp(ServerPlayer player, int amount, WarpType type);

        int getActualWarp(Player player);

        int getFinalWarp(ItemStack stack, LivingEntity wearer);
    }
}
