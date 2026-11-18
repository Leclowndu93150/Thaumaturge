package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
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
            ServerPlayer player,
            ServerLevel level,
            BlockPos anchor,
            BlockPos worldAnchor,
            DimensionTransition.PostDimensionTransition post) {
        var frame = LevelSpaces.frameOf(level, anchor);
        Optional<Vec3> landing = Optional.empty();
        if (!frame.isWorld()) {
            landing = SafeSpot.near(
                            level,
                            anchor,
                            RING_RADIUS,
                            RING_HEIGHT,
                            pos -> frame.equals(LevelSpaces.frameOf(level, pos))
                                    && LevelSpaces.loadedChunk(level, pos) != null)
                    .map(pos -> frame.toWorld(Vec3.atBottomCenterOf(pos)));
        }
        if (landing.isEmpty() && !LevelSpaces.isPlot(level, worldAnchor)) {
            level.getChunkAt(worldAnchor);
            landing = SafeSpot.near(level, worldAnchor, RING_RADIUS, RING_HEIGHT)
                    .or(() -> surface(level, worldAnchor, ThaumaturgeServerConfig.LABYRINTH.returnSearchRadius.get()))
                    .map(Vec3::atBottomCenterOf);
        }
        if (landing.isPresent()) {
            return new DimensionTransition(level, landing.get(), Vec3.ZERO, player.getYRot(), player.getXRot(), post);
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
