package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CrimsonPortalFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CLEARANCE = 3;

    public CrimsonPortalFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos.MutableBlockPos ground = context.origin().mutable().move(Direction.DOWN);
        BlockState groundState = level.getBlockState(ground);
        if (!groundState.isFaceSturdy(level, ground, Direction.UP)) {
            return false;
        }
        for (int i = 1; i <= CLEARANCE; i++) {
            BlockState above = level.getBlockState(ground.above(i));
            if (!above.isAir()) {
                return false;
            }
        }
        BlockPos spawn = ground.above();
        EntityCultistPortalLesser portal = TCEntities.CULTIST_PORTAL_LESSER.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (portal == null) {
            return false;
        }
        portal.setPersistenceRequired();
        portal.snapTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0.0F, 0.0F);
        portal.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), EntitySpawnReason.STRUCTURE, null);
        level.addFreshEntityWithPassengers(portal);
        return true;
    }
}
