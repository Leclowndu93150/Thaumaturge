package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class BellowsHelper {
    private static final int ADJACENT_OFFSET = 1;

    private BellowsHelper() {}

    public static int countBellows(@Nullable Level level, BlockPos pos, Direction[] directions) {
        return countBellows(level, pos, directions, ADJACENT_OFFSET);
    }

    public static int countBellows(@Nullable Level level, BlockPos pos, Direction[] directions, int offset) {
        if (level == null) {
            return 0;
        }
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        int active = 0;
        int index = directions.length;
        while (index-- > 0) {
            Direction side = directions[index];
            probe.set(pos).move(side, offset);
            active += blowsToward(level, probe, side) ? 1 : 0;
        }
        return active;
    }

    private static boolean blowsToward(Level level, BlockPos source, Direction from) {
        if (!LoadedChunks.contains(level, source)) {
            return false;
        }
        if (!(level.getBlockEntity(source) instanceof IBellowsPower bellows)) {
            return false;
        }
        return bellows.bellowsEnabled() && from.getOpposite() == bellows.bellowsFacing();
    }
}
