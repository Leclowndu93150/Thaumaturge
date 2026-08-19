package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndex;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundAspectIndexPayload(AspectIndex index) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientboundAspectIndexPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "aspect_index"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAspectIndexPayload> STREAM_CODEC = AspectIndex.STREAM_CODEC.map(ClientboundAspectIndexPayload::new,
            ClientboundAspectIndexPayload::index);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
