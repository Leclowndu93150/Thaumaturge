package com.leclowndu93150.thaumaturge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThaumaturgeCommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue WUSS_MODE;
    public static final ModConfigSpec.DoubleValue TAINT_SPREAD_RATE;
    public static final ModConfigSpec.IntValue TAINT_SPREAD_AREA;
    public static final ModConfigSpec.IntValue TAINT_FEED_RADIUS;
    public static final ModConfigSpec.DoubleValue TAINT_FEED_RATE;
    public static final ModConfigSpec.DoubleValue TAINT_RESERVE_CAP;
    public static final ModConfigSpec.DoubleValue TAINT_GROWTH_COST;
    public static final ModConfigSpec.DoubleValue TAINT_CHILD_SEED_COST;
    public static final ModConfigSpec.IntValue TAINT_STARVE_TICKS;
    public static final ModConfigSpec.DoubleValue TAINTED_LANDS_STARTING_FLUX;
    public static final ModConfigSpec.BooleanValue TAINT_FROM_FLUX;
    public static final ModConfigSpec.BooleanValue PHYSICAL_FLUX_AURA_FLOOR;
    public static final ModConfigSpec.BooleanValue PHYSICAL_FLUX_TAINT_OUTBREAKS;
    public static final ModConfigSpec.BooleanValue FLUX_PRESSURE_EVENTS;
    public static final ModConfigSpec.DoubleValue ENERGIZED_NODE_VIS_PER_POINT;
    public static final ModConfigSpec.IntValue CRIMSON_PORTAL_RARITY;
    public static final ModConfigSpec.DoubleValue CRIMSON_PATROL_CHANCE;
    public static final ModConfigSpec.DoubleValue WILD_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue MAGICAL_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue EERIE_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue NETHER_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue DARK_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue UNSTABLE_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue PURE_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue TAINTED_NODE_CHANCE;
    public static final ModConfigSpec.DoubleValue HUNGRY_NODE_CHANCE;
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

    public static final ModConfigSpec.IntValue FLUX_SCRUBBER_CHARGES_PER_ROLL;
    public static final ModConfigSpec.DoubleValue FLUX_SCRUBBER_ESSENTIA_CHANCE;
    public static final ModConfigSpec.IntValue FLUX_SCRUBBER_ESSENTIA_PER_ROLL;
    public static final ModConfigSpec.IntValue FLUX_SCRUBBER_ESSENTIA_CAPACITY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("world");

        WUSS_MODE = builder.comment("Setting this to true disables Warp, Taint spread and similar mechanics. You wuss.")
                .define("wussMode", false);
        TAINT_SPREAD_RATE = builder.comment(
                        "The % chance of taint fibres spreading on a block tick. Setting this to 0 will effectively stop taint fibre spread.")
                .defineInRange("taintSpreadRate", 100.0, 0.0, 100.0);
        TAINT_SPREAD_AREA = builder.comment(
                        "The range in blocks that a taint origin (a taint seed or a tainted node) claims. Taint outside the range of every living origin decays, except inside naturally generated Tainted Lands.")
                .defineInRange("taintSpreadArea", 32, 1, 128);
        TAINT_FEED_RADIUS = builder.comment("How many chunks around itself a taint origin can eat flux from.")
                .defineInRange("taintFeedRadius", 2, 0, 8);
        TAINT_FEED_RATE = builder.comment("Flux a taint origin eats from the aura each second.")
                .defineInRange("taintFeedRate", 5.0, 0.0, 1000.0);
        TAINT_RESERVE_CAP = builder.comment("Most flux a taint origin can store for growth.")
                .defineInRange("taintReserveCap", 100.0, 1.0, 100000.0);
        TAINT_GROWTH_COST = builder.comment(
                        "Stored flux a taint origin spends for each block of taint it grows. A chunk polluted up to its base aura pays for roughly one chunk of taint at the default.")
                .defineInRange("taintGrowthCost", 1.0, 0.0, 1000.0);
        TAINT_CHILD_SEED_COST = builder.comment(
                        "Stored flux a well fed taint origin spends to sprout a new taint seed at the edge of its range.")
                .defineInRange("taintChildSeedCost", 25.0, 0.0, 100000.0);
        TAINT_STARVE_TICKS = builder.comment(
                        "Ticks a taint seed survives with nothing to eat before it withers away. A tainted node inside an Ethereal Bloom's range is purified after the same time.")
                .defineInRange("taintStarveTicks", 12000, 20, 1000000);
        TAINTED_LANDS_STARTING_FLUX = builder.comment(
                        "Flux a freshly generated Tainted Lands chunk starts with, as a multiple of its base aura. The tainted nodes and seeds there feed on it. 0 leaves Tainted Lands dormant until flux reaches them.")
                .defineInRange("taintedLandsStartingFlux", 1.0, 0.0, 10.0);
        TAINT_FROM_FLUX = builder.comment("Allows deep, exposed Flux Goo to fester into a taint seed.")
                .define("taintFromFlux", true);
        PHYSICAL_FLUX_AURA_FLOOR = builder.comment(
                        "Allows physical Flux Goo and Gas to sustain a capped minimum Aura Flux in their chunk. Disabling this does not remove physical Flux.")
                .define("physicalFluxAuraFloor", true);
        PHYSICAL_FLUX_TAINT_OUTBREAKS = builder.comment(
                        "Allows large accumulations of Flux Goo and Gas to sprout a taint seed independently of individual Goo blocks.")
                .define("physicalFluxTaintOutbreaks", true);
        FLUX_PRESSURE_EVENTS = builder.comment(
                        "Allows high Aura Flux to trigger Flux-pressure events. Disabling this does not affect Flux Rifts.")
                .define("fluxPressureEvents", true);
        ENERGIZED_NODE_VIS_PER_POINT = builder.comment(
                        "Aura Vis consumed per aspect point restored by an energized node. Higher values increase consumption; 0 makes recharging free.")
                .defineInRange("energizedNodeVisPerPoint", 6.0, 0.0, 100.0);
        CRIMSON_PORTAL_RARITY = builder.comment(
                        "Average chunks per naturally generated lesser Crimson Portal. Higher values make portals rarer; 0, the default, disables natural generation and crimson ranger patrols open portals instead.")
                .defineInRange("crimsonPortalRarity", 0, 0, 1000000);
        CRIMSON_PATROL_CHANCE = builder.comment(
                        "Chance from 0 to 100 that a crimson ranger patrol spawns near a random overworld player at each daytime attempt, about every 10 minutes from day 5 on. 0 disables patrols.")
                .defineInRange("crimsonPatrolChance", 10.0, 0.0, 100.0);

        builder.push("nodes");

        WILD_NODE_CHANCE = builder.comment(
                        "Chance (%) of a wild node placement attempt per Overworld chunk. Default: ~2.7778% (1 in 36 chunks). 0 disables wild node generation.")
                .defineInRange("wildSpawnChance", 100.0 / 36.0, 0.0, 100.0);
        MAGICAL_NODE_CHANCE = builder.comment(
                        "Additional node spawn chance (%) per Magical Forest chunk. Stacks with wild node chance. Default: 0%.")
                .defineInRange("magicalBonusSpawnChance", 0.0, 0.0, 100.0);
        EERIE_NODE_CHANCE = builder.comment(
                        "Additional node spawn chance (%) per Eerie biome chunk. Stacks with wild node chance. These nodes are always dark. Default: 12.5%.")
                .defineInRange("eerieBonusSpawnChance", 12.5, 0.0, 100.0);
        NETHER_NODE_CHANCE = builder.comment(
                        "Chance (%) of a node placement attempt per Nether chunk. Default: ~2.7778% (1 in 36 chunks). 0 disables Nether nodes.")
                .defineInRange("netherSpawnChance", 100.0 / 36.0, 0.0, 100.0);

        builder.comment(
                        "Node type percentages for ordinary random nodes. Defaults leave ~93.3333% normal nodes. Above 100% total, values become relative weights and normal nodes cannot spawn.")
                .push("types");

        DARK_NODE_CHANCE = builder.comment("Chance (%) for ordinary random nodes to be dark. Default: ~1.6667%.")
                .defineInRange("darkChance", 100.0 / 60.0, 0.0, 100.0);
        UNSTABLE_NODE_CHANCE = builder.comment(
                        "Chance (%) for ordinary random nodes to be unstable. Default: ~1.6667%.")
                .defineInRange("unstableChance", 100.0 / 60.0, 0.0, 100.0);
        PURE_NODE_CHANCE = builder.comment("Chance (%) for ordinary random nodes to be pure. Default: ~1.6667%.")
                .defineInRange("pureChance", 100.0 / 60.0, 0.0, 100.0);
        TAINTED_NODE_CHANCE = builder.comment(
                        "Chance (%) for ordinary random nodes to be tainted. Default: ~1.1111%. Disabled by Wuss Mode.")
                .defineInRange("taintedChance", 10.0 / 9.0, 0.0, 100.0);
        HUNGRY_NODE_CHANCE = builder.comment(
                        "Chance (%) for ordinary random nodes to be hungry. Default: ~0.5556% (1 in 180 nodes).")
                .defineInRange("hungryChance", 100.0 / 180.0, 0.0, 100.0);

        builder.pop(2);

        HUNGRY_NODE_BLOCK_EAT_RANGE = builder.comment(
                        "Maximum block-eating ray length in blocks. Dropped items are pulled from this range plus half a block; the entity pulling range is not affected.")
                .defineInRange("hungryNodeBlockEatRange", 16, 1, 64);
        SCALE_HUNGRY_NODE_RANGE_BY_MODIFIER = builder.comment(
                        "Scales hungry node block-eating range by quality. Uses the minimum and maximum ranges below instead of the fixed range.")
                .define("scaleHungryNodeBlockEatRangeByModifier", false);
        HUNGRY_NODE_MINIMUM_BLOCK_EAT_RANGE = builder.comment(
                        "Block-eating range for fading hungry nodes when quality scaling is enabled. Ignored when scaling is disabled.")
                .defineInRange("hungryNodeMinimumBlockEatRange", 16, 1, 64);
        HUNGRY_NODE_MAXIMUM_BLOCK_EAT_RANGE = builder.comment(
                        "Block-eating range for bright hungry nodes when quality scaling is enabled. Values below the minimum use the minimum for all qualities.")
                .defineInRange("hungryNodeMaximumBlockEatRange", 32, 1, 64);
        HUNGRY_NODE_BLOCK_HARDNESS = builder.comment(
                        "Maximum edible block hardness (exclusive). Default: 5; obsidian is 50. 0 disables block destruction. Unbreakable blocks are excluded.")
                .defineInRange("hungryNodeBlockEatHardness", 5.0, 0.0, 100.0);
        HUNGRY_NODE_BLOCK_EAT_INTERVAL = builder.comment(
                        "Ticks between hungry node block-eating attempts. Lower values are faster; 20 ticks = 1 second. Attempts may miss.")
                .defineInRange("hungryNodeBlockEatInterval", 50, 1, 12000);
        SHIELD_RECHARGE = builder.comment(
                        "Ticks between each point of runic shielding recharge. Lower values recharge faster.")
                .defineInRange("shieldRecharge", 40, 1, 12000);
        SHIELD_WAIT = builder.comment("Ticks before runic shielding begins recharging after being fully depleted.")
                .defineInRange("shieldWait", 80, 0, 12000);
        SHIELD_COST = builder.comment(
                        "Aura Vis consumed per point of runic shielding restored. 0 makes recharging free.")
                .defineInRange("shieldCost", 1.0, 0.0, 100.0);
        ALLOW_CHAMPION_MOBS = builder.comment("Allows champion mobs to spawn.").define("allowChampionMobs", true);
        NO_SLEEP = builder.comment(
                        "Setting this to true will make you get the recipe book for salis mundus without having to sleep first.")
                .define("noSleep", false);

        builder.pop();
        builder.push("sounds");

        NO_STRESS = builder.comment("Disables anxiety effects such as heartbeat sounds and Warp-event jump scares.")
                .define("nostress", false);

        builder.pop();
        builder.push("golems");

        SHOW_GOLEM_EMOTES = builder.comment(
                        "Will golems display emote particles if they receive orders or encounter problems.")
                .define("showGolemEmotes", true);

        builder.pop();
        builder.push("fluxScrubber");

        FLUX_SCRUBBER_CHARGES_PER_ROLL = builder.comment(
                        "Flux Goo or Gas units cleaned per Praecantatio recovery attempt. Lower values increase recovery frequency.")
                .defineInRange("chargesPerRoll", 4, 1, 64);
        FLUX_SCRUBBER_ESSENTIA_CHANCE = builder.comment(
                        "Chance (0-1) of recovering Praecantatio per roll. 1.0 guarantees recovery.")
                .defineInRange("essentiaChance", 0.25, 0.0, 1.0);
        FLUX_SCRUBBER_ESSENTIA_PER_ROLL = builder.comment(
                        "Praecantatio produced per successful recovery roll. Increase capacity to accommodate larger amounts.")
                .defineInRange("essentiaPerRoll", 1, 0, 64);
        FLUX_SCRUBBER_ESSENTIA_CAPACITY = builder.comment(
                        "Maximum Praecantatio stored before the scrubber must be drained by an attached pipe or jar.")
                .defineInRange("essentiaCapacity", 4, 1, 1024);

        builder.pop();
        SPEC = builder.build();
    }

    private ThaumaturgeCommonConfig() {}
}
