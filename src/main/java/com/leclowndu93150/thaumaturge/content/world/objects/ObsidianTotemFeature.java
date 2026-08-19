package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.decor.BlockObsidianTotem;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ObsidianTotemFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_CLEARANCE = 2;
    private static final int MAX_CLEARANCE_SCAN = 3;
    private static final int MAX_HEIGHT = 5;
    private static final int EARLY_CAP_ONE_IN = 4;
    private static final int LEAF_DESCENT_MIN_Y = 40;
    private static final int PLACE_FLAGS = 3;

    public ObsidianTotemFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos.MutableBlockPos ground = context.origin().mutable().move(Direction.DOWN);
        if (level.getBlockState(ground).is(BlockTags.LEAVES)) {
            while (!level.getBlockState(ground).is(Blocks.GRASS_BLOCK) && ground.getY() > LEAF_DESCENT_MIN_Y) {
                ground.move(Direction.DOWN);
            }
        }
        BlockState groundState = level.getBlockState(ground);
        if (groundState.is(Blocks.SNOW) || groundState.is(Blocks.SHORT_GRASS)) {
            ground.move(Direction.DOWN);
            groundState = level.getBlockState(ground);
        }
        if (!groundState.is(Blocks.GRASS_BLOCK) && !groundState.is(Blocks.SAND) && !groundState.is(Blocks.DIRT) && !groundState.is(Blocks.STONE) && !groundState.is(Blocks.NETHERRACK)) {
            return false;
        }
        int clearance = 1;
        while (isClear(level, ground.above(clearance)) && clearance < MAX_CLEARANCE_SCAN) {
            clearance++;
        }
        if (clearance < MIN_CLEARANCE) {
            return false;
        }
        BlockState shaft = TCBlocks.OBSIDIAN_TOTEM.get().defaultBlockState();
        level.setBlock(ground, TCBlocks.OBSIDIAN_TILE.get().defaultBlockState(), PLACE_FLAGS);
        int count = 1;
        boolean capped = false;
        while (!capped && count < MAX_HEIGHT && isClear(level, ground.above(count))) {
            level.setBlock(ground.above(count), shaft, PLACE_FLAGS);
            if (count > 1 && random.nextInt(EARLY_CAP_ONE_IN) == 0) {
                placeCapNode(level, ground.above(count), random);
                capped = true;
            }
            count++;
            if (count >= MAX_HEIGHT && !capped) {
                placeCapNode(level, ground.above(MAX_HEIGHT), random);
                capped = true;
            }
        }
        reshapeTotems(level, ground, MAX_HEIGHT);
        return true;
    }

    static void reshapeTotems(WorldGenLevel level, BlockPos base, int height) {
        for (int y = 0; y <= height; y++) {
            BlockPos pos = base.above(y);
            BlockState state = level.getBlockState(pos);
            if (BlockObsidianTotem.isTotem(state)) {
                level.setBlock(pos, BlockObsidianTotem.shapedState(state, level, pos), PLACE_FLAGS);
            }
        }
    }

    private static boolean isClear(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.SNOW) || state.is(Blocks.SHORT_GRASS);
    }

    private static void placeCapNode(WorldGenLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, TCBlocks.OBSIDIAN_TOTEM_CHARGED.get().defaultBlockState(), PLACE_FLAGS);
        NodeGenerator.createRandomNodeAt(level, pos, random, false, true, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
    }
}
