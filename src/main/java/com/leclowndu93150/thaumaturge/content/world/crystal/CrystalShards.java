package com.leclowndu93150.thaumaturge.content.world.crystal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class CrystalShards {
    public static final int COUNT = 8;
    private static final long GAMMA = 0x9E3779B97F4A7C15L;
    private static final long MIX_FIRST = 0xBF58476D1CE4E5B9L;
    private static final long MIX_SECOND = 0x94D049BB133111EBL;
    private static final long UNSUPPORTED_SALT = 0x5851F42D4C957F2DL;
    private static final int SHIFT_FIRST = 30;
    private static final int SHIFT_SECOND = 27;
    private static final int SHIFT_FINAL = 31;
    private static final int UNSUPPORTED_SHIFT = Long.SIZE - 3;

    private CrystalShards() {}

    public static long seed(BlockState state, BlockPos pos) {
        return state.getSeed(pos);
    }

    public static List<Integer> order(Direction face, long seed) {
        long base = seed ^ CrystalFaceTransforms.seedOffset(face);
        long[] keys = new long[COUNT];
        List<Integer> order = new ArrayList<>(COUNT);
        for (int shard = 0; shard < COUNT; shard++) {
            long key = mix(base + (shard + 1) * GAMMA);
            int slot = shard;
            while (slot > 0 && keys[slot - 1] > key) {
                keys[slot] = keys[slot - 1];
                slot--;
            }
            keys[slot] = key;
            order.add(slot, shard);
        }
        return order;
    }

    public static int unsupported(long seed) {
        return (int) (mix(seed ^ UNSUPPORTED_SALT) >>> UNSUPPORTED_SHIFT);
    }

    public static boolean supports(BlockGetter level, BlockPos pos, Direction face) {
        BlockPos neighbour = pos.relative(face);
        return level.getBlockState(neighbour).isFaceSturdy(level, neighbour, face.getOpposite());
    }

    private static long mix(long value) {
        long mixed = value;
        mixed = (mixed ^ (mixed >>> SHIFT_FIRST)) * MIX_FIRST;
        mixed = (mixed ^ (mixed >>> SHIFT_SECOND)) * MIX_SECOND;
        return mixed ^ (mixed >>> SHIFT_FINAL);
    }
}
