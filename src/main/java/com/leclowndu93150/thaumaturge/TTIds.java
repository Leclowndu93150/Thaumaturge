package com.leclowndu93150.thaumaturge;

import net.minecraft.resources.ResourceLocation;

public final class TTIds {
    public static final String MODID = "thaumaturge";
    public static final String CURIOS = "curios";
    public static final String IRIS = "iris";
    public static final String DISTANT_HORIZONS = "distanthorizons";

    private TTIds() {}

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
