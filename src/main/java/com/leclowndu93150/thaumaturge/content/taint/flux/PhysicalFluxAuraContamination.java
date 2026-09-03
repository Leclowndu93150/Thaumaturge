package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBloomRegistry;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lightweight bridge between TC4-style physical Flux and modern numerical aura Flux.
 *
 * <p>Physical Goo/Gas does not add Flux every tick. Instead, active physical Flux blocks report
 * their finite quanta here and establish a capped local aura-Flux floor. If the aura is cleaner
 * than that floor, {@code AuraTickHandler} slowly seeps it upward. Stale reports expire quickly,
 * so cleaning or moving the physical pollution also removes its sustained aura pressure.
 */
public final class PhysicalFluxAuraContamination {
    private static final long STALE_AFTER_TICKS = 80L;
    private static final float GOO_TARGET_PER_QUANTUM = 0.5F;
    private static final float GAS_TARGET_PER_QUANTUM = 0.25F;
    private static final float ABSOLUTE_TARGET_CAP = 30.0F;
    private static final float BASE_TARGET_CAP_RATIO = 0.30F;

    // TC4-first ecological weighting. Goo is dense local corruption; Gas is dangerous but diffuse.
    // This route deliberately outranks the TC6 Rift/Seed path for sustained physical pollution,
    // while still requiring a real accumulation rather than a stray droplet or wisp.
    private static final float GOO_TAINT_WEIGHT_PER_QUANTUM = 1.0F;
    private static final float GAS_TAINT_WEIGHT_PER_QUANTUM = 0.35F;
    private static final float TAINT_OUTBREAK_MIN_WEIGHT = 10.0F;
    private static final float TAINT_OUTBREAK_CHANCE_DIVISOR = 300.0F;
    private static final float TAINT_OUTBREAK_MAX_CHANCE = 0.18F;
    private static final int INITIAL_TAINT_SPREAD_ATTEMPTS = 8;
    private static final int GAS_SURFACE_SEARCH_DEPTH = 24;

    private static final Map<ResourceKey<Level>, Map<Long, Map<Long, Observation>>> OBSERVATIONS =
            new ConcurrentHashMap<>();

    private PhysicalFluxAuraContamination() {}

    private record Observation(int amount, boolean gas, long gameTime) {}

    public static void observeGoo(ServerLevel level, BlockPos pos, int amount) {
        observe(level, pos, amount, false);
    }

    public static void observeGas(ServerLevel level, BlockPos pos, int amount) {
        observe(level, pos, amount, true);
    }

    private static void observe(ServerLevel level, BlockPos pos, int amount, boolean gas) {
        if (amount <= 0) {
            return;
        }
        long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        OBSERVATIONS
                .computeIfAbsent(level.dimension(), ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(chunkKey, ignored -> new ConcurrentHashMap<>())
                .put(
                        pos.asLong(),
                        new Observation(Math.min(PhysicalFlux.MAX_QUANTA, amount), gas, level.getGameTime()));
    }

    public static void forgetChunk(ServerLevel level, ChunkPos chunkPos) {
        Map<Long, Map<Long, Observation>> dimension = OBSERVATIONS.get(level.dimension());
        if (dimension == null) {
            return;
        }
        dimension.remove(chunkPos.toLong());
        if (dimension.isEmpty()) {
            OBSERVATIONS.remove(level.dimension(), dimension);
        }
    }

    /** Returns the sustained local Aura Flux target created by recently observed Goo/Gas. */
    public static float targetFlux(ServerLevel level, ChunkPos chunkPos, float auraBase) {
        Map<Long, Map<Long, Observation>> dimension = OBSERVATIONS.get(level.dimension());
        if (dimension == null) {
            return 0.0F;
        }
        long chunkKey = chunkPos.toLong();
        Map<Long, Observation> samples = dimension.get(chunkKey);
        if (samples == null || samples.isEmpty()) {
            return 0.0F;
        }

        long now = level.getGameTime();
        float target = 0.0F;
        for (Map.Entry<Long, Observation> entry : samples.entrySet()) {
            Observation observation = entry.getValue();
            if (now - observation.gameTime() > STALE_AFTER_TICKS) {
                samples.remove(entry.getKey(), observation);
                continue;
            }
            target += observation.amount() * (observation.gas() ? GAS_TARGET_PER_QUANTUM : GOO_TARGET_PER_QUANTUM);
        }

        if (samples.isEmpty()) {
            dimension.remove(chunkKey, samples);
        }
        if (dimension.isEmpty()) {
            OBSERVATIONS.remove(level.dimension(), dimension);
        }

        float cap = Math.min(ABSOLUTE_TARGET_CAP, Math.max(0.0F, auraBase) * BASE_TARGET_CAP_RATIO);
        return Math.min(target, cap);
    }

    /**
     * Gives sustained TC4-style physical Flux a direct route into persistent Taint.
     *
     * <p>This is intentionally separate from numerical Aura Flux: dense Goo can still fester on
     * its own, while a larger diffuse Goo/Gas mess accumulates weighted ecological pressure here.
     * The roll runs once per aura tick (normally once per second), is capped, and establishes one
     * real Tainted Lands/Fibre foothold. From there ordinary TC4-style Taint ecology takes over.
     * TC6 Rifts/Seeds remain a parallel escalation route rather than the sole bootstrap mechanism.
     */
    public static boolean tryTaintOutbreak(ServerLevel level, ChunkPos chunkPos, RandomSource random) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || !ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get()) {
            return false;
        }

