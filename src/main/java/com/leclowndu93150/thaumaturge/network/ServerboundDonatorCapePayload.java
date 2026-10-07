package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.donator.DonatorPerks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundDonatorCapePayload(boolean visible) implements CustomPacketPayload {
    public static final Type<ServerboundDonatorCapePayload> TYPE = new Type<>(TTIds.rl("donator_cape"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundDonatorCapePayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, ServerboundDonatorCapePayload::visible,
            ServerboundDonatorCapePayload::new);

    public static void handle(ServerboundDonatorCapePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                DonatorPerks.setCapeVisible(player, payload.visible());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
