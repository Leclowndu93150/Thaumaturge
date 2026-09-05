package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeLocationIndex;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.content.entity.WispEntity;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.content.warp.WarpManager;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * TC5-style pressure outlets for heavily polluted aura chunks.
 *
 * <p>Thaumaturge keeps TC6 Flux Rifts as the primary catastrophic outlet. These events restore the
 * smaller TC5 anomalies that used to turn numerical Flux back into visible world pollution and
 * creature hazards. Event costs and weights come from TC5 {@code TaintEvents}; costs are drained
 * only after an event finds a valid target and succeeds.
 */
@EventBusSubscriber(modid = TCIds.MODID)
public final class FluxPressureEvents {
    private static final int RAIN_RADIUS = 16;
    private static final int RAIN_POOL_INTERVAL = 20;
    private static final int LIGHTNING_SCATTER_ATTEMPTS = 4;
    private static final double EFFECT_RANGE = 16.0;
    private static final double STACK_SUPPRESSION_RANGE_SQ = 32.0 * 32.0;

    private static final Map<ResourceKey<Level>, BlockPos> PENDING_EVENTS = new java.util.HashMap<>();
    private static final Map<ResourceKey<Level>, List<FluxRain>> ACTIVE_RAIN = new java.util.HashMap<>();
    private static final Map<ResourceKey<Level>, List<FluxLightning>> ACTIVE_LIGHTNING = new java.util.HashMap<>();

    private FluxPressureEvents() {}

    public enum Kind {
        WISP(25, 5, true),
        LIGHTNING(5, 25, false),
        RAIN(1, 40, false),
        CRAWLER(2, 10, true),
        WARP(5, 20, true),
        EXHAUST(5, 15, true),
        NODE_MUTATION(3, 20, true);

        private final int weight;
        private final int cost;
        private final boolean nearTaintAllowed;

        Kind(int weight, int cost, boolean nearTaintAllowed) {
            this.weight = weight;
            this.cost = cost;
            this.nearTaintAllowed = nearTaintAllowed;
        }

        public int cost() {
            return cost;
        }
    }

