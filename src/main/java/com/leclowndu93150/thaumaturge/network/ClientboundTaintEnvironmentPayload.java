package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundTaintEnvironmentPayload(float pressure) implements CustomPacketPayload {
    public static final Type<ClientboundTaintEnvironmentPayload> TYPE = new Type<>(TTIds.rl("taint_environment"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTaintEnvironmentPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, ClientboundTaintEnvironmentPayload::pressure,
            ClientboundTaintEnvironmentPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
