package com.leclowndu93150.thaumaturge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumaturgeServerConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue INFERNAL_FURNACE_TURN_TO_BLAZE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("infernal_furnace");
        INFERNAL_FURNACE_TURN_TO_BLAZE = builder.comment("Setting this to true will make the lava of the infernal furnace turn into a blaze when it is broken.").define("lavaTurnIntoBlaze", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumaturgeServerConfig() {}
}
