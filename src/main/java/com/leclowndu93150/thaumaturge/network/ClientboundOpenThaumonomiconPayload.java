package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundOpenThaumonomiconPayload() implements CustomPacketPayload {
    public static final ClientboundOpenThaumonomiconPayload INSTANCE = new ClientboundOpenThaumonomiconPayload();

    public static final Type<ClientboundOpenThaumonomiconPayload> TYPE = new Type<>(TCIds.rl("open_thaumonomicon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenThaumonomiconPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
