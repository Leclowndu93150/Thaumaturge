package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import com.leclowndu93150.thaumaturge.registry.TCTreePlacers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class ScatteredFlowersDecorator extends TreeDecorator {
    public static final MapCodec<ScatteredFlowersDecorator> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("flower").forGetter(decorator -> decorator.flower)).apply(instance, ScatteredFlowersDecorator::new));

    private static final int ATTEMPTS = 18;
    private static final int SPREAD = 8;
    private static final int RISE = 4;

    private final Block flower;

    public ScatteredFlowersDecorator(Block flower) {
        this.flower = flower;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TCTreePlacers.SCATTERED_FLOWERS.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        if (context.logs().isEmpty()) {
            return;
        }
        BlockPos foot = trunkFoot(context);
        RandomSource random = context.random();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            BlockPos spot = foot.offset(random.nextInt(SPREAD) - random.nextInt(SPREAD), random.nextInt(RISE) - random.nextInt(RISE), random.nextInt(SPREAD) - random.nextInt(SPREAD));
            if (context.isAir(spot) && context.checkBlock(spot.below(), state -> state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.SAND))) {
                context.setBlock(spot, flower.defaultBlockState());
            }
        }
    }

    private static BlockPos trunkFoot(TreeDecorator.Context context) {
        Map<BlockPos, Integer> heights = new HashMap<>();
        Map<BlockPos, Integer> floors = new HashMap<>();
        for (BlockPos log : context.logs()) {
            BlockPos column = new BlockPos(log.getX(), 0, log.getZ());
            heights.merge(column, 1, Integer::sum);
            floors.merge(column, log.getY(), Math::min);
        }
        BlockPos trunk = heights.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
        return trunk.atY(floors.get(trunk));
    }
}
