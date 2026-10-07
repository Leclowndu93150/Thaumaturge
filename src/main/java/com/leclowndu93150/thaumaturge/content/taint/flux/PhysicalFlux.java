package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class PhysicalFlux {
    public static final int MAX_QUANTA = 8;

    private static final int SPILL_ATTEMPTS = 10;
    private static final int SPILL_SPREAD = 3;

    private PhysicalFlux() {}

    public static boolean isPhysicalFlux(BlockState state) {
        return state.is(TTBlockTags.PHYSICAL_FLUX);
    }

    public static boolean isScrubbable(BlockState state) {
        return state.is(TTBlockTags.FLUX_SCRUBBABLE);
    }

    public static int amount(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof PhysicalFluxBlock flux ? flux.fluxAmount(state) : 0;
    }

    public static int reduce(ServerLevel level, BlockPos pos, int amount) {
        BlockState state = level.getBlockState(pos);
        if (amount <= 0 || !(state.getBlock() instanceof PhysicalFluxBlock flux)) {
            return 0;
        }
        int current = flux.fluxAmount(state);
        if (current <= 0) {
            return 0;
        }
        int removed = Math.min(current, amount);
        int remaining = current - removed;
        if (remaining <= 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, flux.withFluxAmount(remaining), Block.UPDATE_CLIENTS);
            flux.scheduleFluxTick(level, pos);
        }
        return removed;
    }

    public static boolean placeGoo(ServerLevel level, BlockPos pos, int amount) {
        return place(level, pos, TTBlocks.FLUX_GOO.get(), amount);
    }

    public static boolean placeGas(ServerLevel level, BlockPos pos, int amount) {
        return place(level, pos, TTBlocks.FLUX_GAS.get(), amount);
    }

    public static boolean spill(ServerLevel level, BlockPos origin, RandomSource random) {
        if (trySpillAt(level, origin.above(), random)) {
            return true;
        }
        for (int attempt = 0; attempt < SPILL_ATTEMPTS; attempt++) {
            int dx = random.nextInt(SPILL_SPREAD) - 1;
            int dy = random.nextInt(SPILL_SPREAD) - 1;
            int dz = random.nextInt(SPILL_SPREAD) - 1;
            if (dx == 0 && dy == 0 && dz == 0) {
                continue;
            }
            BlockPos target = origin.offset(dx, dy, dz);
            if (level.hasChunkAt(target) && trySpillAt(level, target, random)) {
                return true;
            }
        }
        return false;
    }

    private static boolean place(ServerLevel level, BlockPos pos, PhysicalFluxBlock kind, int amount) {
        int incoming = Math.clamp(amount, 1, MAX_QUANTA);
        BlockState existing = level.getBlockState(pos);
        if (existing.getBlock() == kind) {
            int current = kind.fluxAmount(existing);
            if (current >= MAX_QUANTA) {
                return false;
            }
            level.setBlock(pos, kind.withFluxAmount(Math.min(MAX_QUANTA, current + incoming)), Block.UPDATE_ALL);
            kind.scheduleFluxTick(level, pos);
            return true;
        }
        if (!canReplace(existing)) {
            return false;
        }
        level.setBlock(pos, kind.withFluxAmount(incoming), Block.UPDATE_ALL);
        kind.scheduleFluxTick(level, pos);
        return true;
    }

    private static boolean trySpillAt(ServerLevel level, BlockPos target, RandomSource random) {
        BlockState existing = level.getBlockState(target);
        if (existing.getBlock() instanceof PhysicalFluxBlock flux) {
            return place(level, target, flux, 1);
        }
        return random.nextBoolean() ? placeGas(level, target, 1) : placeGoo(level, target, 1);
    }

    private static boolean canReplace(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (isPhysicalFlux(state)) {
            return false;
        }
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            return state.getBlock() instanceof LiquidBlock && (fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA));
        }
        return state.canBeReplaced();
    }
}
