package com.leclowndu93150.thaumaturge.content.aura.pressure;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class FluxPressureEvents {
    private static final double STACK_SUPPRESSION_RANGE_SQ = 32.0 * 32.0;
    private static final float COST_EPSILON = 0.001F;
    private static final int CHUNK_WIDTH = 16;

    private FluxPressureEvents() {}

    public static boolean isEnabled() {
        return ThaumaturgeCommonConfig.FLUX_PRESSURE_EVENTS.get() && !ThaumaturgeCommonConfig.WUSS_MODE.get();
    }

    public static void queue(ServerLevel level, ChunkPos chunkPos) {
        if (isEnabled()) {
            level.getData(TTAttachments.FLUX_PRESSURE).queue(new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ()));
        }
    }

    public static boolean trigger(ServerLevel level, BlockPos origin, FluxPressureEvent event) {
        if (!isEnabled()) {
            return false;
        }
        FluxPressureState state = level.getData(TTAttachments.FLUX_PRESSURE);
        if (!event.allowedNearTaint() && (TaintHelper.isNearTaintSeed(level, origin) || state.hasRainNear(origin, STACK_SUPPRESSION_RANGE_SQ))) {
            return false;
        }
        if (AuraHelper.drainFlux(level, origin, event.cost(), true) + COST_EPSILON < event.cost() || !event.fire(level, origin, state)) {
            return false;
        }
        AuraHelper.drainFlux(level, origin, event.cost(), false);
        return true;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        FluxPressureState state = level.getExistingDataOrNull(TTAttachments.FLUX_PRESSURE.get());
        if (state == null) {
            return;
        }
        BlockPos pending = state.pollPending();
        if (pending != null) {
            RandomSource random = level.getRandom();
            trigger(level, level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pending.offset(random.nextInt(CHUNK_WIDTH), 0, random.nextInt(CHUNK_WIDTH))), FluxPressureEventTypes.choose(random));
        }
        state.tick(level);
    }
}
