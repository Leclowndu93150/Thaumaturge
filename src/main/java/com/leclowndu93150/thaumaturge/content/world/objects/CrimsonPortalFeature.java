package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.event.EventHooks;

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
        EntityCultistPortalLesser portal =
                TTEntities.CULTIST_PORTAL_LESSER.get().create(level.getLevel());
        if (portal == null) {
            return false;
        }
        portal.setPersistenceRequired();
        portal.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0.0F, 0.0F);
        EventHooks.finalizeMobSpawn(portal, level, level.getCurrentDifficultyAt(spawn), MobSpawnType.STRUCTURE, null);
        level.addFreshEntityWithPassengers(portal);
        return true;
    }
}
