package com.leclowndu93150.thaumaturge.data.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimPatterns;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectDefinition;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

public final class TTTrimBootstrap {
    private static final String MATERIAL_ASSET_PREFIX = "thaumaturge_";
    private static final int BRASS_COLOR = 0xE8C24C;
    private static final int THAUMIUM_COLOR = 0x8A72C8;
    private static final int VOID_METAL_COLOR = 0x6B4E8C;

    public static final String CRYSTAL_DESCRIPTION_KEY = Util.makeDescriptionId("trim_material", TTIds.rl("essentia_crystal"));

    private TTTrimBootstrap() {}

    public static void materials(BootstrapContext<TrimMaterial> context) {
        material(context, TTTrimMaterials.BRASS, BRASS_COLOR);
        material(context, TTTrimMaterials.THAUMIUM, THAUMIUM_COLOR);
        material(context, TTTrimMaterials.VOID_METAL, VOID_METAL_COLOR);
        MaterialAssetGroup assets = MaterialAssetGroup.create(TTTrimMaterials.ESSENTIA_CRYSTAL_ASSET);
        for (AspectDefinition aspect : AspectBootstrap.DEFINITIONS) {
            Component description = Component.translatable(CRYSTAL_DESCRIPTION_KEY).withStyle(Style.EMPTY.withColor(aspect.color()));
            context.register(TTTrimMaterials.essentiaCrystal(aspect.key()), new TrimMaterial(assets, description));
        }
    }

    public static void patterns(BootstrapContext<TrimPattern> context) {
        TrimPatterns.register(context, TTTrimPatterns.ARCANE);
        TrimPatterns.register(context, TTTrimPatterns.OCCULT);
    }

    private static void material(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> key, int color) {
        Component description = Component.translatable(Util.makeDescriptionId("trim_material", key.identifier())).withStyle(Style.EMPTY.withColor(color));
        context.register(key, new TrimMaterial(MaterialAssetGroup.create(MATERIAL_ASSET_PREFIX + key.identifier().getPath()), description));
    }
}
