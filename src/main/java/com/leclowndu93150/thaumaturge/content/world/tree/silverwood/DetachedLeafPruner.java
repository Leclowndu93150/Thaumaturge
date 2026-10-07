package com.leclowndu93150.thaumaturge.content.world.tree.silverwood;

import com.leclowndu93150.thaumaturge.content.world.tree.TreeLeafUpdater;
import com.leclowndu93150.thaumaturge.registry.TTTreePlacers;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class DetachedLeafPruner extends TreeDecorator {
    public static final DetachedLeafPruner INSTANCE = new DetachedLeafPruner();
    public static final MapCodec<DetachedLeafPruner> CODEC = MapCodec.unit(INSTANCE);

    private static final int UNSETTLED = LeavesBlock.DECAY_DISTANCE;

    private DetachedLeafPruner() {}

    @Override
    protected TreeDecoratorType<?> type() {
        return TTTreePlacers.DETACHED_LEAF_PRUNER.get();
    }

    @Override
    public void place(TreeDecorator.Context context) {
        Set<BlockPos> leaves = new HashSet<>(context.leaves());
        Set<BlockPos> unsettled = leaves.stream().filter(pos -> LeavesBlock.getOptionalDistanceAt(context.level().getBlockState(pos)).orElse(0) == UNSETTLED).collect(Collectors.toSet());
        TreeLeafUpdater.run(context.level(), new HashSet<>(context.logs()), leaves, unsettled);
    }
}
