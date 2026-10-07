package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundUpdateJEIAspectListPayload() implements CustomPacketPayload {
    public static final Type<ClientboundUpdateJEIAspectListPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "update_jei_aspect_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateJEIAspectListPayload> STREAM_CODEC =
            StreamCodec.unit(new ClientboundUpdateJEIAspectListPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
