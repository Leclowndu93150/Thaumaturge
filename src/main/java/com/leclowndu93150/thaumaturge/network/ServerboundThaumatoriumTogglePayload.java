package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundThaumatoriumTogglePayload(BlockPos pos, ResourceLocation recipeId)
        implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundThaumatoriumTogglePayload> TYPE =
            new CustomPacketPayload.Type<>(TTIds.rl("thaumatorium_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundThaumatoriumTogglePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerboundThaumatoriumTogglePayload::pos,
                    ResourceLocation.STREAM_CODEC,
                    ServerboundThaumatoriumTogglePayload::recipeId,
                    ServerboundThaumatoriumTogglePayload::new);

    @Override
    public CustomPacketPayload.Type<ServerboundThaumatoriumTogglePayload> type() {
        return TYPE;
    }
}
