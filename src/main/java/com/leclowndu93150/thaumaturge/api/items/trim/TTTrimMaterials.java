package com.leclowndu93150.thaumaturge.api.items.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * Public addon API for essentia crystal armor trims. Every aspect can have its own trim material; an essentia
 * crystal of that aspect is the smithing ingredient.
 *
 * <p>The link from aspect to material is the {@link #CRYSTAL_MATERIAL} data map on the aspect registry. An addon
 * aspect gets a crystal trim by adding a trim material and a data map entry for it.
 *
 * <p>Crystal trim materials share the {@link #ESSENTIA_CRYSTAL_ASSET} palette, a grey ramp that is tinted with the
 * color of the material description when the trim is drawn on armor and on item icons.
 *
 * @since 1.1.0
 */
public final class TTTrimMaterials {
    /**
     * Asset name shared by every crystal trim material. Materials using it are tinted with their description color.
     */
    public static final String ESSENTIA_CRYSTAL_ASSET = "thaumaturge_essentia_crystal";

    /**
     * Data map from an aspect to the trim material its essentia crystal provides. Synced to clients.
     */
    public static final DataMapType<IAspect, Holder<TrimMaterial>> CRYSTAL_MATERIAL = DataMapType.builder(TTIds.rl("trim_material"), IAspect.REGISTRY_KEY, TrimMaterial.CODEC)
            .synced(TrimMaterial.CODEC, false).build();

    private TTTrimMaterials() {}

    /**
     * Returns the conventional key of the crystal trim material for an aspect, in the aspect's namespace.
     *
     * @param aspect the aspect key
     * @return the key {@code <namespace>:essentia_crystal/<path>} of the aspect
     */
    public static ResourceKey<TrimMaterial> essentiaCrystal(ResourceKey<IAspect> aspect) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, aspect.identifier().withPrefix("essentia_crystal/"));
    }
}
