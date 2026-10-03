package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.TCIds;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class GolemSpawnEvents {
    private GolemSpawnEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getEntity() instanceof EntityThaumaturgeGolem golem) {
            golem.restrictTo(golem.blockPosition(), EntityThaumaturgeGolem.HOME_RANGE);
            golem.updateEntityAttributes();
            event.setCanceled(true);
        }
    }
}