        Map<Long, Map<Long, Observation>> dimension = OBSERVATIONS.get(level.dimension());
        if (dimension == null) {
            return false;
        }
        Map<Long, Observation> samples = dimension.get(chunkPos.toLong());
        if (samples == null || samples.isEmpty()) {
            return false;
        }

        long now = level.getGameTime();
        List<ActiveObservation> active = new ArrayList<>();
        float totalWeight = 0.0F;
        for (Map.Entry<Long, Observation> entry : samples.entrySet()) {
            Observation observation = entry.getValue();
            if (now - observation.gameTime() > STALE_AFTER_TICKS) {
                samples.remove(entry.getKey(), observation);
                continue;
            }

            BlockPos pos = BlockPos.of(entry.getKey());
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            int amount = PhysicalFlux.amount(level, pos);
            if (amount <= 0) {
                samples.remove(entry.getKey(), observation);
                continue;
            }
            boolean gas = state.is(TCBlocks.FLUX_GAS.get());
            float weight = amount * (gas ? GAS_TAINT_WEIGHT_PER_QUANTUM : GOO_TAINT_WEIGHT_PER_QUANTUM);
            if (weight <= 0.0F) {
                continue;
            }
            active.add(new ActiveObservation(pos, amount, gas, weight));
            totalWeight += weight;
        }

        if (samples.isEmpty()) {
            dimension.remove(chunkPos.toLong(), samples);
        }
        if (dimension.isEmpty()) {
            OBSERVATIONS.remove(level.dimension(), dimension);
        }
        if (active.isEmpty() || totalWeight < TAINT_OUTBREAK_MIN_WEIGHT) {
            return false;
        }

        float chance = Math.min(
                TAINT_OUTBREAK_MAX_CHANCE, (totalWeight - TAINT_OUTBREAK_MIN_WEIGHT) / TAINT_OUTBREAK_CHANCE_DIVISOR);
        if (chance <= 0.0F || random.nextFloat() >= chance) {
            return false;
        }

        // Weighted selection naturally prefers Goo and dense pockets while still letting a truly
        // severe Gas cloud poison the land below it.
        float pick = random.nextFloat() * totalWeight;
        for (ActiveObservation observation : active) {
            pick -= observation.weight();
            if (pick <= 0.0F && establishOutbreak(level, observation, random)) {
                return true;
            }
        }
        // If the weighted pick happened to be over an unsuitable river/protected column, try the
        // remaining active pockets before giving up this roll.
        for (ActiveObservation observation : active) {
            if (establishOutbreak(level, observation, random)) {
                return true;
            }
        }
        return false;
    }

    private record ActiveObservation(BlockPos pos, int amount, boolean gas, float weight) {}

    private static boolean establishOutbreak(ServerLevel level, ActiveObservation observation, RandomSource random) {
        BlockPos target = findOutbreakTarget(level, observation);
        if (target == null
                || TaintBloomRegistry.isProtected(level, target)
                || TaintBiomeManager.isTainted(level, target)
                || !TaintBiomeManager.taintColumn(level, target)) {
            return false;
        }

        // Physical pollution becomes the first fibrous foothold instead of merely disappearing
        // into an invisible counter. Goo is replaced directly where viable; Gas sacrifices a few
        // quanta from the cloud that poisoned the surface below it.
        BlockState targetState = level.getBlockState(target);
        if (PhysicalFlux.isPhysicalFlux(targetState) || targetState.isAir() || targetState.canBeReplaced()) {
            level.setBlock(target, BlockTaintFibre.stateForWorld(level, target), Block.UPDATE_ALL);
        }
        if (observation.gas()) {
            PhysicalFlux.reduce(level, observation.pos(), Math.min(2, observation.amount()));
        }

        float severity = Math.min(0.30F, 0.10F + observation.weight() * 0.0125F);
        TaintEcology.addPressure(level, target, severity);
        for (int i = 0; i < INITIAL_TAINT_SPREAD_ATTEMPTS; i++) {
            TaintHelper.spreadFibres(level, target, true);
        }
        return true;
    }

    private static BlockPos findOutbreakTarget(ServerLevel level, ActiveObservation observation) {
        BlockPos source = observation.pos();

        // Dense Goo already occupies the ground-level pollution site in the common case. Replacing
        // it with Fibre is the closest modern equivalent to TC4 Goo festering into Tainted Lands.
        if (!observation.gas() && TaintHelper.isAdjacentToSolidBlock(level, source)) {
            return source;
        }

        // Enclosed Gas can infect a wall/floor-adjacent cell directly.
        if (observation.gas() && TaintHelper.isAdjacentToSolidBlock(level, source)) {
            return source;
        }

        // Open-air Gas rises, so project its accumulated pollution back down onto the first solid
        // surface beneath it instead of requiring the cloud itself to remain ground-level.
        BlockPos.MutableBlockPos cursor = source.mutable();
        int minY = Math.max(level.getMinBuildHeight(), source.getY() - GAS_SURFACE_SEARCH_DEPTH);
        while (cursor.getY() > minY) {
            cursor.move(0, -1, 0);
            BlockPos above = cursor.above();
            BlockState floor = level.getBlockState(cursor);
            BlockState space = level.getBlockState(above);
            if (!floor.isAir()
                    && floor.getFluidState().isEmpty()
                    && !floor.canBeReplaced()
                    && (space.isAir() || space.canBeReplaced() || PhysicalFlux.isPhysicalFlux(space))
                    && TaintHelper.isAdjacentToSolidBlock(level, above)) {
                return above.immutable();
            }
        }
        return null;
    }
}
