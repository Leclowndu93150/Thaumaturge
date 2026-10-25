package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.warp.WarpManager;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

final class RiftSpawner {
    private static final int PLACEMENT_SPREAD = 16;
    private static final int NETHER_CLIMB_START = 10;
    private static final int CLIMB_MARGIN = 5;
    private static final int CEILING_MARGIN = 4;
    private static final int NO_FLOOR = Integer.MIN_VALUE;
    private static final double EXCLUSION_RADIUS = 32.0;
    private static final double NOTICE_RADIUS = 32.0;
    private static final double SIZE_FLUX_FACTOR = 3.0;
    private static final double MIN_CREATION_SIZE = 5.0;
    private static final double CELL_CENTER = 0.5;
    private static final int FULL_TURN_DEGREES = 360;
    private static final String NOTICE_RELEASE = "warp.thaumaturge.fluxevent.3";

    private RiftSpawner() {}

    static void spawnNear(ServerLevel level, BlockPos origin) {
        RandomSource random = level.getRandom();
        int z = origin.getZ() + random.nextInt(PLACEMENT_SPREAD);
        int x = origin.getX() + random.nextInt(PLACEMENT_SPREAD);
        BlockPos column = new BlockPos(x, origin.getY(), z);
        if (!level.hasChunkAt(column)) {
            return;
        }
        int ceiling = level.getMaxBuildHeight();
        int y = findFloor(level, column, ceiling);
        if (y == NO_FLOOR || y >= ceiling - CEILING_MARGIN) {
            return;
        }
        BlockPos cell = new BlockPos(x, y, z);
        double size = Math.sqrt(AuraHelper.getFlux(level, cell) * SIZE_FLUX_FACTOR);
        if (size <= MIN_CREATION_SIZE) {
            return;
        }
        AABB exclusion = new AABB(cell).inflate(EXCLUSION_RADIUS);
        if (!level.getEntitiesOfClass(EntityFluxRift.class, exclusion).isEmpty()) {
            return;
        }
        EntityFluxRift rift = TTEntities.FLUX_RIFT.get().create(level);
        if (rift == null) {
            return;
        }
        float yaw = random.nextInt(FULL_TURN_DEGREES);
        rift.reseed(random.nextInt());
        rift.moveTo(x + CELL_CENTER, y + CELL_CENTER, z + CELL_CENTER, yaw, 0.0F);
        rift.resize((int) size);
        if (!level.addFreshEntity(rift)) {
            return;
        }
        AuraHelper.drainFlux(level, cell, (float) size, false);
        notifyNearby(level, new AABB(cell).inflate(NOTICE_RADIUS));
    }

    private static int findFloor(ServerLevel level, BlockPos column, int ceiling) {
        if (level.dimensionType().hasSkyLight()) {
            return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column)
                    .getY();
        }
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(
                column.getX(), Math.max(NETHER_CLIMB_START, level.getMinBuildHeight()), column.getZ());
        while (!level.getBlockState(probe).isAir()) {
            probe.move(0, 1, 0);
            if (probe.getY() > ceiling - CLIMB_MARGIN) {
                return NO_FLOOR;
            }
        }
        return probe.getY();
    }

    private static void notifyNearby(ServerLevel level, AABB area) {
        for (ServerPlayer player : level.players()) {
            if (area.contains(player.position())) {
                WarpManager.sendActionBar(player, NOTICE_RELEASE);
            }
        }
    }
}
