package com.leclowndu93150.thaumaturge.data.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimPatterns;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectBootstrap;
import com.leclowndu93150.thaumaturge.data.worldgen.aspect.AspectDefinition;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

public final class TTTrimBootstrap {
    public static final String CRYSTAL_DESCRIPTION_KEY = Util.makeDescriptionId("trim_material", TTIds.rl("essentia_crystal"));

    private TTTrimBootstrap() {}

    public static void materials(BootstrapContext<TrimMaterial> context) {
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
}
