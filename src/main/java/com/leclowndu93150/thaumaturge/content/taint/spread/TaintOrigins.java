package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.api.taint.ITaintOrigin;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class TaintOrigins {
    private static final double ENTITY_SEARCH_INFLATE = 1.0;
    private static final double FRINGE_INNER_FRACTION = 0.8;

    private TaintOrigins() {}

    public static double claimRadius() {
        return ThaumaturgeCommonConfig.TAINT_SPREAD_AREA.get();
    }

    public static float growthCost() {
        return ThaumaturgeCommonConfig.TAINT_GROWTH_COST.get().floatValue();
    }

    public static void register(ServerLevel level, BlockPos anchor) {
        TaintOriginRegistry.get(level).add(anchor);
    }

    public static void unregister(ServerLevel level, BlockPos anchor) {
        TaintOriginRegistry.get(level).remove(anchor);
    }

    public static @Nullable ITaintOrigin claimingOrigin(ServerLevel level, BlockPos pos) {
        return nearest(level, pos, Float.NEGATIVE_INFINITY);
    }

    public static @Nullable ITaintOrigin payingOrigin(ServerLevel level, BlockPos pos, float cost) {
        return nearest(level, pos, cost);
    }

    public static boolean isSustained(ServerLevel level, BlockPos pos) {
        return isNaturalTaint(level, pos) || claimingOrigin(level, pos) != null;
    }

    public static boolean isNaturalTaint(ServerLevel level, BlockPos pos) {
        return TaintBiomeManager.isTainted(level, pos) && !TaintBiomeManager.isChangedColumn(level, pos);
    }

    public static boolean isOnFringe(ServerLevel level, BlockPos pos) {
        TaintOriginRegistry registry = TaintOriginRegistry.get(level);
        double outer = claimRadius();
        double outerSq = outer * outer;
        double innerSq = outerSq * FRINGE_INNER_FRACTION * FRINGE_INNER_FRACTION;
        int reach = Mth.ceil(outer);
        for (int chunkX = SectionPos.blockToSectionCoord(pos.getX() - reach);
                chunkX <= SectionPos.blockToSectionCoord(pos.getX() + reach);
                chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(pos.getZ() - reach);
                    chunkZ <= SectionPos.blockToSectionCoord(pos.getZ() + reach);
                    chunkZ++) {
                List<BlockPos> bucket = registry.inChunk(chunkX, chunkZ);
                if (bucket == null) {
                    continue;
                }
                for (BlockPos anchor : bucket) {
                    double distSq = anchor.distSqr(pos);
                    if (distSq > innerSq && distSq < outerSq) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static @Nullable ITaintOrigin nearest(ServerLevel level, BlockPos pos, float minReserve) {
        TaintOriginRegistry registry = TaintOriginRegistry.get(level);
        double radius = claimRadius();
        double bestSq = radius * radius;
        int reach = Mth.ceil(radius);
        ITaintOrigin best = null;
        List<BlockPos> stale = null;
        for (int chunkX = SectionPos.blockToSectionCoord(pos.getX() - reach);
                chunkX <= SectionPos.blockToSectionCoord(pos.getX() + reach);
                chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(pos.getZ() - reach);
                    chunkZ <= SectionPos.blockToSectionCoord(pos.getZ() + reach);
                    chunkZ++) {
                List<BlockPos> bucket = registry.inChunk(chunkX, chunkZ);
                if (bucket == null) {
                    continue;
                }
                for (BlockPos anchor : bucket) {
                    double distSq = anchor.distSqr(pos);
                    if (distSq > bestSq || !isLoaded(level, anchor)) {
                        continue;
                    }
                    ITaintOrigin origin = live(level, anchor);
                    if (origin == null) {
                        stale = stale == null ? new ArrayList<>() : stale;
                        stale.add(anchor);
                    } else if (origin.getTaintReserve() >= minReserve) {
                        best = origin;
                        bestSq = distSq;
                    }
                }
            }
        }
        if (stale != null) {
            for (BlockPos anchor : stale) {
                registry.remove(anchor);
            }
        }
        return best;
    }

    private static boolean isLoaded(ServerLevel level, BlockPos anchor) {
        return level.hasChunkAt(anchor) && level.areEntitiesLoaded(ChunkPos.asLong(anchor));
    }

    private static @Nullable ITaintOrigin live(ServerLevel level, BlockPos anchor) {
        if (level.getBlockEntity(anchor) instanceof ITaintOrigin origin && origin.isLiveTaintOrigin()) {
            return origin;
        }
        List<Entity> found = level.getEntities(
                (Entity) null, new AABB(anchor).inflate(ENTITY_SEARCH_INFLATE), TaintOrigins::isLiveEntity);
        return found.isEmpty() ? null : (ITaintOrigin) found.getFirst();
    }

    private static boolean isLiveEntity(Entity entity) {
        return entity instanceof ITaintOrigin origin && origin.isLiveTaintOrigin();
    }
}
