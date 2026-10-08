package com.leclowndu93150.thaumaturge.data.lang;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import net.neoforged.neoforge.common.ModConfigSpec;

final class ConfigLangEn {
    private static final String PREFIX = TTIds.MODID + ".configuration.";
    private static final List<ConfigFile> FILES = List.of(
            new ConfigFile(ThaumaturgeClientConfig.SPEC, "client", "Client"),
            new ConfigFile(ThaumaturgeCommonConfig.SPEC, "common", "Common"),
            new ConfigFile(ThaumaturgeServerConfig.SPEC, "server", "Server"));
    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("liquid_death", "Liquid Death"),
            Map.entry("liquid_death.dropRateBound1", "Minimum Aspect Drop Rate"),
            Map.entry("liquid_death.dropRateBound2", "Maximum Aspect Drop Rate"),
            Map.entry("tooltip", "Tooltips"),
            Map.entry("tooltip.show_aspects_by_default", "Show Aspects by Default"),
            Map.entry("graphics", "Graphics"),
            Map.entry("graphics.large_tag_text", "Large Aspect Numbers"),
            Map.entry("hud", "HUD"),
            Map.entry("hud.dial_bottom", "Vis Dial at Bottom"),
            Map.entry(ThaumaturgeClientConfig.DONATOR_CAPE_KEY, "Donator Cape"),
            Map.entry("world", "World"),
            Map.entry("world.wussMode", "Wuss Mode"),
            Map.entry("world.taintSpreadRate", "Taint Spread Rate"),
            Map.entry("world.taintSpreadArea", "Taint Spread Area"),
            Map.entry("world.taintFrontierRate", "Taint Frontier Rate"),
            Map.entry("world.taintFromFlux", "Taint from Flux"),
            Map.entry("world.physicalFluxAuraFloor", "Physical Flux Aura Floor"),
            Map.entry("world.physicalFluxTaintOutbreaks", "Physical Flux Taint Outbreaks"),
            Map.entry("world.fluxPressureEvents", "Flux Pressure Events"),
            Map.entry("world.energizedNodeVisPerPoint", "Energized Node Vis per Point"),
            Map.entry("world.crimsonPortalRarity", "Crimson Portal Rarity"),
            Map.entry("world.nodes", "Aura Nodes"),
            Map.entry("world.nodes.wildSpawnChance", "Wild Node Chance"),
            Map.entry("world.nodes.magicalBonusSpawnChance", "Magical Forest Bonus Chance"),
            Map.entry("world.nodes.eerieBonusSpawnChance", "Eerie Biome Bonus Chance"),
            Map.entry("world.nodes.netherSpawnChance", "Nether Node Chance"),
            Map.entry("world.nodes.types", "Node Types"),
            Map.entry("world.nodes.types.darkChance", "Dark Node Chance"),
            Map.entry("world.nodes.types.unstableChance", "Unstable Node Chance"),
            Map.entry("world.nodes.types.pureChance", "Pure Node Chance"),
            Map.entry("world.nodes.types.taintedChance", "Tainted Node Chance"),
            Map.entry("world.nodes.types.hungryChance", "Hungry Node Chance"),
            Map.entry("world.hungryNodeBlockEatRange", "Hungry Node Eating Range"),
            Map.entry("world.scaleHungryNodeBlockEatRangeByModifier", "Scale Eating Range by Modifier"),
            Map.entry("world.hungryNodeMinimumBlockEatRange", "Hungry Node Minimum Range"),
            Map.entry("world.hungryNodeMaximumBlockEatRange", "Hungry Node Maximum Range"),
            Map.entry("world.hungryNodeBlockEatHardness", "Hungry Node Hardness Limit"),
            Map.entry("world.hungryNodeBlockEatInterval", "Hungry Node Eating Interval"),
            Map.entry("world.shieldRecharge", "Runic Shield Recharge Interval"),
            Map.entry("world.shieldWait", "Runic Shield Recharge Delay"),
            Map.entry("world.shieldCost", "Runic Shield Vis Cost"),
            Map.entry("world.allowChampionMobs", "Champion Mobs"),
            Map.entry("world.noSleep", "Salis Mundus Without Sleeping"),
            Map.entry("sounds", "Sounds"),
            Map.entry("sounds.nostress", "No Stress"),
            Map.entry("golems", "Golems"),
            Map.entry("golems.showGolemEmotes", "Golem Emotes"),
            Map.entry("fluxScrubber", "Flux Scrubber"),
            Map.entry("fluxScrubber.chargesPerRoll", "Charges per Roll"),
            Map.entry("fluxScrubber.essentiaChance", "Essentia Chance"),
            Map.entry("fluxScrubber.essentiaPerRoll", "Essentia per Roll"),
            Map.entry("fluxScrubber.essentiaCapacity", "Essentia Capacity"),
            Map.entry("infernal_furnace", "Infernal Furnace"),
            Map.entry("infernal_furnace.lavaTurnIntoBlaze", "Lava Becomes a Blaze"),
            Map.entry("spells", "Spells"),
            Map.entry("spells.maxEntitiesPerTick", "Entities per Cast per Tick"),
            Map.entry("spells.maxBlocksPerTick", "Blocks per Cast per Tick"),
            Map.entry("spells.maxNodesPerRun", "Nodes per Cast Step"),
            Map.entry("spells.maxDelayedPerLevel", "Delayed Spells per Level"),
            Map.entry("labyrinth", "Outer Lands Labyrinth"),
            Map.entry("labyrinth.generation", "Generation"),
            Map.entry("labyrinth.generation.sizeScale", "Maze Size Multiplier"),
            Map.entry("labyrinth.generation.loopScale", "Loop Multiplier"),
            Map.entry("labyrinth.generation.decorationDensity", "Decoration Density"),
            Map.entry("labyrinth.generation.lootScale", "Loot Multiplier"),
            Map.entry("labyrinth.generation.disabledRooms", "Disabled Rooms"),
            Map.entry("labyrinth.generation.disabledMarkers", "Disabled Markers"),
            Map.entry("labyrinth.generation.defaultDefinition", "Default Labyrinth"),
            Map.entry("labyrinth.lifecycle", "Lifecycle"),
            Map.entry("labyrinth.lifecycle.maxActiveMazes", "Maximum Active Mazes"),
            Map.entry("labyrinth.lifecycle.retireConqueredAfterDays", "Retire Conquered Mazes After Days"),
            Map.entry("labyrinth.lifecycle.retireAbandonedAfterDays", "Retire Abandoned Mazes After Days"),
            Map.entry("labyrinth.lifecycle.repairChunksPerTick", "Chunk Repairs per Tick"),
            Map.entry("labyrinth.lifecycle.triggerIntervalTicks", "Trigger Check Interval"),
            Map.entry("labyrinth.lifecycle.integrityIntervalTicks", "Integrity Check Interval"),
            Map.entry("labyrinth.lifecycle.voidContactDamage", "Void Contact Damage"),
            Map.entry("labyrinth.entry", "Entry"),
            Map.entry("labyrinth.entry.ritualVisCost", "Ritual Vis Cost"),
            Map.entry("labyrinth.entry.ritualChannelTicks", "Ritual Channel Time"),
            Map.entry("labyrinth.entry.ritualMaxDistance", "Ritual Maximum Distance"),
            Map.entry("labyrinth.entry.allowEyeRemoval", "Allow Eye Removal"),
            Map.entry("labyrinth.entry.awakenEyeThreshold", "Eyes to Awaken Site"),
            Map.entry("labyrinth.entry.sealSiteWhileLinked", "Seal Obelisk While Linked"),
            Map.entry("labyrinth.transit", "Travel"),
            Map.entry("labyrinth.transit.warmupTicks", "Portal Warmup"),
            Map.entry("labyrinth.transit.cooldownTicks", "Portal Cooldown"),
            Map.entry("labyrinth.transit.maxPreloadWaitTicks", "Maximum Preload Wait"),
            Map.entry("labyrinth.transit.preloadRadiusChunks", "Preload Radius"),
            Map.entry("labyrinth.transit.returnSearchRadius", "Return Search Radius"),
            Map.entry("labyrinth.transit.allowArrivalRepair", "Repair Arrival Point"),
            Map.entry("labyrinth.transit.evacuateStrandedOnLogin", "Evacuate Stranded Players"),
            Map.entry("labyrinth.encounter", "Encounters"),
            Map.entry("labyrinth.encounter.lockChargeTicks", "Lock Charge Time"),
            Map.entry("labyrinth.encounter.manifestBlocksPerTick", "Arena Blocks per Tick"),
            Map.entry("labyrinth.encounter.protectHall", "Protect Boss Hall"),
            Map.entry("labyrinth.encounter.noBuildRadius", "No Build Radius"),
            Map.entry("labyrinth.encounter.wipeResetTicks", "Wipe Reset Time"),
            Map.entry("labyrinth.encounter.missingBossRespawnTicks", "Missing Boss Respawn Time"),
            Map.entry("labyrinth.encounter.announceScope", "Announcement Scope"),
            Map.entry("labyrinth.encounter.announceNearbyRange", "Nearby Announcement Range"),
            Map.entry("labyrinth.encounter.scaleWithParticipants", "Scale with Participants"),
            Map.entry("labyrinth.encounter.disruptFlight", "Disrupt Flight"),
            Map.entry("labyrinth.encounter.bossLeashRadius", "Boss Leash Radius"),
            Map.entry("labyrinth.encounter.openExitRift", "Open Exit Rift"),
            Map.entry("labyrinth.rewards", "Rewards"),
            Map.entry("labyrinth.rewards.participationMinPresenceTicks", "Minimum Presence for Rewards"),
            Map.entry("labyrinth.rewards.participationMinDamageFraction", "Minimum Damage Share for Rewards"),
            Map.entry("labyrinth.rewards.pearlPolicy", "Primordial Pearl Policy"),
            Map.entry("labyrinth.rewards.keyRoomRequiresWard", "Key Room Requires Ward"),
            Map.entry("labyrinth.rewards.reissueTablets", "Reissue Lost Tablets"),
            Map.entry("labyrinth.rewards.crumbleSpentTablets", "Crumble Spent Tablets"),
            Map.entry("labyrinth.wayfinding", "Wayfinding"),
            Map.entry("labyrinth.wayfinding.glyphHints", "Glyph Hints"),
            Map.entry("labyrinth.wayfinding.glyphPulseCooldownTicks", "Glyph Hint Cooldown"),
            Map.entry("labyrinth.wayfinding.tabletResonance", "Tablet Resonance"),
            Map.entry("labyrinth.wayfinding.tabletResonanceIntervalMin", "Fastest Tablet Chime Interval"),
            Map.entry("labyrinth.wayfinding.tabletResonanceIntervalMax", "Slowest Tablet Chime Interval"),
            Map.entry("labyrinth.wayfinding.glyphRange", "Glyph Hint Range"),
            Map.entry("labyrinth.wayfinding.tabletResonanceRangeCells", "Tablet Resonance Range"),
            Map.entry("labyrinth.guardians", "Guardians"),
            Map.entry("labyrinth.guardians.postActivationRadius", "Guardian Post Activation Radius"),
            Map.entry("labyrinth.guardians.championChanceOverride", "Champion Chance Override"),
            Map.entry("labyrinth.guardians.postGuardLeashRadius", "Guardian Leash Radius"),
            Map.entry("labyrinth.hazards", "Hazards"),
            Map.entry("labyrinth.hazards.trapDamage", "Rune Trap Damage"),
            Map.entry("labyrinth.hazards.crabVentMaxCrabs", "Crabs per Vent"),
            Map.entry("labyrinth.hazards.crabVentActivationRange", "Crab Vent Activation Range"));

    private ConfigLangEn() {}

    static void add(BiConsumer<String, String> output) {
        output.accept(PREFIX + "title", "Thaumaturge Configuration");
        Map<String, String> keys = new HashMap<>();
        Set<String> paths = new HashSet<>();
        for (ConfigFile file : FILES) {
            String section = PREFIX + "section." + TTIds.MODID + "." + file.type() + ".toml";
            output.accept(section, file.name() + " Settings");
            output.accept(section + ".title", "Thaumaturge " + file.name() + " Settings");
            addLevel(file.spec().getSpec(), "", output, keys, paths);
        }
        Set<String> unused = new HashSet<>(LABELS.keySet());
        unused.removeAll(paths);
        if (!unused.isEmpty()) {
            throw new IllegalStateException("Config labels for missing entries: " + unused);
        }
    }

    private static void addLevel(
            UnmodifiableConfig level,
            String parent,
            BiConsumer<String, String> output,
            Map<String, String> keys,
            Set<String> paths) {
        for (UnmodifiableConfig.Entry entry : level.entrySet()) {
            String path = parent.isEmpty() ? entry.getKey() : parent + "." + entry.getKey();
            String label = LABELS.get(path);
            if (label == null) {
                throw new IllegalStateException("Missing config label for " + path);
            }
            String key = PREFIX + entry.getKey();
            String previous = keys.putIfAbsent(key, path);
            if (previous != null) {
                throw new IllegalStateException(
                        "Config entries " + previous + " and " + path + " share the translation key " + key);
            }
            paths.add(path);
            output.accept(key, label);
            if (entry.getValue() instanceof UnmodifiableConfig section) {
                addLevel(section, path, output, keys, paths);
            }
        }
    }

    private record ConfigFile(ModConfigSpec spec, String type, String name) {}
}
