package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundRequestItemRecipePayload(Identifier itemId) implements CustomPacketPayload {
    public static final Type<ServerboundRequestItemRecipePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "request_item_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestItemRecipePayload> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC,
            ServerboundRequestItemRecipePayload::itemId, ServerboundRequestItemRecipePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
