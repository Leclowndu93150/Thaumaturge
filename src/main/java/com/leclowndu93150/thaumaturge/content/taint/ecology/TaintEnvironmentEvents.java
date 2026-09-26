package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.network.ClientboundTaintEnvironmentPayload;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TaintEnvironmentEvents {
    private static final int SYNC_INTERVAL = 10;
    private static final int BIOME_BLEND_RADIUS = 12;
    private static final int[][] BIOME_SAMPLES = {
        {0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };
    private static final float NATURAL_TAINT_AMBIENCE = 0.35F;
    private static final float DYNAMIC_TAINT_AMBIENCE = 0.55F;
    private static final float SEVERE_ECOLOGY = 0.85F;

    private TaintEnvironmentEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % SYNC_INTERVAL != 0) {
            return;
        }
        float ecologyPressure = TaintEcology.getSaturation(player.serverLevel(), player.blockPosition());
        float ambience = Math.max(ecologyPressure, biomeAmbience(player.serverLevel(), player.blockPosition()));
        PacketDistributor.sendToPlayer(player, new ClientboundTaintEnvironmentPayload(ambience));
        if (ecologyPressure >= SEVERE_ECOLOGY
                && !ThaumaturgeCommonConfig.WUSS_MODE.get()
                && player.level().getDifficulty() != Difficulty.PEACEFUL
                && player.tickCount % 600 == 0
                && player.getRandom().nextInt(4) == 0) {
            trySpawnAmbientTaint(player);
        }
    }

    private static float biomeAmbience(ServerLevel level, BlockPos center) {
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();
        float weightedPressure = 0.0F;
        int totalWeight = 0;
        for (int[] offset : BIOME_SAMPLES) {
            sample.set(
                    center.getX() + offset[0] * BIOME_BLEND_RADIUS,
                    center.getY(),
                    center.getZ() + offset[1] * BIOME_BLEND_RADIUS);
            if (!level.hasChunkAt(sample)) {
                continue;
            }
            int weight = offset[0] == 0 && offset[1] == 0 ? 4 : 1;
            totalWeight += weight;
            if (TaintBiomeManager.isTainted(level, sample)) {
                weightedPressure += weight
                        * (TaintBiomeManager.isDynamicallyTainted(level, sample)
                                ? DYNAMIC_TAINT_AMBIENCE
                                : NATURAL_TAINT_AMBIENCE);
            }
        }
        return totalWeight == 0 ? 0.0F : weightedPressure / totalWeight;
    }

    private static void trySpawnAmbientTaint(ServerPlayer player) {
        var level = player.serverLevel();
        if (!level.getEntitiesOfClass(
                        EntityTaintCrawler.class, player.getBoundingBox().inflate(32.0))
                .isEmpty()) {
            return;
        }
        BlockPos sample = player.blockPosition()
                .offset(
                        player.getRandom().nextInt(25) - 12,
                        0,
                        player.getRandom().nextInt(25) - 12);
        if (!level.hasChunkAt(sample)) {
            return;
        }
        BlockPos spawn = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sample)
                .above();
        EntityTaintCrawler crawler = TCEntities.TAINT_CRAWLER.get().create(level);
        if (crawler == null) return;
        crawler.moveTo(
                spawn.getX() + 0.5,
                spawn.getY(),
                spawn.getZ() + 0.5,
                player.getRandom().nextFloat() * 360.0F,
                0.0F);
        if (level.noCollision(crawler)) {
            level.addFreshEntity(crawler);
        }
    }
}
