package com.leclowndu93150.thaumaturge;

import net.minecraft.resources.Identifier;

public final class TCIds {
    public static final Identifier ELDRITCH_CRESCENT = rl("eldritch_crescent");
    public static final Identifier ELDRITCH_SIGIL = rl("eldritch_sigil");
    public static final Identifier ELDRITCH_NOVA = rl("eldritch_nova");
    public static final Identifier ELDRITCH_HAMMER = rl("eldritch_hammer");
    public static final Identifier ELDRITCH_REND = rl("eldritch_rend");
    public static final String MODID = "thaumaturge";
    public static final String CURIOS = "curios";
    public static final String DISTANT_HORIZONS = "distanthorizons";
    public static final String IRIS = "iris";
    public static final String DYNAMIC_TREES = "dynamictrees";
    public static final Identifier DYNAMIC_TREES_RESOURCE_PACK = rl("resourcepacks/dynamictrees");

    private TCIds() {}

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
