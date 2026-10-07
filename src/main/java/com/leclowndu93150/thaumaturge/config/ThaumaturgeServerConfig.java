package com.leclowndu93150.thaumaturge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumaturgeServerConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue INFERNAL_FURNACE_TURN_TO_BLAZE;
    public static final ModConfigSpec.IntValue SPELL_MAX_ENTITIES_PER_TICK;
    public static final ModConfigSpec.IntValue SPELL_MAX_BLOCKS_PER_TICK;
    public static final ModConfigSpec.IntValue SPELL_MAX_NODES_PER_RUN;
    public static final ModConfigSpec.IntValue SPELL_MAX_DELAYED_PER_LEVEL;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("infernal_furnace");
        INFERNAL_FURNACE_TURN_TO_BLAZE = builder.comment("Setting this to true will make the lava of the infernal furnace turn into a blaze when it is broken.").define("lavaTurnIntoBlaze", true);
        builder.pop();
        builder.push("spells");
        SPELL_MAX_ENTITIES_PER_TICK = builder.comment("How many entities one cast may affect per tick, counting everything it spawned.").defineInRange("maxEntitiesPerTick", 48, 1, 1024);
        SPELL_MAX_BLOCKS_PER_TICK = builder.comment("How many blocks one cast may affect per tick, counting everything it spawned.").defineInRange("maxBlocksPerTick", 96, 1, 4096);
        SPELL_MAX_NODES_PER_RUN = builder.comment("How many spell nodes one cast step may run before the rest is dropped.").defineInRange("maxNodesPerRun", 256, 16, 4096);
        SPELL_MAX_DELAYED_PER_LEVEL = builder.comment("How many delayed spell continuations a level holds at once.").defineInRange("maxDelayedPerLevel", 256, 16, 4096);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumaturgeServerConfig() {}
}
