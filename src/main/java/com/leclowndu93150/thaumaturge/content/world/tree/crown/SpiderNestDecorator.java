package com.leclowndu93150.thaumaturge.content.world.tree.crown;

import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public final class SpiderNestDecorator extends TreeDecorator {
    public static final MapCodec<SpiderNestDecorator> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(decorator -> decorator.probability),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("log").forGetter(decorator -> decorator.log),
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("leaves").forGetter(decorator -> decorator.leaves)).apply(instance, SpiderNestDecorator::new));

    private static final int WEB_BOX_HALF_WIDTH = 7;
    private static final int WEB_BOX_HEIGHT = 10;
    private static final int WEB_DENSITY = 50;
    private static final int CHEST_DEPTH = 2;
    private static final Direction[] DIRECTIONS = Direction.values();

    private final float probability;
    private final Block log;
    private final Block leaves;

    public SpiderNestDecorator(float probability, Block log, Block leaves) {
        this.leaves = leaves;
        this.log = log;
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TTTreePlacers.SPIDER_NEST.get();
    }

    @Override
    public void place(Context context) {
        ObjectArrayList<BlockPos> logs = context.logs();
        RandomSource random = context.random();
        if (logs.isEmpty() || random.nextFloat() >= probability) {
            return;
        }
        BlockPos foot = findFoot(logs);
        BlockPos base = context.checkBlock(foot, state -> state.is(log)) ? foot : foot.above();
        BlockPos spawnerPos = base.below();
        context.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState());
        if (context.level().getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(EntityType.CAVE_SPIDER, random);
        }
        scatterWebs(context, base);
        placeLootChest(context, base.below(CHEST_DEPTH));
    }

    private void scatterWebs(Context context, BlockPos base) {
        RandomSource random = context.random();
        BlockState web = Blocks.COBWEB.defaultBlockState();
        for (int attempt = 0; attempt < WEB_DENSITY; attempt++) {
            BlockPos spot = base.offset(rollHorizontal(random), random.nextInt(WEB_BOX_HEIGHT), rollHorizontal(random));
            if (context.isAir(spot) && touchesAnchor(context, spot)) {
                context.setBlock(spot, web);
            }
        }
    }

    private static void placeLootChest(Context context, BlockPos pos) {
        context.setBlock(pos, Blocks.CHEST.defaultBlockState());
        RandomizableContainer.setBlockEntityLootTable(context.level(), context.random(), pos, BuiltInLootTables.SIMPLE_DUNGEON);
    }

    private static int rollHorizontal(RandomSource random) {
        return random.nextInt(2 * WEB_BOX_HALF_WIDTH + 1) - WEB_BOX_HALF_WIDTH;
    }

    private boolean touchesAnchor(Context context, BlockPos pos) {
        return Arrays.stream(DIRECTIONS).anyMatch(direction -> context.checkBlock(pos.relative(direction), this::isAnchor));
    }

    private boolean isAnchor(BlockState state) {
        return state.is(leaves) || state.is(log);
    }

    private static BlockPos findFoot(ObjectArrayList<BlockPos> logs) {
        int lowest = logs.get(0).getY();
        BlockPos foot = null;
        for (BlockPos candidate : logs) {
            if (candidate.getY() != lowest) {
                continue;
            }
            if (foot == null || candidate.getX() < foot.getX() || (candidate.getX() == foot.getX() && candidate.getZ() < foot.getZ())) {
                foot = candidate;
            }
        }
        return foot;
    }
}
