package com.leclowndu93150.thaumaturge.content.equipment.hover;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class HoverEvents {
    private static final int DISRUPTION_CHECK_TICKS = 20;
    private static final float AIRBORNE_MINING_PENALTY = 5.0F;

    private HoverEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !HoverManager.isHovering(player)) {
            return;
        }
        if (!HoverManager.isWearingHoverGear(player)) {
            HoverManager.setHovering(player, false);
            return;
        }
        if (player.level().dimension() == OuterLands.DIMENSION && !player.isCreative() && player.tickCount % DISRUPTION_CHECK_TICKS == 0) {
            HoverManager.setHovering(player, false);
            player.sendSystemMessage(Component.translatable("message.thaumaturge.hover_disrupted").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!player.onGround() && HoverManager.isHovering(player)) {
            event.setNewSpeed(event.getOriginalSpeed() * AIRBORNE_MINING_PENALTY);
        }
    }
}
