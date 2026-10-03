package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundTubeVentPayload(BlockPos pos, int color) implements CustomPacketPayload {
    public static final Type<ClientboundTubeVentPayload> TYPE = new Type<>(TCIds.rl("tube_vent"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTubeVentPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ClientboundTubeVentPayload::pos, ByteBufCodecs.INT,
            ClientboundTubeVentPayload::color, ClientboundTubeVentPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
