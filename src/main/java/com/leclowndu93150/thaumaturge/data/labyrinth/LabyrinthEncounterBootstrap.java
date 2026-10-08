package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterScaling;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterSettings;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.SingleBossEncounter;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.SwarmEncounter;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;

public final class LabyrinthEncounterBootstrap {
    public static final ResourceKey<LabyrinthEncounter> WARDEN = key("warden");
    public static final ResourceKey<LabyrinthEncounter> GOLEM = key("golem");
    public static final ResourceKey<LabyrinthEncounter> CRIMSON_PORTAL = key("crimson_portal");
    public static final ResourceKey<LabyrinthEncounter> TAINT_SWARM = key("taint_swarm");
    public static final ResourceKey<LabyrinthEncounter> HIEROPHANT = key("hierophant");

    private static final int SWARM_COUNT = 3;
    private static final float SWARM_EXTRA_PER_PARTICIPANT = 0.5F;
    private static final float SWARM_HEALTH_PER_PARTICIPANT = 0.25F;
    private static final EncounterScaling SWARM_SCALING = new EncounterScaling(
            SWARM_HEALTH_PER_PARTICIPANT,
            EncounterScaling.DEFAULT.damagePerParticipant(),
            EncounterScaling.DEFAULT.maxCountedParticipants());
    private static final String TRANSLATION_PREFIX = "encounter";

    private LabyrinthEncounterBootstrap() {}

    public static void bootstrap(BootstrapContext<LabyrinthEncounter> context) {
        single(
                context,
                WARDEN,
                TTLootTables.LABYRINTH_WARDEN,
                BossEvent.BossBarColor.PURPLE,
                TTEntities.ELDRITCH_WARDEN.get());
        single(
                context,
                GOLEM,
                TTLootTables.LABYRINTH_GOLEM,
                BossEvent.BossBarColor.PURPLE,
                TTEntities.ELDRITCH_GOLEM.get());
        single(
                context,
                CRIMSON_PORTAL,
                TTLootTables.LABYRINTH_CRIMSON_PORTAL,
                BossEvent.BossBarColor.RED,
                TTEntities.CULTIST_PORTAL_GREATER.get());
        context.register(
                TAINT_SWARM,
                new SwarmEncounter(
                        settings(
                                TAINT_SWARM,
                                Optional.of(ArenaTemplates.TAINT),
                                TTLootTables.LABYRINTH_TAINT_SWARM,
                                SWARM_SCALING,
                                BossEvent.BossBarColor.PINK),
                        TTEntities.TAINTACLE_GIANT.get(),
                        SWARM_COUNT,
                        SWARM_EXTRA_PER_PARTICIPANT));
        single(
                context,
                HIEROPHANT,
                TTLootTables.LABYRINTH_HIEROPHANT,
                BossEvent.BossBarColor.PURPLE,
                TTEntities.ELDRITCH_HIEROPHANT.get());
    }

    public static String nameKey(ResourceKey<LabyrinthEncounter> key) {
        return Util.makeDescriptionId(TRANSLATION_PREFIX, key.location()) + ".name";
    }

    public static String announceKey(ResourceKey<LabyrinthEncounter> key) {
        return Util.makeDescriptionId(TRANSLATION_PREFIX, key.location()) + ".announce";
    }

    private static void single(
            BootstrapContext<LabyrinthEncounter> context,
            ResourceKey<LabyrinthEncounter> key,
            ResourceKey<LootTable> reward,
            BossEvent.BossBarColor color,
            EntityType<?> boss) {
        context.register(
                key,
                new SingleBossEncounter(
                        settings(key, Optional.empty(), reward, EncounterScaling.DEFAULT, color), boss));
    }

    private static EncounterSettings settings(
            ResourceKey<LabyrinthEncounter> key,
            Optional<ResourceLocation> arena,
            ResourceKey<LootTable> reward,
            EncounterScaling scaling,
            BossEvent.BossBarColor color) {
        return new EncounterSettings(
                Component.translatable(nameKey(key)),
                Component.translatable(announceKey(key)),
                arena,
                reward,
                scaling,
                EncounterSettings.DEFAULT_VICTORY_DELAY,
                color);
    }

    private static ResourceKey<LabyrinthEncounter> key(String path) {
        return ResourceKey.create(LabyrinthEncounter.REGISTRY_KEY, TTIds.rl(path));
    }
}
