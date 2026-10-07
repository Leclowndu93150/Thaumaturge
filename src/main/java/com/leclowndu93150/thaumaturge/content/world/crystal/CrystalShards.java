package com.leclowndu93150.thaumaturge.content.world.crystal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class CrystalShards {
    public static final int COUNT = 8;
    private static final List<Integer> INDICES = List.of(0, 1, 2, 3, 4, 5, 6, 7);

    private CrystalShards() {}

    public static long seed(BlockState state, BlockPos pos) {
        return RandomSource.create(state.getSeed(pos)).nextLong();
    }

    public static List<Integer> order(Direction face, long seed) {
        List<Integer> shuffled = new ArrayList<>(INDICES);
        Util.shuffle(shuffled, RandomSource.create(seed + CrystalFaceTransforms.seedOffset(face)));
        return shuffled;
    }

    public static int unsupported(long seed) {
        return (int) (seed & (COUNT - 1));
    }

    public static boolean supports(BlockGetter level, BlockPos pos, Direction face) {
        BlockPos neighbour = pos.relative(face);
        return level.getBlockState(neighbour).isFaceSturdy(level, neighbour, face.getOpposite());
    }
}
