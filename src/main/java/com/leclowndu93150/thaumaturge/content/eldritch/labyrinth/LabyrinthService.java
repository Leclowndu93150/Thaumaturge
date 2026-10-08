package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.LayoutContext;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.LayoutPlanner;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout.LayoutResult;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.SocketProfile;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world.OuterLandsChunkGenerator;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class LabyrinthService {
    private LabyrinthService() {}

    public static Optional<ServerLevel> outer(MinecraftServer server) {
        return Optional.ofNullable(server.getLevel(OuterLands.DIMENSION));
    }

    public static Optional<LabyrinthRuntime> runtime(ServerLevel outer) {
        return outer.getChunkSource().getGenerator() instanceof OuterLandsChunkGenerator generator
                ? Optional.of(generator.runtime())
                : Optional.empty();
    }

    public static void publish(ServerLevel outer) {
        runtime(outer)
                .ifPresent(runtime -> runtime.publish(LabyrinthData.get(outer).records().stream()
                        .map(MazeRecord::plan)
                        .toList()));
    }

    public static Optional<MazeRecord> find(ServerLevel level, BlockPos pos) {
        if (level.dimension() != OuterLands.DIMENSION) {
            return Optional.empty();
        }
        return runtime(level)
                .map(runtime -> runtime.index().planAtBlock(pos.getX(), pos.getZ()))
                .flatMap(plan -> LabyrinthData.get(level).get(plan.id()));
    }

    public static Optional<MazeRecord> byId(MinecraftServer server, MazeId id) {
        return outer(server).flatMap(outer -> LabyrinthData.get(outer).get(id));
    }

    public static boolean atCapacity(MinecraftServer server) {
        return outer(server)
                .map(outer -> LabyrinthData.get(outer).size() >= ThaumaturgeServerConfig.LABYRINTH.maxActiveMazes.get())
                .orElse(true);
    }

    public static Optional<MazeRecord> resolve(ServerLevel level, Optional<MazeId> bound, BlockPos pos) {
        return bound.flatMap(id -> byId(level.getServer(), id)).or(() -> find(level, pos));
    }

    public static Optional<LabyrinthDefinition> definition(MinecraftServer server, MazeRecord record) {
        return server.registryAccess()
                .lookupOrThrow(LabyrinthDefinition.REGISTRY_KEY)
                .get(record.plan().definition())
                .map(Holder::value);
    }

    public static boolean entitiesLoaded(ServerLevel level, BlockPos center, int radius) {
        for (int chunkX = SectionPos.blockToSectionCoord(center.getX() - radius);
                chunkX <= SectionPos.blockToSectionCoord(center.getX() + radius);
                chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(center.getZ() - radius);
                    chunkZ <= SectionPos.blockToSectionCoord(center.getZ() + radius);
                    chunkZ++) {
                if (!level.areEntitiesLoaded(ChunkPos.asLong(chunkX, chunkZ))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean retire(ServerLevel outer, MazeId id) {
        boolean removed = LabyrinthData.get(outer).remove(id);
        runtime(outer).ifPresent(runtime -> runtime.encounters().forget(id.value()));
        publish(outer);
        return removed;
    }

    public static int requeue(ServerLevel outer, MazePlan plan, BoundingBox area) {
        Optional<LabyrinthRuntime> runtime = runtime(outer);
        if (runtime.isEmpty()) {
            return 0;
        }
        return forLoadedChunks(
                outer,
                SectionPos.blockToSectionCoord(area.minX()),
                SectionPos.blockToSectionCoord(area.minZ()),
                SectionPos.blockToSectionCoord(area.maxX()),
                SectionPos.blockToSectionCoord(area.maxZ()),
                chunk -> {
                    MazePlan owner = runtime.get().index().planAt(chunk.getPos().x, chunk.getPos().z, 0);
                    if (owner == null || !owner.id().equals(plan.id())) {
                        return false;
                    }
                    chunk.setData(TTAttachments.LABYRINTH_STAMP, -1);
                    runtime.get().queueRepair(chunk.getPos().x, chunk.getPos().z);
                    return true;
                });
    }

    private static int forLoadedChunks(
            ServerLevel outer,
            int minChunkX,
            int minChunkZ,
            int maxChunkX,
            int maxChunkZ,
            Predicate<LevelChunk> action) {
        int touched = 0;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = outer.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk != null && action.test(chunk)) {
                    touched++;
                }
            }
        }
        return touched;
    }

    public static Optional<Holder.Reference<LabyrinthDefinition>> definition(
            MinecraftServer server, Optional<ResourceLocation> id) {
        HolderLookup.RegistryLookup<LabyrinthDefinition> registry =
                server.registryAccess().lookupOrThrow(LabyrinthDefinition.REGISTRY_KEY);
        ResourceLocation key = id.orElseGet(
                () -> ResourceLocation.tryParse(ThaumaturgeServerConfig.LABYRINTH.defaultDefinition.get()));
        return key == null ? Optional.empty() : registry.get(ResourceKey.create(LabyrinthDefinition.REGISTRY_KEY, key));
    }

    public static Predicate<Holder<RoomType>> roomFilter(MinecraftServer server, LabyrinthRuntime runtime) {
        RoomFilter config = new RoomFilter(ThaumaturgeServerConfig.LABYRINTH.disabledRooms.get());
        return config.and(
                room -> runtime.templates().usable(server.getStructureManager(), server.registryAccess(), room));
    }

    public static Optional<MazeRecord> open(
            MinecraftServer server, GlobalPos origin, Optional<ResourceLocation> definitionId) {
        Optional<ServerLevel> outer = outer(server);
        Optional<LabyrinthRuntime> runtime = outer.flatMap(LabyrinthService::runtime);
        if (outer.isEmpty() || runtime.isEmpty()) {
            Thaumaturge.LOGGER.error(
                    "Cannot open a labyrinth: the Outer Lands dimension is missing or does not use the labyrinth generator");
            return Optional.empty();
        }
        LabyrinthData data = LabyrinthData.get(outer.get());
        if (data.size() >= ThaumaturgeServerConfig.LABYRINTH.maxActiveMazes.get()) {
            return Optional.empty();
        }
        Optional<Holder.Reference<LabyrinthDefinition>> definition = definition(server, definitionId);
        if (definition.isEmpty()) {
            Thaumaturge.LOGGER.error(
                    "Cannot open a labyrinth: unknown definition {}",
                    definitionId
                            .map(ResourceLocation::toString)
                            .orElse(ThaumaturgeServerConfig.LABYRINTH.defaultDefinition.get()));
            return Optional.empty();
        }
        LabyrinthTuning tuning = LabyrinthTuning.fromConfig();
        MazeId id = data.nextId();
        ChunkPos chunk = MazeRegions.origin(data.nextRegion());
        RandomSource random = layoutRandom(outer.get(), chunk, id);
        LayoutResult layout;
        try {
            layout = LayoutPlanner.plan(
                    new LayoutContext(definition.get().value(), tuning, roomFilter(server, runtime.get()), random));
        } catch (RuntimeException e) {
            Thaumaturge.LOGGER.error(
                    "Labyrinth definition {} could not be laid out",
                    definition.get().key().location(),
                    e);
            return Optional.empty();
        }
        MazeGeometry geometry = new MazeGeometry(
                chunk,
                layout.width(),
                layout.depth(),
                runtime.get().band().baseY(),
                SocketProfile.HEIGHT,
                runtime.get().band().floorY());
        MazeAssembler.Assembly assembly = MazeAssembler.assemble(
                id,
                origin,
                geometry,
                random.nextLong(),
                definition.get().key(),
                tuning,
                layout,
                template ->
                        runtime.get().templates().get(server.getStructureManager(), server.registryAccess(), template));
        MazeRecord record =
                new MazeRecord(assembly.plan(), MazeState.fresh(outer.get().getGameTime(), assembly.triggers()));
        data.commit(record);
        publish(outer.get());
        queueLoadedChunks(outer.get(), runtime.get(), geometry);
        return Optional.of(record);
    }

    private static void queueLoadedChunks(ServerLevel outer, LabyrinthRuntime runtime, MazeGeometry geometry) {
        ChunkPos origin = geometry.origin();
        forLoadedChunks(
                outer, origin.x, origin.z, origin.x + geometry.width() - 1, origin.z + geometry.depth() - 1, chunk -> {
                    runtime.queueRepair(chunk.getPos().x, chunk.getPos().z);
                    return true;
                });
    }

    public static RandomSource layoutRandom(ServerLevel outer, ChunkPos chunk, MazeId id) {
        return outer.getChunkSource()
                .randomState()
                .getOrCreateRandomFactory(TTIds.LABYRINTH_LAYOUT_RANDOM)
                .at(chunk.x, id.value(), chunk.z);
    }
}
