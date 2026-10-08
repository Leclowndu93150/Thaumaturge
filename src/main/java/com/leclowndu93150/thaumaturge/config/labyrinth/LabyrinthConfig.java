package com.leclowndu93150.thaumaturge.config.labyrinth;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class LabyrinthConfig {
    public final ModConfigSpec.DoubleValue sizeScale;
    public final ModConfigSpec.DoubleValue loopScale;
    public final ModConfigSpec.DoubleValue decorationDensity;
    public final ModConfigSpec.DoubleValue lootScale;
    public final ModConfigSpec.ConfigValue<List<? extends String>> disabledRooms;
    public final ModConfigSpec.ConfigValue<List<? extends String>> disabledMarkers;
    public final ModConfigSpec.ConfigValue<String> defaultDefinition;

    public final ModConfigSpec.IntValue maxActiveMazes;
    public final ModConfigSpec.IntValue retireConqueredAfterDays;
    public final ModConfigSpec.IntValue retireAbandonedAfterDays;
    public final ModConfigSpec.IntValue repairChunksPerTick;
    public final ModConfigSpec.IntValue triggerIntervalTicks;
    public final ModConfigSpec.IntValue integrityIntervalTicks;
    public final ModConfigSpec.DoubleValue voidContactDamage;

    public final ModConfigSpec.DoubleValue ritualVisCost;
    public final ModConfigSpec.IntValue ritualChannelTicks;
    public final ModConfigSpec.DoubleValue ritualMaxDistance;
    public final ModConfigSpec.BooleanValue allowEyeRemoval;
    public final ModConfigSpec.IntValue awakenEyeThreshold;
    public final ModConfigSpec.BooleanValue sealSiteWhileLinked;

    public final ModConfigSpec.IntValue warmupTicks;
    public final ModConfigSpec.IntValue cooldownTicks;
    public final ModConfigSpec.IntValue maxPreloadWaitTicks;
    public final ModConfigSpec.IntValue preloadRadiusChunks;
    public final ModConfigSpec.IntValue returnSearchRadius;
    public final ModConfigSpec.BooleanValue allowArrivalRepair;
    public final ModConfigSpec.BooleanValue evacuateStrandedOnLogin;

    public final ModConfigSpec.IntValue lockChargeTicks;
    public final ModConfigSpec.IntValue manifestBlocksPerTick;
    public final ModConfigSpec.BooleanValue protectHall;
    public final ModConfigSpec.DoubleValue noBuildRadius;
    public final ModConfigSpec.IntValue wipeResetTicks;
    public final ModConfigSpec.IntValue missingBossRespawnTicks;
    public final ModConfigSpec.EnumValue<AnnounceScope> announceScope;
    public final ModConfigSpec.DoubleValue announceNearbyRange;
    public final ModConfigSpec.BooleanValue scaleWithParticipants;
    public final ModConfigSpec.BooleanValue disruptFlight;
    public final ModConfigSpec.IntValue bossLeashRadius;
    public final ModConfigSpec.BooleanValue openExitRift;

    public final ModConfigSpec.IntValue participationMinPresenceTicks;
    public final ModConfigSpec.DoubleValue participationMinDamageFraction;
    public final ModConfigSpec.EnumValue<PearlPolicy> pearlPolicy;
    public final ModConfigSpec.BooleanValue keyRoomRequiresWard;
    public final ModConfigSpec.BooleanValue reissueTablets;
    public final ModConfigSpec.BooleanValue crumbleSpentTablets;

    public final ModConfigSpec.BooleanValue glyphHints;
    public final ModConfigSpec.IntValue glyphPulseCooldownTicks;
    public final ModConfigSpec.BooleanValue tabletResonance;
    public final ModConfigSpec.IntValue tabletResonanceIntervalMin;
    public final ModConfigSpec.IntValue tabletResonanceIntervalMax;
    public final ModConfigSpec.DoubleValue glyphRange;
    public final ModConfigSpec.IntValue tabletResonanceRangeCells;

    public final ModConfigSpec.IntValue postActivationRadius;
    public final ModConfigSpec.DoubleValue championChanceOverride;
    public final ModConfigSpec.IntValue postGuardLeashRadius;
    public final ModConfigSpec.DoubleValue trapDamage;
    public final ModConfigSpec.IntValue crabVentMaxCrabs;
    public final ModConfigSpec.IntValue crabVentActivationRange;

    public LabyrinthConfig(ModConfigSpec.Builder builder) {
        builder.push("labyrinth");

        builder.push("generation");
        sizeScale = builder.comment(
                        "Multiplier on the maze size rolled from the labyrinth definition. The result is clamped to 9..41 cells and kept odd.")
                .defineInRange("sizeScale", 1.0, 0.5, 1.5);
        loopScale = builder.comment("Multiplier on the number of extra corridor loops, which give alternative routes.")
                .defineInRange("loopScale", 1.0, 0.0, 3.0);
        decorationDensity = builder.comment(
                        "Multiplier on wall decoration such as glowing crust, glyphs, traps and crab vents.")
                .defineInRange("decorationDensity", 1.0, 0.0, 4.0);
        lootScale = builder.comment("Multiplier on the chance of each loot container marker in room templates.")
                .defineInRange("lootScale", 1.0, 0.0, 4.0);
        disabledRooms = builder.comment(
                        "Room ids or #tags that are never placed, for example \"thaumaturge:passage/webbed_straight\" or \"#thaumaturge:labyrinth/rare\".")
                .defineListAllowEmpty("disabledRooms", List.of(), () -> "", LabyrinthConfig::isIdOrTag);
        disabledMarkers = builder.comment(
                        "Marker type ids that are skipped when rooms are placed, for example \"thaumaturge:aura\".")
                .defineListAllowEmpty("disabledMarkers", List.of(), () -> "", LabyrinthConfig::isId);
        defaultDefinition = builder.comment("Labyrinth definition used when an obelisk site does not name one.")
                .define("defaultDefinition", "thaumaturge:eldritch", LabyrinthConfig::isId);
        builder.pop();

        builder.push("lifecycle");
        maxActiveMazes = builder.comment(
                        "Maximum number of mazes kept at once. Rituals are refused, and their eyes returned, while the limit is reached.")
                .defineInRange("maxActiveMazes", 128, 1, 4096);
        retireConqueredAfterDays = builder.comment(
                        "In-game days after which a conquered maze with nobody inside is dropped. -1 keeps them.")
                .defineInRange("retireConqueredAfterDays", 3, -1, 3650);
        retireAbandonedAfterDays = builder.comment(
                        "In-game days without a visit after which an unconquered maze with nobody inside is dropped. -1 keeps them.")
                .defineInRange("retireAbandonedAfterDays", -1, -1, 3650);
        repairChunksPerTick = builder.comment(
                        "Maze chunks that were generated before their maze existed are rebuilt at this rate.")
                .defineInRange("repairChunksPerTick", 1, 1, 16);
        triggerIntervalTicks = builder.comment("How often proximity markers such as guardian posts are checked.")
                .defineInRange("triggerIntervalTicks", 20, 5, 200);
        integrityIntervalTicks = builder.comment(
                        "How often an occupied maze checks that its portals, lock and reliquaries are still in place, and puts back any that are missing.")
                .defineInRange("integrityIntervalTicks", 100, 20, 1200);
        voidContactDamage = builder.comment(
                        "Damage dealt each time a creature touches the exposed void of the Outer Lands.")
                .defineInRange("voidContactDamage", 8.0, 0.0, 100.0);
        builder.pop();

        builder.push("entry");
        ritualVisCost = builder.comment("Vis drained from the aura over the course of the portal ritual.")
                .defineInRange("ritualVisCost", 100.0, 0.0, 1000.0);
        ritualChannelTicks = builder.comment("Length of the portal ritual channel.")
                .defineInRange("ritualChannelTicks", 60, 1, 1200);
        ritualMaxDistance = builder.comment("The ritual fizzles if the caster moves further than this from the altar.")
                .defineInRange("ritualMaxDistance", 8.0, 1.0, 32.0);
        allowEyeRemoval = builder.comment("Lets players take seated eyes back with an empty hand while sneaking.")
                .define("allowEyeRemoval", true);
        awakenEyeThreshold = builder.comment(
                        "Seated eyes at which the site awakens and spawns guardians until the portal opens. 0 disables awakening.")
                .defineInRange("awakenEyeThreshold", 3, 0, 4);
        sealSiteWhileLinked = builder.comment(
                        "Makes the obelisk unbreakable while its portal leads to an unconquered maze.")
                .define("sealSiteWhileLinked", true);
        builder.pop();

        builder.push("transit");
        warmupTicks = builder.comment(
                        "Ticks a player stands in a portal before travelling, once the destination has loaded.")
                .defineInRange("warmupTicks", 40, 0, 200);
        cooldownTicks = builder.comment("Ticks before the same player can use an eldritch portal again.")
                .defineInRange("cooldownTicks", 100, 0, 1200);
        maxPreloadWaitTicks = builder.comment(
                        "If the destination has not loaded after this many ticks, it is loaded on the server thread instead.")
                .defineInRange("maxPreloadWaitTicks", 200, 20, 500);
        preloadRadiusChunks = builder.comment("Chunk radius loaded around a portal destination before travel.")
                .defineInRange("preloadRadiusChunks", 1, 0, 3);
        returnSearchRadius = builder.comment("Radius searched for a safe surface around the altar when returning.")
                .defineInRange("returnSearchRadius", 16, 0, 64);
        allowArrivalRepair = builder.comment(
                        "Places a floor and clears headroom at the arrival point when no safe spot is found.")
                .define("allowArrivalRepair", true);
        evacuateStrandedOnLogin = builder.comment(
                        "Sends players who log in inside the Outer Lands but outside any known maze back to safety.")
                .define("evacuateStrandedOnLogin", true);
        builder.pop();

        builder.push("encounter");
        lockChargeTicks = builder.comment("Ticks the lock charges before the boss door opens.")
                .defineInRange("lockChargeTicks", 100, 20, 1200);
        manifestBlocksPerTick = builder.comment("Arena blocks placed per tick while the lock charges.")
                .defineInRange("manifestBlocksPerTick", 512, 16, 8192);
        protectHall = builder.comment("Stops block breaking and placing in the boss hall while the encounter runs.")
                .define("protectHall", true);
        noBuildRadius = builder.comment("Blocks cannot be placed or broken within this distance of a living boss.")
                .defineInRange("noBuildRadius", 24.0, 0.0, 64.0);
        wipeResetTicks = builder.comment(
                        "Bosses heal to full when no participant is alive in the hall for this long. -1 disables it.")
                .defineInRange("wipeResetTicks", 600, -1, 72000);
        missingBossRespawnTicks = builder.comment(
                        "A boss that vanished without dying is spawned again after this long.")
                .defineInRange("missingBossRespawnTicks", 200, 20, 72000);
        announceScope = builder.comment(
                        "Who sees encounter titles: everyone in the maze, only players in the hall, or players within announceNearbyRange of it.")
                .defineEnum("announceScope", AnnounceScope.MAZE);
        announceNearbyRange = builder.comment("Range used when announceScope is NEARBY.")
                .defineInRange("announceNearbyRange", 32.0, 8.0, 256.0);
        scaleWithParticipants = builder.comment("Scales boss health and damage with the number of players in the hall.")
                .define("scaleWithParticipants", true);
        disruptFlight = builder.comment("Disrupts hover and flight gear inside the Outer Lands.")
                .define("disruptFlight", true);
        bossLeashRadius = builder.comment("How far encounter bosses may wander from the centre of their hall.")
                .defineInRange("bossLeashRadius", 24, 8, 64);
        openExitRift = builder.comment("Opens a rift back to the altar in the boss hall once the encounter is beaten.")
                .define("openExitRift", true);
        builder.pop();

        builder.push("rewards");
        participationMinPresenceTicks = builder.comment(
                        "Ticks spent in the boss hall during the fight that make a player eligible for the boss reliquary.")
                .defineInRange("participationMinPresenceTicks", 200, 0, 72000);
        participationMinDamageFraction = builder.comment(
                        "Fraction of total boss health dealt that makes a player eligible regardless of time spent in the hall.")
                .defineInRange("participationMinDamageFraction", 0.02, 0.0, 1.0);
        pearlPolicy = builder.comment("Who receives a primordial pearl from the boss reliquary.")
                .defineEnum("pearlPolicy", PearlPolicy.EACH_ELIGIBLE);
        keyRoomRequiresWard = builder.comment(
                        "The key reliquary stays sealed until the guardians watching it are defeated.")
                .define("keyRoomRequiresWard", true);
        reissueTablets = builder.comment(
                        "The key reliquary gives a new bound tablet to a player who has lost theirs while the maze is still sealed.")
                .define("reissueTablets", true);
        crumbleSpentTablets = builder.comment(
                        "Bound tablets crumble once their maze's lock has opened or the maze is gone.")
                .define("crumbleSpentTablets", true);
        builder.pop();

        builder.push("wayfinding");
        glyphHints = builder.comment("Corridor glyphs drift particles toward the current objective.")
                .define("glyphHints", true);
        glyphPulseCooldownTicks = builder.comment("Minimum ticks between glyph hints for one player.")
                .defineInRange("glyphPulseCooldownTicks", 60, 10, 1200);
        tabletResonance = builder.comment("A held runed tablet chimes faster as its lock gets closer.")
                .define("tabletResonance", true);
        tabletResonanceIntervalMin = builder.comment("Chime interval when standing at the lock.")
                .defineInRange("tabletResonanceIntervalMin", 10, 5, 200);
        tabletResonanceIntervalMax = builder.comment("Chime interval when far from the lock.")
                .defineInRange("tabletResonanceIntervalMax", 60, 10, 400);
        glyphRange = builder.comment("Distance at which a corridor glyph gives its hint.")
                .defineInRange("glyphRange", 5.0, 2.0, 16.0);
        tabletResonanceRangeCells = builder.comment(
                        "Path length in maze cells at which the tablet reaches its slowest chime.")
                .defineInRange("tabletResonanceRangeCells", 24, 4, 64);
        builder.pop();

        builder.push("guardians");
        postActivationRadius = builder.comment("Distance at which guardian posts spawn their guardians.")
                .defineInRange("postActivationRadius", 20, 4, 64);
        championChanceOverride = builder.comment(
                        "Overrides the labyrinth definition's champion chance for guardians. -1 uses the definition.")
                .defineInRange("championChanceOverride", -1.0, -1.0, 1.0);
        postGuardLeashRadius = builder.comment("How far guardians spawned by a post may wander from it.")
                .defineInRange("postGuardLeashRadius", 12, 4, 32);
        builder.pop();

        builder.push("hazards");
        trapDamage = builder.comment("Damage dealt by an eldritch rune trap when it fires.")
                .defineInRange("trapDamage", 2.0, 0.0, 100.0);
        crabVentMaxCrabs = builder.comment("Eldritch crabs a vent keeps alive around it before it stops spawning.")
                .defineInRange("crabVentMaxCrabs", 5, 0, 32);
        crabVentActivationRange = builder.comment("A crab vent only spawns while a player is within this distance.")
                .defineInRange("crabVentActivationRange", 16, 4, 64);
        builder.pop();

        builder.pop();
    }

    private static boolean isId(Object value) {
        return value instanceof String text && ResourceLocation.tryParse(text) != null;
    }

    private static boolean isIdOrTag(Object value) {
        return value instanceof String text
                && ResourceLocation.tryParse(text.startsWith("#") ? text.substring(1) : text) != null;
    }
}
