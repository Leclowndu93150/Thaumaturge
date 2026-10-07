package com.leclowndu93150.thaumaturge.data.model.crystal;

import com.google.gson.JsonElement;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;

public final class EssentiaCrystalModelGenerator {
    private static final ResourceLocation CRYSTAL_TEXTURE = TTIds.rl("item/essentia_crystal");

    private EssentiaCrystalModelGenerator() {}

    public static void register(BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput) {
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(TTItems.ESSENTIA_CRYSTAL.get()),
                TextureMapping.layer0(CRYSTAL_TEXTURE),
                modelOutput);
    }
}
