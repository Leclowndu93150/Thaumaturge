package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class DeviceGate {
    private DeviceGate() {}

    public static boolean passes(Player player, ResourceLocation research) {
        if (KnowledgeAccess.of(player).isResearchComplete(research)) {
            return true;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.translatable("message.thaumaturge.device.not_understood")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
        }
        return false;
    }
}
