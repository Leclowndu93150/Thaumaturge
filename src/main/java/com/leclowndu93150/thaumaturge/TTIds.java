package com.leclowndu93150.thaumaturge;

import net.minecraft.resources.ResourceLocation;

public final class TTIds {
    public static final ResourceLocation ELDRITCH_CRESCENT = rl("eldritch_crescent");
    public static final ResourceLocation ELDRITCH_SIGIL = rl("eldritch_sigil");
    public static final ResourceLocation ELDRITCH_NOVA = rl("eldritch_nova");
    public static final ResourceLocation ELDRITCH_HAMMER = rl("eldritch_hammer");
    public static final ResourceLocation ELDRITCH_REND = rl("eldritch_rend");
    public static final ResourceLocation LABYRINTH_LAYOUT_RANDOM = rl("labyrinth_layout");
    public static final ResourceLocation RESEARCH_OCULUS = rl("oculus");
    public static final ResourceLocation LABYRINTH_STAMP_RANDOM = rl("labyrinth_stamp");
    public static final ResourceLocation LABYRINTH_SCALING = rl("labyrinth_scaling");
    public static final String MODID = "thaumaturge";
    public static final String CURIOS = "curios";
    public static final String DISTANT_HORIZONS = "distanthorizons";
    public static final String FTB_LIBRARY = "ftblibrary";
    public static final String IRIS = "iris";
    public static final String DYNAMIC_TREES = "dynamictrees";
    public static final ResourceLocation DYNAMIC_TREES_RESOURCE_PACK = rl("resourcepacks/dynamictrees");

    private TTIds() {}

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
