package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

final class LatticeAnchor {
    static final int REACH_SQR = 74;

    private static final int SETTLE_DELAY = 2;
    private static final int ACROSS_FACTOR = 4;
    private static final int SEARCH_SQR = ACROSS_FACTOR * REACH_SQR;
    private static final int SEARCH_LIMIT = 4096;
    private static final Direction[] DIRECTIONS = Direction.values();

    private LatticeAnchor() {}

    static boolean withinReach(BlockPos condenser, BlockPos cell) {
        return cell.getY() > condenser.getY() && condenser.distSqr(cell) <= REACH_SQR;
    }

    static void schedule(ScheduledTickAccess ticks, BlockPos pos, Block block) {
        ticks.scheduleTick(pos, block, SETTLE_DELAY);
    }

    static void settle(ServerLevel level, BlockPos pos) {
        if (!isLattice(level, pos)) {
            return;
        }
        Survey survey = survey(level, pos);
        BlockPos anchor = null;
        for (BlockPos condenser : survey.condensers()) {
            if (withinReach(condenser, pos) && joined(level, pos, condenser)) {
                anchor = condenser;
                break;
            }
        }
        if (anchor != null) {
            recheck(level, anchor);
        } else if (survey.complete()) {
            level.destroyBlock(pos, true);
        }
    }

    static void released(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        if (level.hasChunkAt(below) && level.getBlockState(below).is(TTBlocks.CONDENSER.get())) {
            recheck(level, below);
        }
    }

    private static void recheck(Level level, BlockPos condenser) {
        if (level.getBlockEntity(condenser) instanceof BlockEntityCondenser entity) {
            entity.triggerCheck();
        }
    }

    private static Survey survey(Level level, BlockPos origin) {
        Walk walk = walk(level, origin, cell -> origin.distSqr(cell) <= SEARCH_SQR);
        List<BlockPos> condensers = new ArrayList<>();
        boolean complete = walk.complete();
        for (BlockPos cell : walk.cells()) {
            BlockPos below = cell.below();
            if (!level.hasChunkAt(below)) {
                complete = false;
            } else if (level.getBlockState(below).is(TTBlocks.CONDENSER.get())) {
                condensers.add(below);
            }
        }
        return new Survey(condensers, complete);
    }

    private static boolean joined(Level level, BlockPos origin, BlockPos condenser) {
        return walk(level, origin, cell -> withinReach(condenser, cell)).cells().contains(condenser.above());
    }

    private static Walk walk(Level level, BlockPos origin, Predicate<BlockPos> bounds) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        boolean complete = true;
        seen.add(origin);
        pending.add(origin);
        while (!pending.isEmpty()) {
            if (seen.size() > SEARCH_LIMIT) {
                return new Walk(seen, false);
            }
            BlockPos cell = pending.poll();
            for (Direction direction : DIRECTIONS) {
                BlockPos next = cell.relative(direction);
                if (!bounds.test(next) || seen.contains(next)) {
                    continue;
                }
                if (!level.hasChunkAt(next)) {
                    complete = false;
                } else if (isLattice(level, next)) {
                    seen.add(next);
                    pending.add(next);
                }
            }
        }
        return new Walk(seen, complete);
    }

    private static boolean isLattice(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof BlockCondenserLattice;
    }

    private record Walk(Set<BlockPos> cells, boolean complete) {
    }

    private record Survey(List<BlockPos> condensers, boolean complete) {
    }
}
