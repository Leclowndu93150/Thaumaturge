package com.leclowndu93150.thaumaturge.content.aura.pressure;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.entity.WispEntity;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.levelgen.Heightmap;

public final class WispPressureEvent extends AbstractFluxPressureEvent {
    private static final String NAME = "wisp";
    private static final int WEIGHT = 25;
    private static final float COST = 5.0F;
    private static final boolean ALLOWED_NEAR_TAINT = true;
    private static final int HEIGHT_ABOVE_SURFACE = 5;
    private static final int VITIUM_CHANCE = 3;

    public WispPressureEvent() {
        super(NAME, WEIGHT, COST, ALLOWED_NEAR_TAINT);
    }

    @Override
    public boolean fire(ServerLevel level, BlockPos origin, FluxPressureState state) {
        BlockPos spawn = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, origin).above(HEIGHT_ABOVE_SURFACE);
        if (spawn.getY() >= level.getMaxY() || !level.hasChunkAt(spawn)) {
            return false;
        }
        WispEntity wisp = TTEntities.WISP.get().create(level, EntitySpawnReason.EVENT);
        if (wisp == null) {
            return false;
        }
        wisp.snapTo(spawn.getX() + 0.5, spawn.getY() + 0.5, spawn.getZ() + 0.5, 0.0F, 0.0F);
        if (level.getRandom().nextInt(VITIUM_CHANCE) == 0) {
            wisp.setAspect(TTAspects.VITIUM.identifier());
        }
        if (!level.noCollision(wisp)) {
            wisp.discard();
            return false;
        }
        return level.addFreshEntity(wisp);
    }
}
