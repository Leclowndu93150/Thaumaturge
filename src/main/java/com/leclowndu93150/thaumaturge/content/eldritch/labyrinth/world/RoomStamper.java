package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthRuntime;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeCells;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeGeometry;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazePlan;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.StripMarkersProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.CompiledRoom;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomTransform;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class RoomStamper {
    private RoomStamper() {}

    public static void stamp(WorldGenLevel level, ChunkAccess chunk, MazePlan plan, LabyrinthRuntime runtime) {
        ChunkPos chunkPos = chunk.getPos();
        MazeGeometry geometry = plan.geometry();
        Optional<MazePlan.PlacedRoom> placed =
                plan.roomAt(geometry.cellOfChunkX(chunkPos.x), geometry.cellOfChunkZ(chunkPos.z));
        if (placed.isPresent()) {
            place(level, chunkPos, plan, runtime, placed.get());
        }
        chunk.setData(TTAttachments.LABYRINTH_STAMP, plan.id().value());
    }

    public static boolean isStamped(ChunkAccess chunk, MazePlan plan) {
        return chunk.getData(TTAttachments.LABYRINTH_STAMP) == plan.id().value();
    }

    public static void ensureStamped(ServerLevel level, ChunkAccess chunk, MazePlan plan, LabyrinthRuntime runtime) {
        if (!isStamped(chunk, plan)) {
            restamp(level, chunk, plan, runtime);
        }
    }

    public static void restamp(ServerLevel level, ChunkAccess chunk, MazePlan plan, LabyrinthRuntime runtime) {
        ChunkPos pos = chunk.getPos();
        BandFiller.fillLive(level, pos.x, pos.z, runtime.band());
        stamp(level, chunk, plan, runtime);
    }

    private static void place(
            WorldGenLevel level, ChunkPos chunkPos, MazePlan plan, LabyrinthRuntime runtime, MazePlan.PlacedRoom room) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }
        RegistryAccess registries = server.registryAccess();
        Optional<CompiledRoom> compiled =
                runtime.templates().get(server.getStructureManager(), registries, room.template());
        if (compiled.isEmpty()) {
            Thaumaturge.LOGGER.error(
                    "Labyrinth {} needs missing room template {}", plan.id().value(), room.template());
            return;
        }
        MazeGeometry geometry = plan.geometry();
        BlockPos origin = RoomTransform.origin(
                geometry.cellMin(room.anchorX(), room.anchorZ()),
                room.transform(),
                compiled.get().size());
        BoundingBox box = new BoundingBox(
                chunkPos.getMinBlockX(),
                level.getMinBuildHeight(),
                chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(),
                level.getMaxBuildHeight(),
                chunkPos.getMaxBlockZ());
        PositionalRandomFactory randoms =
                level.getLevel().getChunkSource().randomState().getOrCreateRandomFactory(TTIds.LABYRINTH_STAMP_RANDOM);
        RandomSource roomRandom = randoms.at(origin.getX(), plan.id().value(), origin.getZ());
        StructurePlaceSettings settings = new LabyrinthPlaceSettings(plan.seed(), plan.tuning())
                .setMirror(MazeCells.mirror(room.transform()))
                .setRotation(MazeCells.rotation(room.transform()))
                .setBoundingBox(box)
                .setKnownShape(true)
                .setIgnoreEntities(true)
                .setRandom(roomRandom);
        settings.addProcessor(StripMarkersProcessor.INSTANCE);
        Optional<Holder.Reference<RoomType>> type =
                registries.lookupOrThrow(RoomType.REGISTRY_KEY).get(room.type());
        type.flatMap(holder -> holder.value().processors()).ifPresent(list -> addAll(settings, list));
        Optional<Holder.Reference<LabyrinthDefinition>> definition =
                registries.lookupOrThrow(LabyrinthDefinition.REGISTRY_KEY).get(plan.definition());
        definition.flatMap(holder -> holder.value().palette()).ifPresent(list -> addAll(settings, list));
        if (type.map(holder -> holder.value().decorate()).orElse(true)) {
            definition.flatMap(holder -> holder.value().decoration()).ifPresent(list -> addAll(settings, list));
        }
        compiled.get().template().placeInWorld(level, origin, origin, settings, roomRandom, Block.UPDATE_CLIENTS);
        for (CompiledRoom.Marker marker : compiled.get().markers()) {
            if (marker.marker().phase() != MarkerPhase.STAMP) {
                continue;
            }
            BlockPos world = RoomTransform.toWorld(marker.local(), room.transform(), origin);
            if (!box.isInside(world) || plan.tuning().disables(marker.marker())) {
                continue;
            }
            marker.marker()
                    .stamp(
                            new StampContext(
                                    level,
                                    box,
                                    plan.id(),
                                    room.transform(),
                                    plan.tuning(),
                                    randoms.at(
                                            world.getX(),
                                            world.getY() ^ plan.id().value(),
                                            world.getZ())),
                            world);
        }
    }

    private static void addAll(StructurePlaceSettings settings, Holder<StructureProcessorList> list) {
        list.value().list().forEach(settings::addProcessor);
    }
}
