package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.EncounterEntry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.FeatureQuota;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.GuardianTable;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthGameplay;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LayoutSettings;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomPools;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthRoomTags;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class LabyrinthDefinitionBootstrap {
    static final ResourceKey<LabyrinthDefinition> ELDRITCH =
            ResourceKey.create(LabyrinthDefinition.REGISTRY_KEY, TTIds.rl("eldritch"));

    private static final int MIN_SIZE = 11;
    private static final int MAX_SIZE = 29;
    private static final int FEATURE_PORTAL_DISTANCE = 4;
    private static final int WEIGHT_COMMON = 4;
    private static final int WEIGHT_UNCOMMON = 3;
    private static final int WEIGHT_RARE = 2;

    private LabyrinthDefinitionBootstrap() {}

    public static void bootstrap(BootstrapContext<LabyrinthDefinition> context) {
        HolderGetter<RoomType> rooms = context.lookup(RoomType.REGISTRY_KEY);
        HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
        RoomPools pools = new RoomPools(
                rooms.getOrThrow(TTLabyrinthRoomTags.PORTAL),
                rooms.getOrThrow(TTLabyrinthRoomTags.KEY),
                rooms.getOrThrow(TTLabyrinthRoomTags.PASSAGES),
                rooms.getOrThrow(TTLabyrinthRoomTags.FALLBACK),
                rooms.getOrThrow(TTLabyrinthRoomTags.BOSS_HALLS),
                List.of(
                        new FeatureQuota(
                                rooms.getOrThrow(TTLabyrinthRoomTags.NESTS),
                                UniformInt.of(1, 3),
                                FeatureQuota.Placement.DEAD_END,
                                FEATURE_PORTAL_DISTANCE),
                        new FeatureQuota(
                                rooms.getOrThrow(TTLabyrinthRoomTags.LIBRARIES),
                                UniformInt.of(1, 2),
                                FeatureQuota.Placement.DEAD_END,
                                FEATURE_PORTAL_DISTANCE),
                        new FeatureQuota(
                                rooms.getOrThrow(TTLabyrinthRoomTags.RARE),
                                UniformInt.of(2, 5),
                                FeatureQuota.Placement.ANY,
                                FEATURE_PORTAL_DISTANCE)));
        GuardianTable guardians = new GuardianTable(
                SimpleWeightedRandomList.single(TTEntities.ELDRITCH_GUARDIAN.get()),
                GuardianTable.DEFAULT_CHAMPION_CHANCE,
                GuardianTable.DEFAULT_PER_POST);
        LabyrinthGameplay gameplay =
                new LabyrinthGameplay(TTLootTables.LABYRINTH_KEY_ROOM, true, true, LabyrinthBlocks.tile());
        HolderGetter<LabyrinthEncounter> encounters = context.lookup(LabyrinthEncounter.REGISTRY_KEY);
        List<EncounterEntry> pool = List.of(
                entry(encounters, LabyrinthEncounterBootstrap.WARDEN, WEIGHT_COMMON, Optional.empty()),
                entry(encounters, LabyrinthEncounterBootstrap.GOLEM, WEIGHT_COMMON, Optional.empty()),
                entry(encounters, LabyrinthEncounterBootstrap.CRIMSON_PORTAL, WEIGHT_UNCOMMON, Optional.empty()),
                entry(encounters, LabyrinthEncounterBootstrap.TAINT_SWARM, WEIGHT_UNCOMMON, Optional.empty()),
                entry(
                        encounters,
                        LabyrinthEncounterBootstrap.HIEROPHANT,
                        WEIGHT_RARE,
                        Optional.of(rooms.getOrThrow(TTLabyrinthRoomTags.BOSS_HALLS_OPEN))));
        context.register(
                ELDRITCH,
                new LabyrinthDefinition(
                        UniformInt.of(MIN_SIZE, MAX_SIZE),
                        LayoutSettings.DEFAULT,
                        pools,
                        pool,
                        Optional.of(processors.getOrThrow(LabyrinthProcessorBootstrap.ELDRITCH_PALETTE)),
                        Optional.of(processors.getOrThrow(LabyrinthProcessorBootstrap.ELDRITCH_DECOR)),
                        guardians,
                        gameplay));
    }

    private static EncounterEntry entry(
            HolderGetter<LabyrinthEncounter> encounters,
            ResourceKey<LabyrinthEncounter> key,
            int weight,
            Optional<HolderSet<RoomType>> halls) {
        return new EncounterEntry(encounters.getOrThrow(key), weight, halls);
    }
}