    /**
     * Queues one TC5-style pressure event candidate for this dimension.
     *
     * <p>TC5 stored a single trigger position per dimension and consumed it on the next 20-tick
     * server event pass. Keeping the same one-slot pressure valve prevents a large set of dirty
     * loaded chunks from firing many anomalies in the same second. Later candidates intentionally
     * replace the earlier one, matching the old map-backed trigger.
     */
    public static void queueTrigger(ServerLevel level, ChunkPos chunkPos) {
        if (!ThaumaturgeCommonConfig.FLUX_PRESSURE_EVENTS.get() || ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }
        PENDING_EVENTS.put(level.dimension(), new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ()));
    }

    private static boolean tryPending(ServerLevel level, BlockPos chunkOrigin) {
        RandomSource random = level.getRandom();
        Kind kind = choose(random);
        BlockPos surface = randomSurface(level, chunkOrigin, 16);
        return trigger(level, surface, kind);
    }

    /** Debug/test hook that executes the real event validation and Flux cost path. */
    public static boolean trigger(ServerLevel level, BlockPos origin, Kind kind) {
        if (!ThaumaturgeCommonConfig.FLUX_PRESSURE_EVENTS.get() || ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return false;
        }
        if (!kind.nearTaintAllowed && nearTaintOrMajorEvent(level, origin)) {
            return false;
        }
        if (AuraHelper.drainFlux(level, origin, kind.cost, true) + 0.001F < kind.cost) {
            return false;
        }

        boolean succeeded =
                switch (kind) {
                    case WISP -> spawnWisp(level, origin);
                    case LIGHTNING -> startLightning(level, origin);
                    case RAIN -> startRain(level, origin);
                    case CRAWLER -> spawnCrawler(level, origin);
                    case WARP -> warpPulse(level, origin);
                    case EXHAUST -> exhaustPulse(level, origin);
                    case NODE_MUTATION -> mutateNode(level, origin);
                };
        if (succeeded) {
            AuraHelper.drainFlux(level, origin, kind.cost, false);
        }
        return succeeded;
    }

    private static Kind choose(RandomSource random) {
        int total = 0;
        for (Kind kind : Kind.values()) {
            total += kind.weight;
        }
        int roll = random.nextInt(total);
        for (Kind kind : Kind.values()) {
            roll -= kind.weight;
            if (roll < 0) {
                return kind;
            }
        }
        return Kind.WISP;
    }

    private static boolean spawnWisp(ServerLevel level, BlockPos origin) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, origin);
        BlockPos spawn = surface.above(5);
        if (spawn.getY() >= level.getMaxBuildHeight() - 1 || !level.hasChunkAt(spawn)) {
            return false;
        }
        WispEntity wisp = TCEntities.WISP.get().create(level);
        if (wisp == null) {
            return false;
        }
        wisp.moveTo(spawn.getX() + 0.5, spawn.getY() + 0.5, spawn.getZ() + 0.5, 0.0F, 0.0F);
        if (level.getRandom().nextInt(3) == 0) {
            wisp.setAspect(TCAspects.VITIUM.location());
        }
        if (!level.noCollision(wisp)) {
            wisp.discard();
            return false;
        }
        return level.addFreshEntity(wisp);
    }

    private static boolean spawnCrawler(ServerLevel level, BlockPos origin) {
        BlockPos surface =
                level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, origin).above();
        EntityTaintCrawler crawler = TCEntities.TAINT_CRAWLER.get().create(level);
        if (crawler == null) {
            return false;
        }
        crawler.moveTo(
                surface.getX() + 0.5,
                surface.getY(),
                surface.getZ() + 0.5,
                level.getRandom().nextInt(360),
                0.0F);
        if (!level.noCollision(crawler)) {
            crawler.discard();
            return false;
        }
        return level.addFreshEntity(crawler);
    }

    private static boolean warpPulse(ServerLevel level, BlockPos origin) {
        List<ServerPlayer> targets =
                level.getEntitiesOfClass(ServerPlayer.class, new AABB(origin).inflate(EFFECT_RANGE));
        if (targets.isEmpty()) {
            return false;
        }
        for (ServerPlayer player : targets) {
            WarpManager.sendActionBar(player, "warp.thaumaturge.fluxevent.1");
            if (level.getRandom().nextFloat() < 0.25F) {
                WarpHelper.addWarp(player, 1, WarpType.NORMAL);
            } else {
                WarpHelper.addWarp(player, 2 + level.getRandom().nextInt(4), WarpType.TEMPORARY);
            }
        }
        return true;
    }

    private static boolean exhaustPulse(ServerLevel level, BlockPos origin) {
        List<LivingEntity> targets =
                level.getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(EFFECT_RANGE));
        if (targets.isEmpty()) {
            return false;
        }
        for (LivingEntity target : targets) {
            if (target instanceof ServerPlayer player) {
                WarpManager.sendActionBar(player, "warp.thaumaturge.fluxevent.2");
            }
            target.addEffect(new MobEffectInstance(TCMobEffects.INFECTIOUS_VIS_EXHAUST, 3000, 2, false, true, false));
        }
        return true;
    }

    private static boolean mutateNode(ServerLevel level, BlockPos origin) {
        Optional<BlockPos> targetPos = NodeLocationIndex.get(level).findNearestAny(origin, EFFECT_RANGE);
        if (targetPos.isEmpty() || !(level.getBlockEntity(targetPos.get()) instanceof BlockEntityNode node)) {
            return false;
        }
        NodeType next;
        if (level.getRandom().nextBoolean()) {
            next = NodeType.TAINTED;
        } else {
            NodeType[] candidates = {
                NodeType.NORMAL, NodeType.UNSTABLE, NodeType.DARK, NodeType.TAINTED, NodeType.HUNGRY
            };
            next = candidates[level.getRandom().nextInt(candidates.length)];
        }
        node.setNodeType(next);
        node.nodeChange();
        return true;
    }

    private static boolean startRain(ServerLevel level, BlockPos origin) {
        BlockPos center =
                level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, origin).above(20);
        if (center.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        int lifespan = (30 + level.getRandom().nextInt(10)) * 20;
        ACTIVE_RAIN
                .computeIfAbsent(level.dimension(), ignored -> new ArrayList<>())
                .add(new FluxRain(center, lifespan));
        return true;
    }

    private static boolean startLightning(ServerLevel level, BlockPos origin) {
        BlockPos strike = findLightningTarget(level, origin);
        if (!level.hasChunkAt(strike) || !level.canSeeSky(strike)) {
            return false;
        }
        flashLightning(level, strike, true);
        int reflashes = 1 + level.getRandom().nextInt(3);
        ACTIVE_LIGHTNING
                .computeIfAbsent(level.dimension(), ignored -> new ArrayList<>())
                .add(new FluxLightning(strike, reflashes, 4 + level.getRandom().nextInt(5)));
        return true;
    }

    private static BlockPos findLightningTarget(ServerLevel level, BlockPos origin) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, origin);
        AABB column = new AABB(surface).inflate(4.0, 16.0, 4.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class, column, entity -> entity.isAlive() && level.canSeeSky(entity.blockPosition()));
        if (!entities.isEmpty()) {
            return entities.get(level.getRandom().nextInt(entities.size())).blockPosition();
        }
        return surface;
    }

    private static void flashLightning(ServerLevel level, BlockPos strike, boolean scatter) {
        RandomSource random = level.getRandom();
        level.playSound(
                null,
                strike,
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.WEATHER,
                4.0F,
                0.9F + random.nextFloat() * 0.2F);
        level.playSound(
                null,
                strike,
                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.WEATHER,
                2.0F,
                0.5F + random.nextFloat() * 0.2F);
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                strike.getX() + 0.5,
                strike.getY() + 0.8,
                strike.getZ() + 0.5,
                24,
                0.5,
                1.0,
                0.5,
                0.12);
        placeLightningGoo(level, strike);
        if (scatter) {
            for (int i = 0; i < LIGHTNING_SCATTER_ATTEMPTS; i++) {
                BlockPos target = strike.offset(random.nextInt(5) - 2, random.nextInt(5) - 2, random.nextInt(5) - 2);
                placeLightningGoo(level, target);
            }
        }
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(strike).inflate(3.0))) {
            target.hurt(level.damageSources().magic(), 3.0F);
            target.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, 1200, 0, false, true, false));
        }
    }

    private static void placeLightningGoo(ServerLevel level, BlockPos target) {
        if (!level.hasChunkAt(target)) {
            return;
        }
        BlockPos placedAt = target;
        if (!PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA)) {
            placedAt = target.above();
            if (!PhysicalFlux.placeGoo(level, placedAt, PhysicalFlux.MAX_QUANTA)) {
                return;
            }
        }
        TaintEcology.addPressure(level, placedAt, 0.04F);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pending = PENDING_EVENTS.remove(level.dimension());
        if (pending != null && !ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            tryPending(level, pending);
        }
        tickRain(level);
        tickLightning(level);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PENDING_EVENTS.remove(level.dimension());
            ACTIVE_RAIN.remove(level.dimension());
            ACTIVE_LIGHTNING.remove(level.dimension());
        }
    }

    private static void tickRain(ServerLevel level) {
        List<FluxRain> rains = ACTIVE_RAIN.get(level.dimension());
        if (rains == null || rains.isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        Iterator<FluxRain> iterator = rains.iterator();
        while (iterator.hasNext()) {
            FluxRain rain = iterator.next();
            rain.remainingTicks--;
            if (rain.remainingTicks <= 0) {
                iterator.remove();
                continue;
            }
            if (rain.remainingTicks % 5 == 0) {
                level.sendParticles(
                        ParticleTypes.WITCH,
                        rain.center.getX() + 0.5,
                        rain.center.getY() + 0.25,
                        rain.center.getZ() + 0.5,
                        6,
                        12.0,
                        2.0,
                        12.0,
                        0.02);
            }
            if (rain.remainingTicks % RAIN_POOL_INTERVAL != 0) {
                continue;
            }
            int x = rain.center.getX() + random.nextInt(RAIN_RADIUS * 2 + 1) - RAIN_RADIUS;
            int z = rain.center.getZ() + random.nextInt(RAIN_RADIUS * 2 + 1) - RAIN_RADIUS;
            BlockPos sample = new BlockPos(x, rain.center.getY(), z);
            if (!level.hasChunkAt(sample)) {
                continue;
            }
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, sample);
            BlockPos target = level.getBlockState(surface).canBeReplaced() ? surface : surface.above();
            if (TaintHelper.isNearTaintSeed(level, target)) {
                continue;
            }
            if (AuraHelper.drainFlux(level, target, 1.0F, true) < 0.999F) {
                rain.remainingTicks = Math.max(0, rain.remainingTicks - 20);
                continue;
            }
            if (PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA)) {
                AuraHelper.drainFlux(level, target, 1.0F, false);
                TaintEcology.addPressure(level, target, 0.02F);
            }
        }
        if (rains.isEmpty()) {
            ACTIVE_RAIN.remove(level.dimension());
        }
    }

    private static void tickLightning(ServerLevel level) {
        List<FluxLightning> bolts = ACTIVE_LIGHTNING.get(level.dimension());
        if (bolts == null || bolts.isEmpty()) {
            return;
        }
        Iterator<FluxLightning> iterator = bolts.iterator();
        while (iterator.hasNext()) {
            FluxLightning bolt = iterator.next();
            if (--bolt.delay > 0) {
                continue;
            }
            flashLightning(level, bolt.strike, false);
            bolt.remainingFlashes--;
            if (bolt.remainingFlashes <= 0) {
                iterator.remove();
            } else {
                bolt.delay = 4 + level.getRandom().nextInt(5);
            }
        }
        if (bolts.isEmpty()) {
            ACTIVE_LIGHTNING.remove(level.dimension());
        }
    }

    private static boolean nearTaintOrMajorEvent(ServerLevel level, BlockPos origin) {
        if (TaintHelper.isNearTaintSeed(level, origin)) {
            return true;
        }
        List<FluxRain> rains = ACTIVE_RAIN.get(level.dimension());
        if (rains != null) {
            for (FluxRain rain : rains) {
                if (rain.center.distSqr(origin) <= STACK_SUPPRESSION_RANGE_SQ) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BlockPos randomSurface(ServerLevel level, BlockPos origin, int width) {
        RandomSource random = level.getRandom();
        BlockPos sample = origin.offset(random.nextInt(width), 0, random.nextInt(width));
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, sample);
    }

    private static final class FluxRain {
        private final BlockPos center;
        private int remainingTicks;

        private FluxRain(BlockPos center, int remainingTicks) {
            this.center = center.immutable();
            this.remainingTicks = remainingTicks;
        }
    }

    private static final class FluxLightning {
        private final BlockPos strike;
        private int remainingFlashes;
        private int delay;

        private FluxLightning(BlockPos strike, int remainingFlashes, int delay) {
            this.strike = strike.immutable();
            this.remainingFlashes = remainingFlashes;
            this.delay = delay;
        }
    }
}
