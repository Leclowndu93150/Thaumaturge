package com.leclowndu93150.thaumaturge.content.equipment.runic;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class RunicShieldingEvents {
    private static final float EFFECT_VOLUME = 0.66F;

    private RunicShieldingEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.isSpectator()) {
            RunicShielding.tick(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.getAbsorptionAmount() <= 0.0F || player.getData(TCAttachments.RUNIC_SHIELD.get()).maxCharge <= 0) {
            return;
        }
        player.level().playSound(null, player.blockPosition(), TCSounds.RUNICSHIELDEFFECT.get(), SoundSource.PLAYERS, EFFECT_VOLUME, 1.1F + player.getRandom().nextFloat() * 0.1F);
    }
}
