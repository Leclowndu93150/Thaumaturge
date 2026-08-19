package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundGolemPressStuffPayload(BlockPos pos, byte[] stuff) implements CustomPacketPayload {
    public static final Type<ClientboundGolemPressStuffPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "golem_press_stuff"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundGolemPressStuffPayload> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, ClientboundGolemPressStuffPayload::pos,
            ByteBufCodecs.BYTE_ARRAY, ClientboundGolemPressStuffPayload::stuff, ClientboundGolemPressStuffPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
