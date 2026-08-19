package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public record ClientboundRecipeDisplayPayload(Identifier recipeId, List<RecipeDisplay> displays) implements CustomPacketPayload {
    public static final Type<ClientboundRecipeDisplayPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "recipe_display"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRecipeDisplayPayload> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, ClientboundRecipeDisplayPayload::recipeId,
            RecipeDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundRecipeDisplayPayload::displays, ClientboundRecipeDisplayPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
