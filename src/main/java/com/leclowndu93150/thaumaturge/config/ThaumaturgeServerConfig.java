package com.leclowndu93150.thaumaturge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumaturgeServerConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue INFERNAL_FURNACE_TURN_TO_BLAZE;
    public static final ModConfigSpec.DoubleValue LD_DROP_RATE_BOUND_1;
    public static final ModConfigSpec.DoubleValue LD_DROP_RATE_BOUND_2;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("infernal_furnace");
        INFERNAL_FURNACE_TURN_TO_BLAZE = builder.comment(
                        "Setting this to true will make the lava of the infernal furnace turn into a blaze when it is broken.")
                .define("lavaTurnIntoBlaze", true);
        builder.pop();
        builder.push("liquid_death");
        builder.comment(
                "Liquid Death will roll every aspect of the entity killed by it with the formula: floor(rand(bound1, bound2) * <aspect count>)");
        LD_DROP_RATE_BOUND_1 = builder.defineInRange("dropRateBound1", 0.1, 0.0, 1.0);
        LD_DROP_RATE_BOUND_2 = builder.defineInRange("dropRateBound2", 0.25, 0.0, 1.0);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumaturgeServerConfig() {}
}
