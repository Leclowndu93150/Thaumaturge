package com.leclowndu93150.thaumaturge.debug.network;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundToggleRaycastDebugPayload() implements CustomPacketPayload {
    public static final ClientboundToggleRaycastDebugPayload INSTANCE = new ClientboundToggleRaycastDebugPayload();

    public static final Type<ClientboundToggleRaycastDebugPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "debug/toggle_raycast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundToggleRaycastDebugPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
