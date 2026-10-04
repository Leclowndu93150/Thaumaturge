package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundToggleHoverPayload() implements CustomPacketPayload {
    public static final ServerboundToggleHoverPayload INSTANCE = new ServerboundToggleHoverPayload();

    public static final Type<ServerboundToggleHoverPayload> TYPE = new Type<>(TCIds.rl("toggle_hover"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundToggleHoverPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(ServerboundToggleHoverPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                HoverManager.toggle(player);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
