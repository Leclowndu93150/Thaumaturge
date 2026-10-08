package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.BlockMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.EntryPortalMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GlyphMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.GuardianPostMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.KeyReliquaryMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LandmarkMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LockMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.LootMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.ReliquaryMarker;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker.SpawnerMarker;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTLabyrinthMarkers {
    public static final DeferredRegister<LabyrinthMarkerType<?>> MARKERS =
            DeferredRegister.create(LabyrinthMarkerType.REGISTRY_KEY, TTIds.MODID);
    private static final Registry<LabyrinthMarkerType<?>> REGISTRY =
            MARKERS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<LandmarkMarker>> LANDMARK =
            MARKERS.register("landmark", () -> new LabyrinthMarkerType<>(LandmarkMarker.CODEC));
    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<LootMarker>> LOOT =
            MARKERS.register("loot", () -> new LabyrinthMarkerType<>(LootMarker.CODEC));
    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<BlockMarker>> BLOCK =
            MARKERS.register("block", () -> new LabyrinthMarkerType<>(BlockMarker.CODEC));
    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<SpawnerMarker>> SPAWNER =
            MARKERS.register("spawner", () -> new LabyrinthMarkerType<>(SpawnerMarker.CODEC));
    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<GlyphMarker>> GLYPH =
            MARKERS.register("wayfinding_glyph", () -> new LabyrinthMarkerType<>(GlyphMarker.CODEC));
    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<LockMarker>> LOCK =
            MARKERS.register("lock", () -> new LabyrinthMarkerType<>(LockMarker.CODEC));
    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<KeyReliquaryMarker>> KEY_RELIQUARY =
            MARKERS.register("key_reliquary", () -> new LabyrinthMarkerType<>(KeyReliquaryMarker.CODEC));
    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<ReliquaryMarker>> RELIQUARY =
            MARKERS.register("reliquary", () -> new LabyrinthMarkerType<>(ReliquaryMarker.CODEC));
    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<EntryPortalMarker>> ENTRY_PORTAL =
            MARKERS.register("entry_portal", () -> new LabyrinthMarkerType<>(EntryPortalMarker.CODEC));
    public static final DeferredHolder<LabyrinthMarkerType<?>, LabyrinthMarkerType<GuardianPostMarker>> GUARDIAN_POST =
            MARKERS.register("guardian_post", () -> new LabyrinthMarkerType<>(GuardianPostMarker.CODEC));

    private TTLabyrinthMarkers() {}

    public static Registry<LabyrinthMarkerType<?>> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        MARKERS.register(modBus);
    }
}
