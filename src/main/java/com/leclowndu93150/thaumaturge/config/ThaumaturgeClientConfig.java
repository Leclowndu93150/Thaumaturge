package com.leclowndu93150.thaumaturge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumaturgeClientConfig {
    public static final String DONATOR_CAPE_KEY = "donator_cape";

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue SHOW_ASPECTS_BY_DEFAULT;
    private static final ModConfigSpec.BooleanValue LARGE_TAG_TEXT;
    private static final ModConfigSpec.BooleanValue DIAL_BOTTOM;
    private static final ModConfigSpec.BooleanValue ELDRITCH_GUARDIAN_FOG;
    private static final ModConfigSpec.BooleanValue WARP_MIST_FOG;
    private static final ModConfigSpec.BooleanValue TAINT_ENVIRONMENT_FOG;
    private static final ModConfigSpec.BooleanValue TAINTED_LANDS_WATER_FOG;
    private static final ModConfigSpec.BooleanValue FLUX_GOO_FOG;
    private static final ModConfigSpec.BooleanValue OUTER_LANDS_FOG;
    // private static final ModConfigSpec.BooleanValue HIDE_RECIPES_IF_MISSING_RESEARCH;

    private static final ModConfigSpec.BooleanValue DONATOR_CAPE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SHOW_ASPECTS_BY_DEFAULT = builder.comment(
                        "When true, item aspects render on tooltips by default and Shift hides them. When false, Shift reveals them.")
                .define("tooltip.show_aspects_by_default", false);

        LARGE_TAG_TEXT = builder.comment(
                        "When true, aspect tag amount and bonus counters render at full font size. When false, they render half-scale to fit narrow GUIs.")
                .define("graphics.large_tag_text", false);

        DIAL_BOTTOM = builder.comment(
                        "When true, the caster vis dial renders at the bottom left of the screen instead of the top left.")
                .define("hud.dial_bottom", false);

        ELDRITCH_GUARDIAN_FOG = builder.comment("Show the short mist caused by nearby Eldritch Guardians.")
                .define("graphics.eldritch_guardian_fog", true);
        WARP_MIST_FOG = builder.comment("Show the mist caused by Warp events.").define("graphics.warp_mist_fog", true);
        TAINT_ENVIRONMENT_FOG = builder.comment("Show dense fog in heavily tainted areas.")
                .define("graphics.taint_environment_fog", true);
        TAINTED_LANDS_WATER_FOG = builder.comment("Show the purple underwater fog in Tainted Lands.")
                .define("graphics.tainted_lands_water_fog", true);
        FLUX_GOO_FOG = builder.comment("Show the pink fog while submerged in Flux Goo.")
                .define("graphics.flux_goo_fog", true);
        OUTER_LANDS_FOG =
                builder.comment("Show the dark fog in the Outer Lands.").define("graphics.outer_lands_fog", true);

        DONATOR_CAPE = builder.comment(
                        "Donators only: shows your Thaumaturge cape. While it is on, every player on the server sees it.")
                .define(DONATOR_CAPE_KEY, true);

        /*HIDE_RECIPES_IF_MISSING_RESEARCH = builder
        .comment("Hide recipes from JEI if you don't have the research for it")
        .define("jei.hide_recipes_without_research", false);*/

        SPEC = builder.build();
    }

    private ThaumaturgeClientConfig() {}

    public static boolean showAspectsByDefault() {
        return SHOW_ASPECTS_BY_DEFAULT.get();
    }

    public static boolean largeTagText() {
        return LARGE_TAG_TEXT.get();
    }

    public static boolean dialBottom() {
        return DIAL_BOTTOM.get();
    }

    public static boolean eldritchGuardianFog() {
        return ELDRITCH_GUARDIAN_FOG.get();
    }

    public static boolean warpMistFog() {
        return WARP_MIST_FOG.get();
    }

    public static boolean taintEnvironmentFog() {
        return TAINT_ENVIRONMENT_FOG.get();
    }

    public static boolean taintedLandsWaterFog() {
        return TAINTED_LANDS_WATER_FOG.get();
    }

    public static boolean fluxGooFog() {
        return FLUX_GOO_FOG.get();
    }

    public static boolean outerLandsFog() {
        return OUTER_LANDS_FOG.get();
    }

    /*public static boolean hideRecipesIfMissingResearch(){
        return HIDE_RECIPES_IF_MISSING_RESEARCH.get();
    }*/
    public static boolean donatorCape() {
        return DONATOR_CAPE.get();
    }
}
