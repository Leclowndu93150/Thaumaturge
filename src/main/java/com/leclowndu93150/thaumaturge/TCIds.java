package com.leclowndu93150.thaumaturge;

import net.minecraft.resources.ResourceLocation;

public final class TCIds {
    public static final String MODID = "thaumaturge";
    public static final String CURIOS = "curios";
    public static final String IRIS = "iris";

    private TCIds() {}

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
