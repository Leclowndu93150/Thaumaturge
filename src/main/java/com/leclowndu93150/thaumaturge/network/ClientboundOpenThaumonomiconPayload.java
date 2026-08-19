package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundOpenThaumonomiconPayload() implements CustomPacketPayload {
    public static final ClientboundOpenThaumonomiconPayload INSTANCE = new ClientboundOpenThaumonomiconPayload();

    public static final Type<ClientboundOpenThaumonomiconPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "open_thaumonomicon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenThaumonomiconPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
