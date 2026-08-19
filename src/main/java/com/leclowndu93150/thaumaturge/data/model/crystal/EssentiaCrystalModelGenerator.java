package com.leclowndu93150.thaumaturge.data.model.crystal;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.color.CrystalAspectTint;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

public final class EssentiaCrystalModelGenerator {
    private static final Identifier CRYSTAL_TEXTURE = Identifier.fromNamespaceAndPath(TCIds.MODID, "item/essentia_crystal");

    private EssentiaCrystalModelGenerator() {}

    public static void register(ItemModelGenerators itemModels) {
        Identifier model = ModelLocationUtils.getModelLocation(TCItems.ESSENTIA_CRYSTAL.get());
        ModelTemplates.FLAT_ITEM.create(model, TextureMapping.layer0(new Material(CRYSTAL_TEXTURE)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(TCItems.ESSENTIA_CRYSTAL.get(), ItemModelUtils.tintedModel(model, new CrystalAspectTint(0xFFFFFF)));
    }
}
