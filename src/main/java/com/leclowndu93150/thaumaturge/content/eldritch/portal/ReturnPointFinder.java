package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

final class ReturnPointFinder {
    private static final int RING_RADIUS = 4;
    private static final int RING_HEIGHT = 3;

    private ReturnPointFinder() {}

    static DimensionTransition find(
            ServerPlayer player, ServerLevel level, BlockPos anchor, DimensionTransition.PostDimensionTransition post) {
        Optional<BlockPos> spot = SafeSpot.near(level, anchor, RING_RADIUS, RING_HEIGHT)
                .or(() -> surface(level, anchor, ThaumaturgeServerConfig.LABYRINTH.returnSearchRadius.get()));
        if (spot.isPresent()) {
            return new DimensionTransition(
                    level, Vec3.atBottomCenterOf(spot.get()), Vec3.ZERO, player.getYRot(), player.getXRot(), post);
        }
        return player.findRespawnPositionAndUseSpawnBlock(false, post);
    }

    private static Optional<BlockPos> surface(ServerLevel level, BlockPos center, int maxRadius) {
        for (int radius = 0; radius <= maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    Optional<BlockPos> spot = SafeSpot.surface(level, center.getX() + dx, center.getZ() + dz);
                    if (spot.isPresent()) {
                        return spot;
                    }
                }
            }
        }
        return Optional.empty();
    }
}
