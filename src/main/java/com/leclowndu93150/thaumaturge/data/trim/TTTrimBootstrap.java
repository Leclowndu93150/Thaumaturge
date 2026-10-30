package com.leclowndu93150.thaumaturge.data.trim;

import com.leclowndu93150.thaumaturge.api.items.trim.TTTrimMaterials;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public final class TTTrimBootstrap {
    private static final String MATERIAL_ASSET_PREFIX = "thaumaturge_";
    private static final int BRASS_COLOR = 0xE8C24C;
    private static final int THAUMIUM_COLOR = 0x8A72C8;
    private static final int VOID_METAL_COLOR = 0x6B4E8C;

    private TTTrimBootstrap() {}

    public static void materials(BootstrapContext<TrimMaterial> context) {
        material(context, TTTrimMaterials.BRASS, BRASS_COLOR);
        material(context, TTTrimMaterials.THAUMIUM, THAUMIUM_COLOR);
        material(context, TTTrimMaterials.VOID_METAL, VOID_METAL_COLOR);
    }

    private static void material(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> key, int color) {
        Component description = Component.translatable(Util.makeDescriptionId("trim_material", key.identifier())).withStyle(Style.EMPTY.withColor(color));
        context.register(key, new TrimMaterial(MaterialAssetGroup.create(MATERIAL_ASSET_PREFIX + key.identifier().getPath()), description));
    }
}
