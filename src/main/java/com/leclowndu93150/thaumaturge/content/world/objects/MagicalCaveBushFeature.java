package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class MagicalCaveBushFeature extends Feature<NoneFeatureConfiguration> {
    private static final int PLACE_FLAGS = 2;
    private static final Direction[] LEAF_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };
    private static final BlockState LEAVES =
            Blocks.OAK_LEAVES.defaultBlockState().setValue(BlockStateProperties.DISTANCE, 1);

    public MagicalCaveBushFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos logPos = context.origin();
        if (!level.getBlockState(logPos).isAir()
                || !level.getBlockState(logPos.below()).is(TCBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)) {
            return false;
        }

        BlockPos upperLogPos = logPos.above();
        BlockPos topLeafPos = upperLogPos.above();
        if (!level.getBlockState(upperLogPos).isAir()
                || !level.getBlockState(topLeafPos).isAir()) {
            return false;
        }
        BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();
        for (Direction direction : LEAF_DIRECTIONS) {
            leafPos.set(upperLogPos).move(direction);
            if (!isInOriginChunk(logPos, leafPos)
                    || !level.getBlockState(leafPos).isAir()) return false;
        }

        level.setBlock(logPos, Blocks.OAK_LOG.defaultBlockState(), PLACE_FLAGS);
        level.setBlock(upperLogPos, Blocks.OAK_LOG.defaultBlockState(), PLACE_FLAGS);
        level.setBlock(topLeafPos, LEAVES, PLACE_FLAGS);
        for (Direction direction : LEAF_DIRECTIONS) {
            level.setBlock(leafPos.set(upperLogPos).move(direction), LEAVES, PLACE_FLAGS);
        }
        return true;
    }

    private static boolean isInOriginChunk(BlockPos origin, BlockPos pos) {
        return (origin.getX() >> 4) == (pos.getX() >> 4) && (origin.getZ() >> 4) == (pos.getZ() >> 4);
    }
}
