package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.ITaintOrigin;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

public final class TaintFeeding {
    private static final int CHUNK_CENTER = 8;
    private static final int GROWTH_EXTRA_LIMIT = 3;
    private static final float GROWTH_FILL_FACTOR = 3.0F;

    private TaintFeeding() {}

    public static float reserveCap() {
        return ThaumaturgeCommonConfig.TAINT_RESERVE_CAP.get().floatValue();
    }

    public static boolean feed(ServerLevel level, BlockPos at, ITaintOrigin origin, float seconds) {
        float room = reserveCap() - origin.getTaintReserve();
        if (room <= 0.0F) {
            return true;
        }
        if (TaintBlooms.isProtected(level, at)) {
            return false;
        }
        BlockPos richest = richestChunk(level, at);
        if (richest == null) {
            return false;
        }
        float bite =
                Math.min(room, ThaumaturgeCommonConfig.TAINT_FEED_RATE.get().floatValue() * seconds);
        float eaten = AuraHelper.drainFlux(level, richest, bite, false);
        if (eaten <= 0.0F) {
            return false;
        }
        origin.setTaintReserve(origin.getTaintReserve() + eaten);
        return true;
    }

    public static int growthAttempts(ITaintOrigin origin) {
        float fill = origin.getTaintReserve() / reserveCap();
        return 1 + Math.min(GROWTH_EXTRA_LIMIT, (int) Math.floor(fill * GROWTH_FILL_FACTOR));
    }

    public static boolean isDormant(ITaintOrigin origin) {
        return origin.getTaintReserve() < TaintOrigins.growthCost();
    }

    private static @Nullable BlockPos richestChunk(ServerLevel level, BlockPos at) {
        int radius = ThaumaturgeCommonConfig.TAINT_FEED_RADIUS.get();
        int centerX = SectionPos.blockToSectionCoord(at.getX());
        int centerZ = SectionPos.blockToSectionCoord(at.getZ());
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        float bestFlux = 0.0F;
        for (int chunkX = centerX - radius; chunkX <= centerX + radius; chunkX++) {
            for (int chunkZ = centerZ - radius; chunkZ <= centerZ + radius; chunkZ++) {
                probe.set(
                        SectionPos.sectionToBlockCoord(chunkX, CHUNK_CENTER),
                        at.getY(),
                        SectionPos.sectionToBlockCoord(chunkZ, CHUNK_CENTER));
                if (!level.hasChunkAt(probe)) {
                    continue;
                }
                float flux = AuraHelper.getFlux(level, probe);
                if (flux > bestFlux) {
                    bestFlux = flux;
                    best = probe.immutable();
                }
            }
        }
        return best;
    }
}
