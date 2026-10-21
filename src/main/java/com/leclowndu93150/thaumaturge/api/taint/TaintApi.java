package com.leclowndu93150.thaumaturge.api.taint;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Static facade over the taint spread engine. Addons can seed, query, and drive taint without
 * touching internals.
 *
 * <p>All mutating calls are server side; passing a client world is a no-op for queries and
 * ignored for mutators.
 *
 * @since 1.0.0
 */
public final class TaintApi {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("TaintApi");

    private TaintApi() {}

    private static Bindings impl() {
        return BINDING.get();
    }

    /**
     * Attempts to taint the four-by-four block biome column containing {@code at}, across the
     * level's full build height. Honours wuss mode, disabled spread, bloom protection and
     * {@link TaintBiomeEvent}. Never loads a chunk. Existing biome persistence and client
     * updates are used. Only {@link BiomeChange#CHANGED} reports a resource-producing change;
     * callers must not charge for any other outcome. This operation does not consume fuel.
     *
     * @param world the server level
     * @param at the requested position, inside the world border and build height
     * @param player the initiator, or null for a trusted server mechanic; never supplied by a client
     * @return the outcome of this attempt
     * @since 1.0.4
     */
    public static BiomeChange tryTaintBiome(ServerLevel world, BlockPos at, @Nullable ServerPlayer player) {
        return impl().tryTaintBiome(world, at, player);
    }

    /** Outcomes of an addon biome mutation. */
    public enum BiomeChange {
        CHANGED, UNCHANGED, DISABLED, DENIED, UNLOADED, UNSUPPORTED, NOT_SERVER_THREAD
    }

    /**
     * Runs one taint spread attempt from the given position, identical to the random-tick spread
     * performed by taint blocks. Honours the wuss-mode and spread-rate configuration unless
     * {@code ignoreGates} is set.
     *
     * @param world the world to mutate
     * @param at the spread origin
     * @param ignoreGates when true, bypasses the configured rate and wuss-mode gates
     */
    public static void spreadFibres(ServerLevel world, BlockPos at, boolean ignoreGates) {
        impl().spreadFibres(world, at, ignoreGates);
    }

    /**
     * Returns whether the position counts as tainted land: its biome is tagged
     * {@code thaumaturge:is_tainted}, or its chunk's pressure has reached the tainted threshold.
     * Client levels report {@code false}.
     *
     * @param world the world to query
     * @param at the position to test
     * @return {@code true} when the position is tainted
     */
    public static boolean isTainted(Level world, BlockPos at) {
        return impl().isTainted(world, at);
    }

    /**
     * Returns the ecological taint pressure of the chunk containing the position, from 0 to 1.
     * Pressure decays slowly over time, and more slowly where the aura holds a lot of flux.
     * Unloaded chunks and client levels report 0.
     *
     * @param world the world to query
     * @param at any position inside the chunk
     * @return the current pressure, from 0 to 1
     */
    public static float getEcologicalPressure(Level world, BlockPos at) {
        return impl().getEcologicalPressure(world, at);
    }

    /**
     * Registers a taint seed anchor. Spread checks treat registered positions as live seeds as
     * long as a taint seed entity is present within one block of the position; stale entries are
     * pruned automatically during spread checks.
     *
     * @param world the world owning the seed
     * @param at the seed anchor position
     */
    public static void addTaintSeed(ServerLevel world, BlockPos at) {
        impl().addTaintSeed(world, at);
    }

    /**
     * Removes ecological taint pressure from the chunk containing the position, down to 0. The
     * stored pressure is discarded once it reaches 0. Does nothing when the chunk is not loaded.
     *
     * @param world the world to mutate
     * @param at any position inside the chunk
     * @param delta the pressure to remove; zero or negative amounts are ignored
     */
    public static void cleanEcologicalPressure(ServerLevel world, BlockPos at, float delta) {
        impl().cleanEcologicalPressure(world, at, delta);
    }

    /**
     * Adds ecological taint pressure to the chunk containing the position, capped at 1. Does
     * nothing in wuss mode or when the chunk is not loaded.
     *
     * @param world the world to mutate
     * @param at any position inside the chunk
     * @param delta the pressure to add; zero or negative amounts are ignored
     */
    public static void addEcologicalPressure(ServerLevel world, BlockPos at, float delta) {
        impl().addEcologicalPressure(world, at, delta);
    }

    /**
     * Removes a taint seed anchor previously registered via {@link #addTaintSeed}.
     *
     * @param world the world owning the seed
     * @param at the seed anchor position
     */
    public static void removeTaintSeed(ServerLevel world, BlockPos at) {
        impl().removeTaintSeed(world, at);
    }

    /**
     * Returns whether an active taint source, currently a live taint seed, is close enough to
     * the position to drive spread there.
     *
     * @param world the world to query
     * @param at the position to test
     * @return {@code true} when an active source is in range
     */
    public static boolean hasActiveSource(Level world, BlockPos at) {
        return impl().hasActiveSource(world, at);
    }

    /**
     * Returns whether the position lies on the outer fringe of a seed's spread zone, the ring
     * where new taint seeds may sprout naturally.
     *
     * @param world the world to query
     * @param at the position to test
     * @return {@code true} when the position is between 80% and 100% of the spread radius
     */
    public static boolean isAtTaintSeedEdge(Level world, BlockPos at) {
        return impl().isAtTaintSeedEdge(world, at);
    }

    /**
     * Returns whether the position lies within the configured spread radius of a live taint
     * seed. A registered seed whose entity has died is pruned and reported as absent.
     *
     * @param world the world to query
     * @param at the position to test
     * @return {@code true} when a live seed is in range
     */
    public static boolean isNearTaintSeed(Level world, BlockPos at) {
        return impl().isNearTaintSeed(world, at);
    }

    /**
     * Binds the facade's implementation. Called once at mod init by the implementation; addons
     * must not call this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /**
     * Implementation hook supplied by Thaumaturge at mod init. Each method on this interface
     * corresponds to a public static on {@link TaintApi}. Addons must not implement this
     * interface.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        BiomeChange tryTaintBiome(ServerLevel level, BlockPos pos, @Nullable ServerPlayer player);

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
