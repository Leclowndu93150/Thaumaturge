package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundWarpFXPayload(byte kind) implements CustomPacketPayload {
    public static final byte KIND_HEARTBEAT = 0;
    public static final byte KIND_MIST = 1;
    public static final byte KIND_MIST_SHORT = 2;

    public static final Type<ClientboundWarpFXPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "warp_fx"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWarpFXPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BYTE, ClientboundWarpFXPayload::kind,
            ClientboundWarpFXPayload::new);

    public static ClientboundWarpFXPayload heartbeat() {
        return new ClientboundWarpFXPayload(KIND_HEARTBEAT);
    }

    public static ClientboundWarpFXPayload mist() {
        return new ClientboundWarpFXPayload(KIND_MIST);
    }

    public static ClientboundWarpFXPayload mistShort() {
        return new ClientboundWarpFXPayload(KIND_MIST_SHORT);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
