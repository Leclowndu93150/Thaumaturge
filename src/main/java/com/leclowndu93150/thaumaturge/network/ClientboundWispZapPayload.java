package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundWispZapPayload(int sourceId, int targetId) implements CustomPacketPayload {
    public static final Type<ClientboundWispZapPayload> TYPE = new Type<>(TCIds.rl("wisp_zap"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWispZapPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundWispZapPayload::sourceId,
            ByteBufCodecs.VAR_INT, ClientboundWispZapPayload::targetId, ClientboundWispZapPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
