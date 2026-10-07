package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundGolemPressPayload(BlockPos pos, GolemProperties props, boolean craft)
        implements CustomPacketPayload {
    public static final Type<ServerboundGolemPressPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "golem_press"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundGolemPressPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerboundGolemPressPayload::pos,
                    GolemProperties.STREAM_CODEC,
                    ServerboundGolemPressPayload::props,
                    ByteBufCodecs.BOOL,
                    ServerboundGolemPressPayload::craft,
                    ServerboundGolemPressPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
