package com.leclowndu93150.thaumaturge.content.donator;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class DonatorPerks {
    private static final int TITLE_COLOR = 0x9B6BD8;
    private static final int THANKS_COLOR = 0xC9A8F5;
    private static final String TITLE = "message.thaumaturge.donator.title";
    private static final String THANKS = "message.thaumaturge.donator.thanks";
    private static final String CAPE = "message.thaumaturge.donator.cape";
    private static final String SETTINGS = "message.thaumaturge.donator.settings";
    private static final String ONCE = "message.thaumaturge.donator.once";

    private DonatorPerks() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !Donators.is(player) || player.getData(TTAttachments.DONATOR_WELCOMED)) {
            return;
        }
        player.setData(TTAttachments.DONATOR_WELCOMED, true);
        player.sendSystemMessage(Component.translatable(TITLE).withStyle(style -> style.withColor(TITLE_COLOR).withBold(true)));
        player.sendSystemMessage(Component.translatable(THANKS, player.getDisplayName()).withColor(THANKS_COLOR));
        player.sendSystemMessage(Component.translatable(CAPE).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(SETTINGS).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable(ONCE).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    public static void setCapeVisible(ServerPlayer player, boolean visible) {
        if (Donators.is(player) && player.getData(TTAttachments.DONATOR_CAPE) != visible) {
            player.setData(TTAttachments.DONATOR_CAPE, visible);
        }
    }
}
