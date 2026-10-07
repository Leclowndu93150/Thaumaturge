package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class EquipmentEvents {
    private EquipmentEvents() {}

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!event.getSource().is(DamageTypeTags.IS_FALL) || !(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!player.getItemBySlot(EquipmentSlot.FEET).is(TTItems.TRAVELLER_BOOTS.get())) {
            return;
        }
        float reduced = Math.max(0.0F, event.getAmount() / 2.0F - 1.0F);
        if (reduced < 1.0F) {
            event.setCanceled(true);
        } else {
            event.setAmount(reduced);
        }
    }

    @SubscribeEvent
    public static void onVanillaGameEvent(VanillaGameEvent event) {
        if (event.getVanillaEvent().is(GameEvent.ITEM_INTERACT_FINISH.key())
                && event.getCause() instanceof LivingEntity living
                && living.getUseItem().getItem() instanceof ElementalSwordItem) {
            event.setCanceled(true);
        }
    }
}
