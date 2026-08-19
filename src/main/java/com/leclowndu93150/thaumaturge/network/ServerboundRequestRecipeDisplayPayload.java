package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundRequestRecipeDisplayPayload(Identifier recipeId) implements CustomPacketPayload {
    public static final Type<ServerboundRequestRecipeDisplayPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "request_recipe_display"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestRecipeDisplayPayload> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC,
            ServerboundRequestRecipeDisplayPayload::recipeId, ServerboundRequestRecipeDisplayPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
