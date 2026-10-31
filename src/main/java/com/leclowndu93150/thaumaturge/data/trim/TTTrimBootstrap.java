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
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;

public final class TTTrimBootstrap {
    public static final String CRYSTAL_DESCRIPTION_KEY =
            Util.makeDescriptionId("trim_material", TTIds.rl("essentia_crystal"));
    private static final float CRYSTAL_MODEL_INDEX = 0.031F;

    public static final List<ItemModelGenerators.TrimModelData> MATERIAL_MODELS =
            List.of(new ItemModelGenerators.TrimModelData(
                    TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET, CRYSTAL_MODEL_INDEX, Map.of()));

    private TTTrimBootstrap() {}

    public static void materials(BootstrapContext<TrimMaterial> context) {
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
}
