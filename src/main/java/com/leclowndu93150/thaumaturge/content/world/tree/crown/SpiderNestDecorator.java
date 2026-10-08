package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public final class SpiderNestDecorator extends TreeDecorator {
    public static final MapCodec<SpiderNestDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(decorator -> decorator.probability),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("log").forGetter(decorator -> decorator.log),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("leaves").forGetter(decorator -> decorator.leaves))
            .apply(instance, SpiderNestDecorator::new));

    private static final int WEB_ATTEMPTS = 50;
    private static final int WEB_SPREAD = 7;
    private static final int WEB_HEIGHT = 10;

    private final float probability;
    private final Block log;
    private final Block leaves;

    public SpiderNestDecorator(float probability, Block log, Block leaves) {
        this.probability = probability;
        this.log = log;
        this.leaves = leaves;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TTTreePlacers.SPIDER_NEST.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        if (!(context.level() instanceof WorldGenLevel level)) {
            return;
        }
        RandomSource random = context.random();
        if (context.logs().isEmpty() || random.nextFloat() >= probability) {
            return;
        }
        int lowest = context.logs().getFirst().getY();
        BlockPos foot = context.logs().stream()
                .filter(pos -> pos.getY() == lowest)
                .min(Comparator.comparingInt(Vec3i::getX).thenComparingInt(Vec3i::getZ))
                .orElseThrow();
        BlockPos spawnerPos = foot.below();
        context.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState());
        if (!(level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner)) {
            return;
        }
        spawner.setEntityId(EntityType.CAVE_SPIDER, random);
        for (int attempt = 0; attempt < WEB_ATTEMPTS; attempt++) {
            BlockPos web = new BlockPos(
                    foot.getX() - WEB_SPREAD + random.nextInt(WEB_SPREAD * 2),
                    foot.getY() + random.nextInt(WEB_HEIGHT),
                    foot.getZ() - WEB_SPREAD + random.nextInt(WEB_SPREAD * 2));
            if (context.isAir(web) && clingsToTree(context, web)) {
                context.setBlock(web, Blocks.COBWEB.defaultBlockState());
            }
        }
        BlockPos chestPos = foot.below(2);
        context.setBlock(chestPos, Blocks.CHEST.defaultBlockState());
        RandomizableContainer.setBlockEntityLootTable(level, random, chestPos, BuiltInLootTables.SIMPLE_DUNGEON);
    }

    private boolean clingsToTree(TreeDecorator.Context context, BlockPos web) {
        for (Direction direction : Direction.values()) {
            if (context.level()
                    .isStateAtPosition(web.relative(direction), state -> state.is(log) || state.is(leaves))) {
                return true;
            }
        }
        return false;
    }
}
