package com.leclowndu93150.thaumaturge.content.recipe.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record CrucibleRecipeDisplay(SlotDisplay catalyst, AspectList aspects, SlotDisplay result) implements RecipeDisplay {
    public static final MapCodec<CrucibleRecipeDisplay> MAP_CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(SlotDisplay.CODEC.fieldOf("catalyst").forGetter(CrucibleRecipeDisplay::catalyst), AspectList.CODEC.fieldOf("aspects").forGetter(CrucibleRecipeDisplay::aspects),
                    SlotDisplay.CODEC.fieldOf("result").forGetter(CrucibleRecipeDisplay::result)).apply(i, CrucibleRecipeDisplay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipeDisplay> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, CrucibleRecipeDisplay::catalyst,
            AspectList.STREAM_CODEC, CrucibleRecipeDisplay::aspects, SlotDisplay.STREAM_CODEC, CrucibleRecipeDisplay::result, CrucibleRecipeDisplay::new);

    @Override
    public SlotDisplay craftingStation() {
        return new SlotDisplay.ItemSlotDisplay(TCItems.CRUCIBLE.get());
    }

    @Override
    public Type<CrucibleRecipeDisplay> type() {
        return TCRecipeTypes.CRUCIBLE_DISPLAY.get();
    }
}
