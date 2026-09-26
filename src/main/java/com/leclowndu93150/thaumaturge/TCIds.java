package com.leclowndu93150.thaumaturge;

import net.minecraft.resources.Identifier;

public final class TCIds {
    public static final String MODID = "thaumaturge";
    public static final String CURIOS = "curios";
    public static final String DISTANT_HORIZONS = "distanthorizons";
    public static final String IRIS = "iris";

    private TCIds() {}

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
