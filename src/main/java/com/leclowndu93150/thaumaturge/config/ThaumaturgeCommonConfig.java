package com.leclowndu93150.thaumaturge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumaturgeCommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue WUSS_MODE;
    public static final ModConfigSpec.DoubleValue TAINT_SPREAD_RATE;
    public static final ModConfigSpec.IntValue TAINT_SPREAD_AREA;
    public static final ModConfigSpec.DoubleValue ENERGIZED_NODE_VIS_PER_POINT;
    public static final ModConfigSpec.IntValue CRIMSON_PORTAL_RARITY;
    public static final ModConfigSpec.DoubleValue WILD_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue MAGICAL_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue EERIE_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue NETHER_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue DARK_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue UNSTABLE_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue PURE_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue HUNGRY_NODE_CHANCE;
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
        builder.push("nodes");
        WILD_NODE_CHANCE = builder.comment(
                        "Chance from 0 to 100 for a wild node placement attempt in each Overworld chunk. 4 means 4%, or about one attempt per 25 chunks. 0 disables this source.")
                .defineInRange("wildSpawnChance", 4.0, 0.0, 100.0);
        MAGICAL_NODE_CHANCE = builder.comment(
                        "Additional chance from 0 to 100 in each Magical Forest chunk. This stacks with wildSpawnChance. 8.333 means roughly one additional attempt per 12 chunks.")
                .defineInRange("magicalBonusSpawnChance", 100.0 / 12.0, 0.0, 100.0);
        EERIE_NODE_CHANCE = builder.comment(
                        "Additional chance from 0 to 100 in each Eerie biome chunk. This stacks with wildSpawnChance and its node is always dark. 12.5 means one attempt per 8 chunks.")
                .defineInRange("eerieBonusSpawnChance", 12.5, 0.0, 100.0);
        NETHER_NODE_CHANCE = builder.comment(
                        "Chance from 0 to 100 for a node placement attempt in each Nether chunk. 2.5 means one attempt per 40 chunks. 0 disables Nether nodes.")
                .defineInRange("netherSpawnChance", 2.5, 0.0, 100.0);
        builder.comment(
                        "The following values are percentages among ordinary random nodes. Their default total is 6.6667%, leaving 93.3333% normal nodes. If their total exceeds 100, they are treated as relative weights and normal nodes become 0%.")
                .push("types");
        DARK_NODE_CHANCE = builder.comment("Dark-node percentage, from 0 to 100. Default: 2%.")
                .defineInRange("darkChance", 2.0, 0.0, 100.0);
        UNSTABLE_NODE_CHANCE = builder.comment("Unstable-node percentage, from 0 to 100. Default: 2%.")
                .defineInRange("unstableChance", 2.0, 0.0, 100.0);
        PURE_NODE_CHANCE = builder.comment("Pure-node percentage, from 0 to 100. Default: 2%.")
                .defineInRange("pureChance", 2.0, 0.0, 100.0);
        HUNGRY_NODE_CHANCE = builder.comment(
                        "Hungry-node percentage, from 0 to 100. Default: 0.6667%, approximately one hungry node per 150 ordinary nodes.")
                .defineInRange("hungryChance", 2.0 / 3.0, 0.0, 100.0);
        builder.pop(2);
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
