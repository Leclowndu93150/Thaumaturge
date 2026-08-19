package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.registry.TCItems;
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

public record InfusionRecipeDisplay(SlotDisplay catalyst, List<SlotDisplay> components, AspectList aspects, int instability, SlotDisplay result) implements RecipeDisplay {
    public static final MapCodec<InfusionRecipeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(SlotDisplay.CODEC.fieldOf("catalyst").forGetter(InfusionRecipeDisplay::catalyst),
            SlotDisplay.CODEC.listOf().fieldOf("components").forGetter(InfusionRecipeDisplay::components), AspectList.CODEC.fieldOf("aspects").forGetter(InfusionRecipeDisplay::aspects),
            Codec.INT.fieldOf("instability").forGetter(InfusionRecipeDisplay::instability), SlotDisplay.CODEC.fieldOf("result").forGetter(InfusionRecipeDisplay::result))
            .apply(i, InfusionRecipeDisplay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipeDisplay> STREAM_CODEC = StreamCodec.composite(SlotDisplay.STREAM_CODEC, InfusionRecipeDisplay::catalyst,
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), InfusionRecipeDisplay::components, AspectList.STREAM_CODEC, InfusionRecipeDisplay::aspects, ByteBufCodecs.VAR_INT,
            InfusionRecipeDisplay::instability, SlotDisplay.STREAM_CODEC, InfusionRecipeDisplay::result, InfusionRecipeDisplay::new);

    @Override
    public SlotDisplay craftingStation() {
        return new SlotDisplay.ItemSlotDisplay(TCItems.INFUSION_MATRIX.get());
    }

    @Override
    public Type<InfusionRecipeDisplay> type() {
        return TCRecipeTypes.INFUSION_DISPLAY.get();
    }
}
