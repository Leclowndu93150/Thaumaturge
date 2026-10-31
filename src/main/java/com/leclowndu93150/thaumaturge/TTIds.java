package com.leclowndu93150.thaumaturge;

import net.minecraft.resources.Identifier;

public final class TTIds {
    public static final Identifier ELDRITCH_CRESCENT = rl("eldritch_crescent");
    public static final Identifier ELDRITCH_SIGIL = rl("eldritch_sigil");
    public static final Identifier ELDRITCH_NOVA = rl("eldritch_nova");
    public static final Identifier ELDRITCH_HAMMER = rl("eldritch_hammer");
    public static final Identifier ELDRITCH_REND = rl("eldritch_rend");
    public static final Identifier LABYRINTH_LAYOUT_RANDOM = rl("labyrinth_layout");
    public static final Identifier RESEARCH_OCULUS = rl("oculus");
    public static final Identifier LABYRINTH_STAMP_RANDOM = rl("labyrinth_stamp");
    public static final Identifier LABYRINTH_SCALING = rl("labyrinth_scaling");
    public static final String MODID = "thaumaturge";
    public static final String CURIOS = "curios";
    public static final String DISTANT_HORIZONS = "distanthorizons";
    public static final String FTB_LIBRARY = "ftblibrary";
    public static final String IRIS = "iris";
    public static final String DYNAMIC_TREES = "dynamictrees";
    public static final String BIOMES_O_PLENTY = "biomesoplenty";
    public static final Identifier DYNAMIC_TREES_RESOURCE_PACK = rl("resourcepacks/dynamictrees");
    public static final Identifier DT_SILVERWOOD_BRANCH = rl("silverwood_branch");
    public static final Identifier DT_GREATWOOD_BRANCH = rl("greatwood_branch");
    public static final Identifier DT_STRIPPED_SILVERWOOD_BRANCH = rl("stripped_silverwood_branch");
    public static final Identifier DT_STRIPPED_GREATWOOD_BRANCH = rl("stripped_greatwood_branch");
    public static final Identifier DT_SILVERWOOD_LEAVES = rl("silverwood_leaves");
    public static final Identifier DT_GREATWOOD_LEAVES = rl("greatwood_leaves");
    public static final Identifier DT_SILVERWOOD_SAPLING = rl("silverwood_sapling");
    public static final Identifier DT_GREATWOOD_SAPLING = rl("greatwood_sapling");
    public static final Identifier DT_SILVERWOOD_SEED = rl("silverwood_seed");
    public static final Identifier DT_GREATWOOD_SEED = rl("greatwood_seed");
    public static final Identifier DT_SHIMMERLEAF_FEATURE = rl("shimmerleaf");
    public static final Identifier DT_TREE_FOOTPRINT_FEATURE = rl("tree_footprint");

    private TTIds() {}

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
