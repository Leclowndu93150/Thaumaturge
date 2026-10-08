package com.leclowndu93150.thaumaturge.content.aura.pressure;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public final class LightningPressureEvent extends AbstractFluxPressureEvent {
    private static final String NAME = "lightning";
    private static final int WEIGHT = 5;
    private static final float COST = 25.0F;
    private static final boolean ALLOWED_NEAR_TAINT = false;
    private static final double TARGET_RANGE_XZ = 4.0;
    private static final double TARGET_RANGE_Y = 16.0;
    private static final int MIN_REFLASHES = 1;
    private static final int REFLASH_SPREAD = 3;

    public LightningPressureEvent() {
        super(NAME, WEIGHT, COST, ALLOWED_NEAR_TAINT);
    }

    @Override
    public boolean fire(ServerLevel level, BlockPos origin, FluxPressureState state) {
        BlockPos strike = findTarget(level, origin);
        if (!level.hasChunkAt(strike) || !level.canSeeSky(strike)) {
            return false;
        }
        RandomSource random = level.getRandom();
        FluxLightning.flash(level, strike, true);
        state.addLightning(new FluxLightning(strike, MIN_REFLASHES + random.nextInt(REFLASH_SPREAD), random));
        return true;
    }

    private static BlockPos findTarget(ServerLevel level, BlockPos origin) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, origin);
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(surface).inflate(TARGET_RANGE_XZ, TARGET_RANGE_Y, TARGET_RANGE_XZ),
                entity -> entity.isAlive() && level.canSeeSky(entity.blockPosition()));
        return entities.isEmpty()
                ? surface
                : entities.get(level.getRandom().nextInt(entities.size())).blockPosition();
    }
}
