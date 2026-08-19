package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record MultiblockRecipeDisplay(Identifier blueprint, SlotDisplay result) implements RecipeDisplay {
    public static final MapCodec<MultiblockRecipeDisplay> MAP_CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Identifier.CODEC.fieldOf("blueprint").forGetter(MultiblockRecipeDisplay::blueprint), SlotDisplay.CODEC.fieldOf("result").forGetter(MultiblockRecipeDisplay::result))
                    .apply(i, MultiblockRecipeDisplay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiblockRecipeDisplay> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, MultiblockRecipeDisplay::blueprint,
            SlotDisplay.STREAM_CODEC, MultiblockRecipeDisplay::result, MultiblockRecipeDisplay::new);

    @Override
    public SlotDisplay craftingStation() {
        return new SlotDisplay.ItemSlotDisplay(TCItems.SALIS_MUNDUS.get());
    }

    @Override
    public Type<MultiblockRecipeDisplay> type() {
        return TCRecipeTypes.MULTIBLOCK_DISPLAY.get();
    }
}
