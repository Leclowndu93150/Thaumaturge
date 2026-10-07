package com.leclowndu93150.thaumaturge.content.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class TTActionBar {
    private TTActionBar() {}

    public static void sendPurple(Player player, String key, Object... args) {
        send(player, Component.translatable(key, args).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    public static void send(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(message));
        }
    }
}
