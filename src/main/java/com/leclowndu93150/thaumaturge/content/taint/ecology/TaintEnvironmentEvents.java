package com.leclowndu93150.thaumaturge.content.taint.ecology;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.network.ClientboundTaintEnvironmentPayload;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TaintEnvironmentEvents {
    private static final int SYNC_INTERVAL = 10;
    private static final int BIOME_BLEND_RADIUS = 12;
    private static final int BIOME_CENTER_WEIGHT = 4;
    private static final int[][] BIOME_SAMPLES = {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private static final float NATURAL_TAINT_AMBIENCE = 0.35F;
    private static final float CHANGED_TAINT_AMBIENCE = 0.55F;
    private static final float FUME_THRESHOLD = 0.3F;
    private static final float FUME_CHANCE_PER_TICK = 0.12F;
    private static final int FUME_COLOR = 0xD0751891;
    private static final float FUME_SCALE = 0.7F;
    private static final double FUME_RISE = 0.01;
    private static final int FUME_RANGE_XZ = 17;
    private static final int FUME_RANGE_Y = 7;
    private static final int FUME_DROP_Y = 2;
    private static final float SEVERE_ECOLOGY = 0.85F;
    private static final int CRAWLER_INTERVAL = 600;
    private static final int CRAWLER_CHANCE = 4;
    private static final double CRAWLER_SPACING = 32.0;
    private static final int CRAWLER_RANGE = 25;
    private static final float FULL_TURN = 360.0F;

    private TaintEnvironmentEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % SYNC_INTERVAL != 0) {
            return;
        }
        ServerLevel level = player.level();
        BlockPos pos = player.blockPosition();
        float pressure = TaintEcology.getSaturation(level, pos);
        float ambience = Math.max(pressure, biomeAmbience(level, pos));
        PacketDistributor.sendToPlayer(player, new ClientboundTaintEnvironmentPayload(ambience));
        spawnFumes(level, player, ambience);
        if (pressure >= SEVERE_ECOLOGY && !ThaumaturgeCommonConfig.WUSS_MODE.get() && level.getDifficulty() != Difficulty.PEACEFUL && player.tickCount % CRAWLER_INTERVAL == 0
                && player.getRandom().nextInt(CRAWLER_CHANCE) == 0) {
            trySpawnAmbientCrawler(level, player);
        }
    }

    private static float biomeAmbience(ServerLevel level, BlockPos center) {
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();
        float weightedAmbience = 0.0F;
        int totalWeight = 0;
        for (int[] offset : BIOME_SAMPLES) {
            sample.set(center.getX() + offset[0] * BIOME_BLEND_RADIUS, center.getY(), center.getZ() + offset[1] * BIOME_BLEND_RADIUS);
            if (!level.hasChunkAt(sample)) {
                continue;
            }
            int weight = offset[0] == 0 && offset[1] == 0 ? BIOME_CENTER_WEIGHT : 1;
            totalWeight += weight;
            if (TaintBiomeManager.isTainted(level, sample)) {
                weightedAmbience += weight * (TaintBiomeManager.isChangedColumn(level, sample) ? CHANGED_TAINT_AMBIENCE : NATURAL_TAINT_AMBIENCE);
            }
        }
        return totalWeight == 0 ? 0.0F : weightedAmbience / totalWeight;
    }

    private static void spawnFumes(ServerLevel level, ServerPlayer player, float ambience) {
        if (ambience < FUME_THRESHOLD) {
            return;
        }
        RandomSource random = player.getRandom();
        for (int tick = 0; tick < SYNC_INTERVAL; tick++) {
            if (random.nextFloat() < ambience * FUME_CHANCE_PER_TICK) {
                Vec3 at = new Vec3(player.getX() + random.nextInt(FUME_RANGE_XZ) - FUME_RANGE_XZ / 2, player.getY() + random.nextInt(FUME_RANGE_Y) - FUME_DROP_Y,
                        player.getZ() + random.nextInt(FUME_RANGE_XZ) - FUME_RANGE_XZ / 2);
                Effects.taint(level, at).color(FUME_COLOR).scale(FUME_SCALE).motion(0.0, FUME_RISE, 0.0).send();
            }
        }
    }

    private static void trySpawnAmbientCrawler(ServerLevel level, ServerPlayer player) {
        if (!level.getEntitiesOfClass(EntityTaintCrawler.class, player.getBoundingBox().inflate(CRAWLER_SPACING)).isEmpty()) {
            return;
        }
        RandomSource random = player.getRandom();
        BlockPos sample = player.blockPosition().offset(random.nextInt(CRAWLER_RANGE) - CRAWLER_RANGE / 2, 0, random.nextInt(CRAWLER_RANGE) - CRAWLER_RANGE / 2);
        if (!level.hasChunkAt(sample)) {
            return;
        }
        BlockPos spawn = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sample);
        EntityTaintCrawler crawler = TTEntities.TAINT_CRAWLER.get().create(level, EntitySpawnReason.EVENT);
        if (crawler == null) {
            return;
        }
        crawler.snapTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, random.nextFloat() * FULL_TURN, 0.0F);
        if (level.noCollision(crawler)) {
            level.addFreshEntity(crawler);
        } else {
            crawler.discard();
        }
    }
}
