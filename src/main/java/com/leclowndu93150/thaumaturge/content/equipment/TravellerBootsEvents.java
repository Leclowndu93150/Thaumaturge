package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TravellerBootsEvents {
    private TravellerBootsEvents() {}

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() == EquipmentSlot.FEET && event.getEntity() instanceof ServerPlayer player && event.getFrom().getItem() instanceof TravellerBootsItem
                && !(event.getTo().getItem() instanceof TravellerBootsItem)) {
            TravellerBootsItem.clearMovementBoosts(player);
        }
    }
}
