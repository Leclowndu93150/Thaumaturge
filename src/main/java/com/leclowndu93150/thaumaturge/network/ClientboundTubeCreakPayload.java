package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundTubeCreakPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ClientboundTubeCreakPayload> TYPE = new Type<>(TCIds.rl("tube_creak"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTubeCreakPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ClientboundTubeCreakPayload::pos,
            ClientboundTubeCreakPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
