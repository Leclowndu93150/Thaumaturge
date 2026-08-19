package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public record ClientboundItemRecipePayload(Identifier recipeId, List<RecipeDisplay> displays) implements CustomPacketPayload {
    public static final Type<ClientboundItemRecipePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "item_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundItemRecipePayload> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, ClientboundItemRecipePayload::recipeId,
            RecipeDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundItemRecipePayload::displays, ClientboundItemRecipePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
