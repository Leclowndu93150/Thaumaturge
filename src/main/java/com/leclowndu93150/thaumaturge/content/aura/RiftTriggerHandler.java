package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class RiftTriggerHandler {
    private static final int POLL_INTERVAL_TICKS = 20;

    private RiftTriggerHandler() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % POLL_INTERVAL_TICKS != 0) {
            return;
        }
        BlockPos trigger = AuraManager.pollRiftTrigger(level);
        if (trigger != null && !ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            EntityFluxRift.createRift(level, trigger);
        }
    }
}
