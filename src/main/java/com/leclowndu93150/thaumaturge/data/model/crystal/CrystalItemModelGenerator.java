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
import net.minecraft.world.item.Item;

public final class CrystalItemModelGenerator {
    private static final ResourceLocation PLANTER_TEXTURE = TTIds.rl("item/crystal_planter");

    private CrystalItemModelGenerator() {}

    public static void register(BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput) {
        emit(modelOutput, TTItems.CRYSTAL_AER.get());
        emit(modelOutput, TTItems.CRYSTAL_IGNIS.get());
        emit(modelOutput, TTItems.CRYSTAL_AQUA.get());
        emit(modelOutput, TTItems.CRYSTAL_TERRA.get());
        emit(modelOutput, TTItems.CRYSTAL_ORDO.get());
        emit(modelOutput, TTItems.CRYSTAL_PERDITIO.get());
        emit(modelOutput, TTItems.CRYSTAL_VITIUM.get());
    }

    private static void emit(BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, Item item) {
        ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(PLANTER_TEXTURE), modelOutput);
    }
}
