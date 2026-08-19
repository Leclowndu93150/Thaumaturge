package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundInfusionSourcePayload(BlockPos matrixPos, BlockPos sourcePos) implements CustomPacketPayload {
    public static final Type<ClientboundInfusionSourcePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "fx_infusion_source"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundInfusionSourcePayload> STREAM_CODEC = StreamCodec.of((buf, data) -> {
        buf.writeBlockPos(data.matrixPos);
        buf.writeBlockPos(data.sourcePos);
    }, buf -> new ClientboundInfusionSourcePayload(buf.readBlockPos(), buf.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
