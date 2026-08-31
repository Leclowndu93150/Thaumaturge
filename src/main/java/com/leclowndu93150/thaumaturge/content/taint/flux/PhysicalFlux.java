package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Shared compatibility surface for physical Flux pollution.
 *
 * <p>TC4 represented much of its magical waste as finite Flux Goo and Flux Gas. Modern Thaumaturge
 * also has TC6-style numerical aura Flux. This helper deliberately covers only the physical layer so
 * machines such as the Flux Scrubber do not need to hard-code every physical Flux implementation.
 */
public final class PhysicalFlux {
    public static final int MAX_QUANTA = 8;

    private static final int SPILL_ATTEMPTS = 10;

    private PhysicalFlux() {}

    public static boolean isPhysicalFlux(BlockState state) {
        return state.is(TCBlockTags.PHYSICAL_FLUX);
    }

    public static boolean isScrubbable(BlockState state) {
        return state.is(TCBlockTags.FLUX_SCRUBBABLE);
    }

    public static int amount(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(TCBlocks.FLUX_GAS.get())) {
            return state.getValue(BlockFluxGas.AMOUNT);
        }
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty() && fluid.getType().isSame(TCFluids.FLUX_GOO_SOURCE.get())) {
            return fluid.getAmount();
        }
        return 0;
    }

    /** Reduces a physical Flux block by up to {@code amount} finite quanta. */
    public static int reduce(ServerLevel level, BlockPos pos, int amount) {
        if (amount <= 0) {
            return 0;
        }
        BlockState state = level.getBlockState(pos);
        int current = amount(level, pos);
        if (current <= 0) {
            return 0;
        }
        int removed = Math.min(current, amount);
        int remaining = current - removed;
        if (remaining <= 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return removed;
        }
        if (state.is(TCBlocks.FLUX_GAS.get())) {
            level.setBlock(pos, BlockFluxGas.gasBlockState(remaining), Block.UPDATE_CLIENTS);
            BlockFluxGas.scheduleTick(level, pos);
        } else {
            level.setBlock(pos, FluxGooFluid.gooBlockState(remaining), Block.UPDATE_CLIENTS);
            level.scheduleTick(
                    pos,
                    level.getFluidState(pos).getType(),
                    TCFluids.FLUX_GOO_SOURCE.get().getTickDelay(level));
        }
        return removed;
    }

    public static boolean placeGoo(ServerLevel level, BlockPos pos, int amount) {
        return placeGoo(level, pos, amount, Block.UPDATE_ALL);
    }

    public static boolean placeGas(ServerLevel level, BlockPos pos, int amount) {
        return placeGas(level, pos, amount, Block.UPDATE_ALL);
    }

    public static boolean placeGoo(ServerLevel level, BlockPos pos, int amount, int flags) {
        int incoming = clamp(amount);
        BlockState existing = level.getBlockState(pos);
        FluidState existingFluid = existing.getFluidState();
        if (!existingFluid.isEmpty() && existingFluid.getType().isSame(TCFluids.FLUX_GOO_SOURCE.get())) {
            if (existingFluid.getAmount() >= MAX_QUANTA) {
                return false;
            }
            int merged = Math.min(MAX_QUANTA, existingFluid.getAmount() + incoming);
            level.setBlock(pos, FluxGooFluid.gooBlockState(merged), flags);
            level.scheduleTick(
                    pos,
                    level.getFluidState(pos).getType(),
                    TCFluids.FLUX_GOO_SOURCE.get().getTickDelay(level));
            return true;
        }
        if (!canReplaceWithPhysicalFlux(existing)) {
            return false;
        }
        level.setBlock(pos, FluxGooFluid.gooBlockState(incoming), flags);
        level.scheduleTick(
                pos,
                level.getFluidState(pos).getType(),
                TCFluids.FLUX_GOO_SOURCE.get().getTickDelay(level));
        return true;
    }

    public static boolean placeGas(ServerLevel level, BlockPos pos, int amount, int flags) {
        int incoming = clamp(amount);
        BlockState existing = level.getBlockState(pos);
        if (existing.is(TCBlocks.FLUX_GAS.get())) {
            if (existing.getValue(BlockFluxGas.AMOUNT) >= MAX_QUANTA) {
                return false;
            }
            int merged = Math.min(MAX_QUANTA, existing.getValue(BlockFluxGas.AMOUNT) + incoming);
            level.setBlock(pos, BlockFluxGas.gasBlockState(merged), flags);
            BlockFluxGas.scheduleTick(level, pos);
            return true;
        }
        if (!canReplaceWithPhysicalFlux(existing)) {
            return false;
        }
        level.setBlock(pos, BlockFluxGas.gasBlockState(incoming), flags);
        BlockFluxGas.scheduleTick(level, pos);
        return true;
    }

    /**
     * Performs one TC4-style containment spill attempt near a machine.
     *
     * <p>The first choice is the block above the machine, followed by a bounded local search. Goo
     * and Gas are chosen independently for each candidate. This intentionally does not force-load
     * chunks or destroy solid blocks.
     */
    public static boolean spill(ServerLevel level, BlockPos origin, RandomSource random) {
        if (trySpillAt(level, origin.above(), random)) {
            return true;
        }
        for (int attempt = 0; attempt < SPILL_ATTEMPTS; attempt++) {
            BlockPos target = origin.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
            if (!level.hasChunkAt(target)) {
                continue;
            }
            if (trySpillAt(level, target, random)) {
                return true;
            }
        }
        return false;
    }

    private static boolean trySpillAt(ServerLevel level, BlockPos target, RandomSource random) {
        return random.nextBoolean() ? placeGas(level, target, 1) : placeGoo(level, target, 1);
    }

    private static boolean canReplaceWithPhysicalFlux(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (isPhysicalFlux(state)) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            return state.getFluidState().is(FluidTags.WATER)
                    || state.getFluidState().is(FluidTags.LAVA);
        }
        return state.canBeReplaced();
    }

    private static int clamp(int amount) {
        return Math.max(1, Math.min(MAX_QUANTA, amount));
    }
}
