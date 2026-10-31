package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.dtteam.dynamictrees.systems.genfeature.context.PreGenerationContext;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public final class TreeFootprintGenFeature extends GenFeature {
    public TreeFootprintGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {}

    @Override
    protected BlockPos preGenerate(GenFeatureConfiguration configuration, PreGenerationContext context) {
        BlockPos root = context.pos();
        if (!context.isWorldGen()) {
            return root;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                pos.setWithOffset(root, dx, 0, dz);
                if (!context.species().isAcceptableSoilForWorldgen(context.level(), pos, context.level().getBlockState(pos))) {
                    return root.below();
                }
                for (int dy = 1; dy <= context.species().getSignalEnergy(); dy++) {
                    pos.setWithOffset(root, dx, dy, dz);
                    if (context.level().isOutsideBuildHeight(pos) || !context.level().getFluidState(pos).isEmpty() || !DynamicTreeFeature.validTreePos(context.level(), pos)) {
                        return root.below();
                    }
                }
            }
        }
        return root;
    }
}
