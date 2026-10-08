package com.leclowndu93150.thaumaturge.config;

import com.leclowndu93150.thaumaturge.config.labyrinth.LabyrinthConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumaturgeServerConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue INFERNAL_FURNACE_TURN_TO_BLAZE;
    public static final ModConfigSpec.DoubleValue LD_DROP_RATE_BOUND_1;
    public static final ModConfigSpec.DoubleValue LD_DROP_RATE_BOUND_2;

    public static final ModConfigSpec.IntValue SPELL_MAX_ENTITIES_PER_TICK;
    public static final ModConfigSpec.IntValue SPELL_MAX_BLOCKS_PER_TICK;
    public static final ModConfigSpec.IntValue SPELL_MAX_NODES_PER_RUN;
    public static final ModConfigSpec.IntValue SPELL_MAX_DELAYED_PER_LEVEL;

    public static final LabyrinthConfig LABYRINTH;

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
        builder.push("spells");
        SPELL_MAX_ENTITIES_PER_TICK = builder.comment(
                        "How many entities one cast may affect per tick, counting everything it spawned.")
                .defineInRange("maxEntitiesPerTick", 48, 1, 1024);
        SPELL_MAX_BLOCKS_PER_TICK = builder.comment(
                        "How many blocks one cast may affect per tick, counting everything it spawned.")
                .defineInRange("maxBlocksPerTick", 96, 1, 4096);
        SPELL_MAX_NODES_PER_RUN = builder.comment(
                        "How many spell nodes one cast step may run before the rest is dropped.")
                .defineInRange("maxNodesPerRun", 256, 16, 4096);
        SPELL_MAX_DELAYED_PER_LEVEL = builder.comment("How many delayed spell continuations a level holds at once.")
                .defineInRange("maxDelayedPerLevel", 256, 16, 4096);
        builder.pop();
        LABYRINTH = new LabyrinthConfig(builder);
        SPEC = builder.build();
    }

    private ThaumaturgeServerConfig() {}
}
