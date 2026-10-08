package com.leclowndu93150.thaumaturge.content.aura.pressure;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

public final class CrawlerPressureEvent extends AbstractFluxPressureEvent {
    private static final String NAME = "crawler";
    private static final int WEIGHT = 2;
    private static final float COST = 10.0F;
    private static final boolean ALLOWED_NEAR_TAINT = true;
    private static final int FULL_TURN = 360;

    public CrawlerPressureEvent() {
        super(NAME, WEIGHT, COST, ALLOWED_NEAR_TAINT);
    }

    @Override
    public boolean fire(ServerLevel level, BlockPos origin, FluxPressureState state) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, origin);
        EntityTaintCrawler crawler = TTEntities.TAINT_CRAWLER.get().create(level);
        if (crawler == null) {
            return false;
        }
        crawler.moveTo(
                surface.getX() + 0.5,
                surface.getY(),
                surface.getZ() + 0.5,
                level.getRandom().nextInt(FULL_TURN),
                0.0F);
        if (!level.noCollision(crawler)) {
            crawler.discard();
            return false;
        }
        return level.addFreshEntity(crawler);
    }
}
