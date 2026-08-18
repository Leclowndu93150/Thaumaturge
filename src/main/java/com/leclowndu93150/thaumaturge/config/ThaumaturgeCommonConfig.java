package com.leclowndu93150.thaumaturge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumaturgeCommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue WUSS_MODE;
    public static final ModConfigSpec.DoubleValue TAINT_SPREAD_RATE;
    public static final ModConfigSpec.IntValue TAINT_SPREAD_AREA;
    public static final ModConfigSpec.DoubleValue ENERGIZED_NODE_VIS_PER_POINT;
    public static final ModConfigSpec.IntValue CRIMSON_PORTAL_RARITY;
    public static final ModConfigSpec.IntValue HUNGRY_NODE_BLOCK_EAT_RANGE;
    public static final ModConfigSpec.BooleanValue SCALE_HUNGRY_NODE_RANGE_BY_MODIFIER;
    public static final ModConfigSpec.IntValue HUNGRY_NODE_MINIMUM_BLOCK_EAT_RANGE;
    public static final ModConfigSpec.IntValue HUNGRY_NODE_MAXIMUM_BLOCK_EAT_RANGE;
    public static final ModConfigSpec.DoubleValue HUNGRY_NODE_BLOCK_HARDNESS;
    public static final ModConfigSpec.IntValue HUNGRY_NODE_BLOCK_EAT_INTERVAL;
    public static final ModConfigSpec.IntValue SHIELD_RECHARGE;
    public static final ModConfigSpec.IntValue SHIELD_WAIT;
    public static final ModConfigSpec.DoubleValue SHIELD_COST;
    public static final ModConfigSpec.BooleanValue ALLOW_CHAMPION_MOBS;
    public static final ModConfigSpec.BooleanValue NO_SLEEP;
    public static final ModConfigSpec.BooleanValue NO_STRESS;
    public static final ModConfigSpec.BooleanValue SHOW_GOLEM_EMOTES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("world");
        WUSS_MODE = builder.comment("Setting this to true disables Warp, Taint spread and similar mechanics. You wuss.")
                .define("wussMode", false);
        TAINT_SPREAD_RATE = builder.comment(
                        "The % chance of taint fibres spreading on a block tick. Setting this to 0 will effectively stop taint fibre spread.")
                .defineInRange("taintSpreadRate", 100.0, 0.0, 100.0);
        TAINT_SPREAD_AREA = builder.comment(
                        "The range at which taint can spread from a taint seed. This value is only a base and will be modified by flux levels.")
                .defineInRange("taintSpreadArea", 32, 1, 128);
        ENERGIZED_NODE_VIS_PER_POINT = builder.comment(
                        "Raw vis an energized node drains from the chunk aura to restore one aspect point. Normal nodes refine at 3.0 per point; higher values make energized nodes more wasteful. 0 makes their refill free.")
                .defineInRange("energizedNodeVisPerPoint", 6.0, 0.0, 100.0);
        CRIMSON_PORTAL_RARITY = builder.comment(
                        "Average number of chunks per wild lesser crimson portal. Higher is rarer. 0 disables wild portals entirely.")
                .defineInRange("crimsonPortalRarity", 500, 0, 1000000);
        HUNGRY_NODE_BLOCK_EAT_RANGE = builder.comment(
                        "Maximum length in blocks of a hungry node's random block-eating ray.",
                        "Default: 16. Range: 1 to 64. A larger area gives each attempt more possible targets; it does not guarantee a distant block will be selected.",
                        "This setting does not change entity or dropped-item pulling range.")
                .defineInRange("hungryNodeBlockEatRange", 16, 1, 64);
        SCALE_HUNGRY_NODE_RANGE_BY_MODIFIER = builder.comment(
                        "Whether a hungry node's block-eating range scales with its current modifier (quality).",
                        "False: hungryNodeBlockEatRange is always used. True: the minimum/maximum settings below override hungryNodeBlockEatRange.",
                        "Fading uses the minimum, pale and normal are evenly spaced between them, and bright uses the maximum.")
                .define("scaleHungryNodeBlockEatRangeByModifier", false);
        HUNGRY_NODE_MINIMUM_BLOCK_EAT_RANGE = builder.comment(
                        "Block-eating range of a fading hungry node when modifier scaling is enabled.",
                        "Default: 16. Range: 1 to 64. This setting does nothing while scaleHungryNodeBlockEatRangeByModifier is false.")
                .defineInRange("hungryNodeMinimumBlockEatRange", 16, 1, 64);
        HUNGRY_NODE_MAXIMUM_BLOCK_EAT_RANGE = builder.comment(
                        "Block-eating range of a bright hungry node when modifier scaling is enabled.",
                        "Default: 32. Range: 1 to 64. This overrides hungryNodeBlockEatRange while scaling is enabled.",
                        "If set below the minimum range, the minimum is used for every modifier.")
                .defineInRange("hungryNodeMaximumBlockEatRange", 32, 1, 64);
        HUNGRY_NODE_BLOCK_HARDNESS = builder.comment(
                        "Maximum block hardness a hungry node can eat. The comparison is strictly below this value, not equal to it.",
                        "Examples: dirt 0.5, stone 1.5, most logs 2, ores/deepslate 3, iron and diamond blocks 5, obsidian 50.",
                        "Default: 5.0, so it can eat ordinary terrain and ores, but not hardness-5 metal/gem blocks or obsidian.",
                        "Range: 0 to 100. 0 disables block destruction. Unbreakable blocks such as bedrock have negative hardness and are never eaten.")
                .defineInRange("hungryNodeBlockEatHardness", 5.0, 0.0, 100.0);
        HUNGRY_NODE_BLOCK_EAT_INTERVAL = builder.comment(
                        "Ticks between hungry-node block-eating attempts. Minecraft normally runs at 20 ticks per second; lower values are faster.",
                        "Examples: 1 = 20 attempts/second, 20 = once/second, 50 = once/2.5 seconds, 1200 = once/minute.",
                        "Default: 50. Range: 1 to 12000. An attempt may miss or hit an ineligible block, so this is not a guaranteed destruction interval.")
                .defineInRange("hungryNodeBlockEatInterval", 50, 1, 12000);
        SHIELD_RECHARGE = builder.comment("Ticks between each point of runic shielding recharge.")
                .defineInRange("shieldRecharge", 40, 1, 12000);
        SHIELD_WAIT = builder.comment("Ticks runic shielding waits before recharging after being fully depleted.")
                .defineInRange("shieldWait", 80, 0, 12000);
        SHIELD_COST = builder.comment(
                        "Vis drained from the local aura per point of runic shielding recharged. 0 makes recharging free.")
                .defineInRange("shieldCost", 1.0, 0.0, 100.0);
        ALLOW_CHAMPION_MOBS = builder.comment("Setting this to false will disable spawning champion mobs.")
                .define("allowChampionMobs", true);
        NO_SLEEP = builder.comment(
                        "Setting this to true will make you get the recipe book for salis mundus without having to sleep first.")
                .define("noSleep", false);
        builder.pop();
        builder.push("sounds");
        NO_STRESS = builder.comment(
                        "Set to true to disable anxiety triggers like the heartbeat sound and warp-event jump scares.")
                .define("nostress", false);
        builder.pop();
        builder.push("golems");
        SHOW_GOLEM_EMOTES = builder.comment(
                        "Will golems display emote particles if they receive orders or encounter problems.")
                .define("showGolemEmotes", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ThaumaturgeCommonConfig() {}
}
