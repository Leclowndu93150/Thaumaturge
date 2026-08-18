package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;

/** Persistent index used by the node locator command. */
public final class NodeLocationIndex extends SavedData {
    private static final String DATA_NAME = "thaumaturge_node_locations";
    private static final double REACHED_DISTANCE_SQ = 10.0 * 10.0;
    private static final Pattern REGION_FILE = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
    private static final int REGION_HEADER_BYTES = 4096;
    private static final int LEGACY_CHUNKS_PER_TICK = 2;
    private static final SavedData.Factory<NodeLocationIndex> FACTORY =
            new SavedData.Factory<>(NodeLocationIndex::new, NodeLocationIndex::load, DataFixTypes.LEVEL);

    private final Map<NodeType, Set<Long>> nodes = new EnumMap<>(NodeType.class);
    private final Deque<Long> legacyChunks = new ArrayDeque<>();
    private boolean migrationInitialized;
    private boolean migrationComplete;
    private int migrationTotal;

    public NodeLocationIndex() {
        for (NodeType type : NodeType.values()) {
            nodes.put(type, new HashSet<>());
        }
    }

    public static NodeLocationIndex get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public void register(BlockPos pos, NodeType type) {
        long packed = pos.asLong();
        boolean changed = false;
        for (Map.Entry<NodeType, Set<Long>> entry : nodes.entrySet()) {
            if (entry.getKey() != type) {
                changed |= entry.getValue().remove(packed);
            }
        }
        changed |= nodes.get(type).add(packed);
        if (changed) {
            setDirty();
        }
    }

    public void remove(BlockPos pos) {
        long packed = pos.asLong();
        boolean changed = false;
        for (Set<Long> positions : nodes.values()) {
            changed |= positions.remove(packed);
        }
        if (changed) {
            setDirty();
        }
    }

    public Optional<BlockPos> findNearest(BlockPos origin, NodeType type) {
        return nodes.get(type).stream()
                .filter(pos -> BlockPos.of(pos).distSqr(origin) > REACHED_DISTANCE_SQ)
                .min(Comparator.comparingDouble(pos -> BlockPos.of(pos).distSqr(origin)))
                .map(BlockPos::of);
    }

    public boolean isMigrationComplete() {
        return migrationComplete;
    }

    public int migrationRemaining() {
        return legacyChunks.size();
    }

    public int migrationTotal() {
        return migrationTotal;
    }

    public void tickLegacyMigration(ServerLevel level) {
        initializeLegacyMigration(level);
        for (int i = 0; i < LEGACY_CHUNKS_PER_TICK && !legacyChunks.isEmpty(); i++) {
            ChunkPos pos = new ChunkPos(legacyChunks.removeFirst());
            // Every queued position came from a non-empty region-file header entry, so this loads
            // existing terrain without probing or generating coordinates outside the old save.
            ChunkAccess loaded = level.getChunkSource().getChunk(pos.x, pos.z, ChunkStatus.FULL, true);
            if (loaded instanceof LevelChunk chunk) {
                chunk.getBlockEntities().values().stream()
                        .filter(BlockEntityNode.class::isInstance)
                        .map(BlockEntityNode.class::cast)
                        .filter(node ->
                                node.getBlockState().is(com.leclowndu93150.thaumaturge.registry.TCBlocks.NODE.get()))
                        .forEach(node -> register(node.getBlockPos(), node.getNodeType()));
            }
        }
        if (legacyChunks.isEmpty() && !migrationComplete) {
            migrationComplete = true;
            setDirty();
        }
    }

    private void initializeLegacyMigration(ServerLevel level) {
        if (migrationInitialized || migrationComplete) {
            return;
        }
        migrationInitialized = true;
        Path root = level.getServer().getWorldPath(LevelResource.ROOT);
        Path regionDirectory =
                DimensionType.getStorageFolder(level.dimension(), root).resolve("region");
        if (!Files.isDirectory(regionDirectory)) {
            return;
        }
        try (var files = Files.list(regionDirectory)) {
            files.filter(Files::isRegularFile).forEach(this::readRegionHeader);
        } catch (IOException ignored) {
            // A later server session can retry if the save directory was temporarily unavailable.
            migrationInitialized = false;
            return;
        }
        migrationTotal = legacyChunks.size();
    }

    private void readRegionHeader(Path path) {
        Matcher matcher = REGION_FILE.matcher(path.getFileName().toString());
        if (!matcher.matches()) {
            return;
        }
        int regionX = Integer.parseInt(matcher.group(1));
        int regionZ = Integer.parseInt(matcher.group(2));
        ByteBuffer header = ByteBuffer.allocate(REGION_HEADER_BYTES);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            while (header.hasRemaining() && channel.read(header) >= 0) {
                // Fill the location table.
            }
        } catch (IOException ignored) {
            return;
        }
        header.flip();
        for (int index = 0; index < 1024 && header.remaining() >= Integer.BYTES; index++) {
            if (header.getInt() != 0) {
                int chunkX = regionX * 32 + index % 32;
                int chunkZ = regionZ * 32 + index / 32;
                legacyChunks.addLast(ChunkPos.asLong(chunkX, chunkZ));
            }
        }
    }

    private static NodeLocationIndex load(CompoundTag tag, HolderLookup.Provider registries) {
        NodeLocationIndex index = new NodeLocationIndex();
        index.migrationComplete = tag.getBoolean("LegacyMigrationComplete");
        ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
        for (Tag value : nodeList) {
            CompoundTag node = (CompoundTag) value;
            parseType(node.getString("Type"))
                    .ifPresent(type -> index.nodes.get(type).add(node.getLong("Pos")));
        }
        return index;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag nodeList = new ListTag();
        nodes.forEach((type, positions) -> positions.forEach(pos -> {
            CompoundTag node = new CompoundTag();
            node.putString("Type", type.getSerializedName());
            node.putLong("Pos", pos);
            nodeList.add(node);
        }));
        tag.put("Nodes", nodeList);

        tag.putBoolean("LegacyMigrationComplete", migrationComplete);
        return tag;
    }

    private static Optional<NodeType> parseType(String name) {
        for (NodeType type : NodeType.values()) {
            if (type.getSerializedName().equals(name)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
