package com.leclowndu93150.thaumaturge.content.entity.ranger;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class CrimsonPatrols {
    private static final int INTERVAL = 12000;
    private static final int INTERVAL_JITTER = 1200;
    private static final long TICKS_PER_DAY = 24000L;
    private static final long FIRST_PATROL_DAY = 5L;
    private static final double PERCENT = 100.0;
    private static final int VILLAGE_SECTIONS = 2;
    private static final int MIN_OFFSET = 24;
    private static final int OFFSET_SPREAD = 24;
    private static final int CHUNK_MARGIN = 10;
    private static final int RANGERS_PER_PATROL = 2;
    private static final int MEMBER_SPREAD = 5;
    private static final int MAX_BLOCK_LIGHT = 8;
    private static final double BLOCK_CENTER = 0.5;

    private CrimsonPatrols() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            tick(level);
        }
    }

    private static void tick(ServerLevel level) {
        double chance = ThaumaturgeCommonConfig.CRIMSON_PATROL_CHANCE.get();
        if (chance <= 0.0
                || level.getDifficulty() == Difficulty.PEACEFUL
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                || !level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return;
        }
        CrimsonPatrolSchedule schedule = CrimsonPatrolSchedule.get(level);
        long now = level.getGameTime();
        RandomSource random = level.getRandom();
        if (!schedule.isScheduled()) {
            schedule.schedule(now + INTERVAL + random.nextInt(INTERVAL_JITTER));
            return;
        }
        if (!schedule.isDue(now)) {
            return;
        }
        schedule.schedule(now + INTERVAL + random.nextInt(INTERVAL_JITTER));
        if (!level.isDay()
                || level.getDayTime() / TICKS_PER_DAY < FIRST_PATROL_DAY
                || random.nextDouble() * PERCENT >= chance) {
            return;
        }
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }
        ServerPlayer player = players.get(random.nextInt(players.size()));
        if (player.isSpectator() || level.isCloseToVillage(player.blockPosition(), VILLAGE_SECTIONS)) {
            return;
        }
        BlockPos.MutableBlockPos spot = player.blockPosition().mutable().move(offset(random), 0, offset(random));
        if (!level.hasChunksAt(
                        spot.getX() - CHUNK_MARGIN,
                        spot.getZ() - CHUNK_MARGIN,
                        spot.getX() + CHUNK_MARGIN,
                        spot.getZ() + CHUNK_MARGIN)
                || level.getBiome(spot).is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
            return;
        }
        spawnPatrol(level, spot, random, MobSpawnType.PATROL);
    }

    public static boolean spawnPatrol(ServerLevel level, BlockPos origin, RandomSource random, MobSpawnType reason) {
        boolean natural = reason == MobSpawnType.PATROL;
        BlockPos leaderSpot = natural ? surface(level, origin) : origin;
        if (!spawnMember(level, leaderSpot, random, TTEntities.CULTIST_RANGER_CAPTAIN.get(), true, reason)) {
            return false;
        }
        for (int i = 0; i < RANGERS_PER_PATROL; i++) {
            BlockPos spot = origin.offset(
                    random.nextInt(MEMBER_SPREAD) - random.nextInt(MEMBER_SPREAD),
                    0,
                    random.nextInt(MEMBER_SPREAD) - random.nextInt(MEMBER_SPREAD));
            if (!spawnMember(
                            level,
                            natural ? surface(level, spot) : spot,
                            random,
                            TTEntities.CULTIST_RANGER.get(),
                            false,
                            reason)
                    && !natural) {
                spawnMember(level, leaderSpot, random, TTEntities.CULTIST_RANGER.get(), false, reason);
            }
        }
        return true;
    }

    private static BlockPos surface(ServerLevel level, BlockPos pos) {
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
    }

    private static boolean spawnMember(
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            EntityType<? extends AbstractCultistRanger> type,
            boolean leader,
            MobSpawnType reason) {
        BlockState state = level.getBlockState(pos);
        if (!NaturalSpawner.isValidEmptySpawnBlock(level, pos, state, state.getFluidState(), type)) {
            return false;
        }
        if (reason == MobSpawnType.PATROL
                && (level.getBrightness(LightLayer.BLOCK, pos) > MAX_BLOCK_LIGHT
                        || !Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random))) {
            return false;
        }
        AbstractCultistRanger ranger = type.create(level);
        if (ranger == null) {
            return false;
        }
        ranger.setPos(pos.getX() + BLOCK_CENTER, pos.getY(), pos.getZ() + BLOCK_CENTER);
        if (leader) {
            ranger.findPatrolTarget();
        } else {
            ranger.setPatrolling(true);
        }
        ranger.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), reason, null);
        level.addFreshEntityWithPassengers(ranger);
        return true;
    }

    private static int offset(RandomSource random) {
        return (MIN_OFFSET + random.nextInt(OFFSET_SPREAD)) * (random.nextBoolean() ? -1 : 1);
    }
}
