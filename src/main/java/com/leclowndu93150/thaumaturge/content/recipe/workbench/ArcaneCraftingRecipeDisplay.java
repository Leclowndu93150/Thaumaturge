package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record ArcaneCraftingRecipeDisplay(int width, int height, boolean shapeless, List<SlotDisplay> ingredients, SlotDisplay result, SlotDisplay craftingStation, int visCost,
        List<SlotDisplay> crystals) implements RecipeDisplay {
    public static final MapCodec<ArcaneCraftingRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.INT.fieldOf("width").forGetter(ArcaneCraftingRecipeDisplay::width),
            Codec.INT.fieldOf("height").forGetter(ArcaneCraftingRecipeDisplay::height), Codec.BOOL.optionalFieldOf("shapeless", false).forGetter(ArcaneCraftingRecipeDisplay::shapeless),
            SlotDisplay.CODEC.listOf().fieldOf("ingredients").forGetter(ArcaneCraftingRecipeDisplay::ingredients), SlotDisplay.CODEC.fieldOf("result").forGetter(ArcaneCraftingRecipeDisplay::result),
            SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(ArcaneCraftingRecipeDisplay::craftingStation), Codec.INT.fieldOf("vis_cost").forGetter(ArcaneCraftingRecipeDisplay::visCost),
            SlotDisplay.CODEC.listOf().optionalFieldOf("crystals", List.of()).forGetter(ArcaneCraftingRecipeDisplay::crystals)).apply(i, ArcaneCraftingRecipeDisplay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArcaneCraftingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ArcaneCraftingRecipeDisplay::width,
            ByteBufCodecs.VAR_INT, ArcaneCraftingRecipeDisplay::height, ByteBufCodecs.BOOL, ArcaneCraftingRecipeDisplay::shapeless, SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ArcaneCraftingRecipeDisplay::ingredients, SlotDisplay.STREAM_CODEC, ArcaneCraftingRecipeDisplay::result, SlotDisplay.STREAM_CODEC, ArcaneCraftingRecipeDisplay::craftingStation,
            ByteBufCodecs.VAR_INT, ArcaneCraftingRecipeDisplay::visCost, SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), ArcaneCraftingRecipeDisplay::crystals, ArcaneCraftingRecipeDisplay::new);

    @Override
    public Type<ArcaneCraftingRecipeDisplay> type() {
        return TCRecipeTypes.ARCANE_DISPLAY.get();
    }
}
