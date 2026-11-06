package com.leclowndu93150.thaumaturge.data.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimPatterns;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectDefinition;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;

public final class TTTrimBootstrap {
    public static final String CRYSTAL_DESCRIPTION_KEY =
            Util.makeDescriptionId("trim_material", TTIds.rl("essentia_crystal"));
    private static final String MATERIAL_ASSET_PREFIX = "thaumaturge_";
    private static final int BRASS_COLOR = 0xE8C24C;
    private static final int THAUMIUM_COLOR = 0x8A72C8;
    private static final int VOID_METAL_COLOR = 0x6B4E8C;
    private static final float BRASS_MODEL_INDEX = 0.031F;
    private static final float THAUMIUM_MODEL_INDEX = 0.032F;
    private static final float VOID_METAL_MODEL_INDEX = 0.033F;
    private static final float CRYSTAL_MODEL_INDEX = 0.034F;

    public static final List<ItemModelGenerators.TrimModelData> MATERIAL_MODELS = List.of(
            new ItemModelGenerators.TrimModelData(assetName(TTTrimMaterials.BRASS), BRASS_MODEL_INDEX, Map.of()),
            new ItemModelGenerators.TrimModelData(assetName(TTTrimMaterials.THAUMIUM), THAUMIUM_MODEL_INDEX, Map.of()),
            new ItemModelGenerators.TrimModelData(
                    assetName(TTTrimMaterials.VOID_METAL), VOID_METAL_MODEL_INDEX, Map.of()),
            new ItemModelGenerators.TrimModelData(
                    TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET, CRYSTAL_MODEL_INDEX, Map.of()));

    private TTTrimBootstrap() {}

    public static void materials(BootstrapContext<TrimMaterial> context) {
        material(context, TTTrimMaterials.BRASS, TTItems.INGOT_BRASS.get(), BRASS_COLOR, BRASS_MODEL_INDEX);
        material(context, TTTrimMaterials.THAUMIUM, TTItems.INGOT_THAUMIUM.get(), THAUMIUM_COLOR, THAUMIUM_MODEL_INDEX);
        material(
                context,
                TTTrimMaterials.VOID_METAL,
                TTItems.INGOT_VOID.get(),
                VOID_METAL_COLOR,
                VOID_METAL_MODEL_INDEX);
        for (AspectDefinition aspect : AspectBootstrap.DEFINITIONS) {
            Component description =
                    Component.translatable(CRYSTAL_DESCRIPTION_KEY).withStyle(Style.EMPTY.withColor(aspect.color()));
            context.register(
                    TTTrimMaterials.essentiaCrystal(aspect.key()),
                    TrimMaterial.create(
                            TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET,
                            TTItems.ESSENTIA_CRYSTAL.get(),
                            CRYSTAL_MODEL_INDEX,
                            description,
                            Map.of()));
        }
    }

    public static void patterns(BootstrapContext<TrimPattern> context) {
        TrimPatterns.register(context, TTItems.ARCANE_ARMOR_TRIM_SMITHING_TEMPLATE.get(), TTTrimPatterns.ARCANE);
        TrimPatterns.register(context, TTItems.OCCULT_ARMOR_TRIM_SMITHING_TEMPLATE.get(), TTTrimPatterns.OCCULT);
    }

    private static void material(
            BootstrapContext<TrimMaterial> context,
            ResourceKey<TrimMaterial> key,
            Item ingredient,
            int color,
            float itemModelIndex) {
        Component description = Component.translatable(Util.makeDescriptionId("trim_material", key.location()))
                .withStyle(Style.EMPTY.withColor(color));
        context.register(key, TrimMaterial.create(assetName(key), ingredient, itemModelIndex, description, Map.of()));
    }

    private static String assetName(ResourceKey<TrimMaterial> key) {
        return MATERIAL_ASSET_PREFIX + key.location().getPath();
    }
}
