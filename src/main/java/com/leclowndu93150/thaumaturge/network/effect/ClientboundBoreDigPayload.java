package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundBoreDigPayload(BlockPos target, int boreEntityId, BlockPos borePos, int delay)
        implements CustomPacketPayload {
    public static final Type<ClientboundBoreDigPayload> TYPE = new Type<>(TCIds.rl("bore_dig"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBoreDigPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ClientboundBoreDigPayload::target,
                    ByteBufCodecs.VAR_INT,
                    ClientboundBoreDigPayload::boreEntityId,
                    BlockPos.STREAM_CODEC,
                    ClientboundBoreDigPayload::borePos,
                    ByteBufCodecs.VAR_INT,
                    ClientboundBoreDigPayload::delay,
                    ClientboundBoreDigPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
