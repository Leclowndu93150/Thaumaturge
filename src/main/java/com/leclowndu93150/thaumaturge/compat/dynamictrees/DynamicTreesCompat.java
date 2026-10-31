package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.dtteam.dynamictrees.event.RegistryEvent;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;

public final class DynamicTreesCompat {
    private DynamicTreesCompat() {}

    public static void init(IEventBus modBus) {
        NeoForgeRegistryHandler.setup(TTIds.MODID, modBus);
        modBus.addListener(DynamicTreesCompat::onGenFeatureRegistry);
    }

    public static void onGenFeatureRegistry(RegistryEvent<GenFeature> event) {
        if (event.isEntryOfType(GenFeature.class)) {
            event.getRegistry().register(new ShimmerleafGenFeature(TTIds.DT_SHIMMERLEAF_FEATURE));
            event.getRegistry().register(new TreeFootprintGenFeature(TTIds.DT_TREE_FOOTPRINT_FEATURE));
        }
    }

    public static boolean supportsManaPod(LevelReader level, BlockPos branchPos) {
        BlockState state = level.getBlockState(branchPos);
        return TreeHelper.getBranchOpt(state).filter(branch -> branch.getRadius(state) >= 1).isPresent();
    }

    public static int manaPodLift(LevelReader level, BlockPos branchPos) {
        BlockState state = level.getBlockState(branchPos);
        return TreeHelper.getBranchOpt(state).map(branch -> switch (branch.getRadius(state)) {
            case 1 -> 4;
            case 2 -> 3;
            case 3 -> 2;
            default -> 1;
        }).orElse(0);
    }
}
