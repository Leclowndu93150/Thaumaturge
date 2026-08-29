package com.leclowndu93150.thaumaturge.api.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Static facade over the taint spread engine, letting addons seed, query, and drive taint
 * without touching internals.
 *
 * <p>All mutating calls are server side; passing a client level is a no-op for queries and
 * ignored for mutators.
 *
 * @since 1.0.0
 */
public final class TaintApi {
    private static Bindings impl;

    private TaintApi() {}

    /**
     * Registers a taint seed anchor. Spread checks treat registered positions as live seeds as
     * long as a taint seed entity is present within one block of the position; stale entries are
     * pruned automatically during spread checks.
     *
     * @param level the level owning the seed
     * @param pos   the seed anchor position
     */
    public static void addTaintSeed(ServerLevel level, BlockPos pos) {
        bindingOrThrow().addTaintSeed(level, pos);
    }

    /**
     * Removes a taint seed anchor previously registered via {@link #addTaintSeed}.
     *
     * @param level the level owning the seed
     * @param pos   the seed anchor position
     */
    public static void removeTaintSeed(ServerLevel level, BlockPos pos) {
        bindingOrThrow().removeTaintSeed(level, pos);
    }

    /**
     * Returns whether the position lies within the configured spread radius of a live taint
     * seed. A registered seed whose entity has died is pruned and reported as absent.
     *
     * @param level the level to query
     * @param pos   the position to test
     * @return {@code true} when a live seed is in range
     */
    public static boolean isNearTaintSeed(Level level, BlockPos pos) {
        return bindingOrThrow().isNearTaintSeed(level, pos);
    }

    /**
     * Returns whether the position lies on the outer fringe of a seed's spread zone, the ring
     * where new taint seeds may sprout naturally.
     *
     * @param level the level to query
     * @param pos   the position to test
     * @return {@code true} when the position is between 80% and 100% of the spread radius
     */
    public static boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        return bindingOrThrow().isAtTaintSeedEdge(level, pos);
    }

    /**
     * Runs one local fibre/terrain colonisation attempt from the given position. Ordinary calls only
     * colonise Tainted Lands, matching TC4. Explicit outbreak sources may pass {@code force} to
     * establish Tainted Lands at a viable target before placing/converting taint there. The separate
     * TC4 biome-frontier probability is not bypassed because it is not part of this method.
     *
     * @param level the level to mutate
     * @param pos   the spread origin
     * @param force when true, allows a viable target to bootstrap its column into Tainted Lands
     */
    public static void spreadFibres(ServerLevel level, BlockPos pos, boolean force) {
        bindingOrThrow().spreadFibres(level, pos, force);
    }

    public static float getEcologicalPressure(Level level, BlockPos pos) {
        return bindingOrThrow().getEcologicalPressure(level, pos);
    }

    public static boolean isTainted(Level level, BlockPos pos) {
        return bindingOrThrow().isTainted(level, pos);
    }

    public static boolean hasActiveSource(Level level, BlockPos pos) {
        return bindingOrThrow().hasActiveSource(level, pos);
    }

    public static void addEcologicalPressure(ServerLevel level, BlockPos pos, float amount) {
        bindingOrThrow().addEcologicalPressure(level, pos, amount);
    }

    public static void cleanEcologicalPressure(ServerLevel level, BlockPos pos, float amount) {
        bindingOrThrow().cleanEcologicalPressure(level, pos, amount);
    }

    /**
     * Binds the facade's implementation. Called once at mod init by the implementation; addons
     * must not call this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        if (impl != null) {
            throw new IllegalStateException("TaintApi already bound");
        }
        impl = bindings;
    }

    private static Bindings bindingOrThrow() {
        if (impl == null) {
            throw new IllegalStateException("TaintApi accessed before binding");
        }
        return impl;
    }

    /**
     * Implementation hook supplied by Thaumaturge at mod init. Each method on this interface
     * corresponds to a public static on {@link TaintApi}. Addons must not implement this
     * interface.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        void addTaintSeed(ServerLevel level, BlockPos pos);

        void removeTaintSeed(ServerLevel level, BlockPos pos);

        boolean isNearTaintSeed(Level level, BlockPos pos);

        boolean isAtTaintSeedEdge(Level level, BlockPos pos);

        void spreadFibres(ServerLevel level, BlockPos pos, boolean force);

        float getEcologicalPressure(Level level, BlockPos pos);

        boolean isTainted(Level level, BlockPos pos);

        boolean hasActiveSource(Level level, BlockPos pos);

        void addEcologicalPressure(ServerLevel level, BlockPos pos, float amount);

        void cleanEcologicalPressure(ServerLevel level, BlockPos pos, float amount);
    }
}
