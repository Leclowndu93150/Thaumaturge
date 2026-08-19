package com.leclowndu93150.thaumaturge.debug.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;

public record ClientboundRaycastDebugPayload(HitResult result) implements CustomPacketPayload {

    public static final Type<ClientboundRaycastDebugPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "debug/raycast_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRaycastDebugPayload> STREAM_CODEC = StreamCodec.composite(HitResultStreamCodecs.HIT_RESULT,
            ClientboundRaycastDebugPayload::result, ClientboundRaycastDebugPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
