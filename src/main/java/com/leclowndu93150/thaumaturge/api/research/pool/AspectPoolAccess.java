package com.leclowndu93150.thaumaturge.api.research.pool;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Static facade over a player's aspect research point pools.
 *
 * <p>Every aspect a player has discovered carries a point balance earned by scanning. Points
 * are spent to place aspects on research notes, so this is the currency of research
 * progression. Discovery is tracked separately from the balance: an aspect may be discovered
 * with zero points remaining.
 *
 * <p>Primal aspects are always considered discovered. Compound aspects report discovery only
 * once the player has actually encountered them.
 *
 * <p>Mutating methods require a {@link ServerPlayer} and take effect on the server, syncing to
 * the owning client automatically. Query methods accept any {@link Player} and are safe on
 * both sides.
 *
 * <p>The implementation is bound once at mod init by Thaumaturge via {@link #bind(Bindings)};
 * addons must not call {@code bind}.
 *
 * @since 1.0.0
 */
public final class AspectPoolAccess {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("AspectPoolAccess");

    private AspectPoolAccess() {}

    /**
     * Binds the implementation. Called by Thaumaturge during mod init.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /**
     * Whether the player has discovered the given aspect. Primal aspects are always
     * discovered.
     *
     * @param player the player to query
     * @param aspect the aspect to test
     * @return {@code true} when the aspect is discovered
     */
    public static boolean isDiscovered(Player player, Holder<IAspect> aspect) {
        return BINDING.get().isDiscovered(player, aspect);
    }

    /**
     * The player's current point balance for the given aspect.
     *
     * @param player the player to query
     * @param aspect the aspect to query
     * @return the balance, zero when undiscovered
     */
    public static int amount(Player player, Holder<IAspect> aspect) {
        return BINDING.get().amount(player, aspect);
    }

    /**
     * Whether every component of the given aspect has been discovered. Always {@code true}
     * for primal aspects.
     *
     * @param player the player to query
     * @param aspect the aspect whose components are tested
     * @return {@code true} when all components are discovered
     */
    public static boolean hasDiscoveredComponents(Player player, Holder<IAspect> aspect) {
        return BINDING.get().hasDiscoveredComponents(player, aspect);
    }

    /**
     * Grants points for the given aspect, discovering it when new. Grants are subject to the
     * diminishing returns applied to large balances.
     *
     * @param player the player to grant to
     * @param aspect the aspect to grant
     * @param amount the requested point amount
     * @return the points actually granted after scaling
     */
    public static int grant(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        return BINDING.get().grant(player, aspect, amount);
    }

    /**
     * Grants points for every entry of the given list.
     *
     * @param player  the player to grant to
     * @param aspects the aspects and amounts to grant
     */
    public static void grantAll(ServerPlayer player, AspectList aspects) {
        BINDING.get().grantAll(player, aspects);
    }

    /**
     * Spends points of the given aspect.
     *
     * @param player the player to charge
     * @param aspect the aspect to spend
     * @param amount the point amount to spend
     * @return {@code true} when the player could afford it and the points were deducted
     */
    public static boolean spend(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        return BINDING.get().spend(player, aspect, amount);
    }

    /**
     * Whether the player can afford every entry of the given cost.
     *
     * @param player the player to query
     * @param cost   the aspects and amounts required
     * @return {@code true} when the whole cost is affordable
     */
    public static boolean canAfford(Player player, AspectList cost) {
        return BINDING.get().canAfford(player, cost);
    }

    /**
     * Spends the whole cost atomically. Nothing is deducted when any entry is unaffordable.
     *
     * @param player the player to charge
     * @param cost   the aspects and amounts to spend
     * @return {@code true} when the whole cost was deducted
     */
    public static boolean spendAll(ServerPlayer player, AspectList cost) {
        return BINDING.get().spendAll(player, cost);
    }

    /**
     * Returns previously spent points without applying grant scaling.
     *
     * @param player the player to refund
     * @param aspect the aspect to refund
     * @param amount the point amount to return
     */
    public static void refund(ServerPlayer player, Holder<IAspect> aspect, int amount) {
        BINDING.get().refund(player, aspect, amount);
    }

    /**
     * Implementation hook. Each method corresponds to a public static on
     * {@link AspectPoolAccess}. Addons must not implement this interface.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        boolean isDiscovered(Player player, Holder<IAspect> aspect);

        int amount(Player player, Holder<IAspect> aspect);

        boolean hasDiscoveredComponents(Player player, Holder<IAspect> aspect);

        int grant(ServerPlayer player, Holder<IAspect> aspect, int amount);

        void grantAll(ServerPlayer player, AspectList aspects);

        boolean spend(ServerPlayer player, Holder<IAspect> aspect, int amount);

        boolean canAfford(Player player, AspectList cost);

        boolean spendAll(ServerPlayer player, AspectList cost);

        void refund(ServerPlayer player, Holder<IAspect> aspect, int amount);
    }
}
