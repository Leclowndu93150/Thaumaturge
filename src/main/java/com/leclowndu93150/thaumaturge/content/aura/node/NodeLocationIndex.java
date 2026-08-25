package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persistent index used by the node locator command. Unloaded chunks are never loaded to populate
 * it; {@link BlockEntityNode} registers nodes as their chunks naturally load.
 */
public final class NodeLocationIndex extends SavedData {
    private static final String DATA_NAME = "thaumaturge_node_locations";
    private static final double REACHED_DISTANCE_SQ = 10.0 * 10.0;
    private static final SavedData.Factory<NodeLocationIndex> FACTORY =
            new SavedData.Factory<>(NodeLocationIndex::new, NodeLocationIndex::load, DataFixTypes.LEVEL);

    private final Map<NodeType, Set<Long>> nodes = new EnumMap<>(NodeType.class);

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

    private static NodeLocationIndex load(CompoundTag tag, HolderLookup.Provider registries) {
        NodeLocationIndex index = new NodeLocationIndex();
        if (!tag.getBoolean("LegacyMigrationComplete")) {
            // Retire pending eager migration without doing any world or chunk IO.
            index.setDirty();
        }
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

        // Prevent affected older builds from restarting the removed eager migration.
        tag.putBoolean("LegacyMigrationComplete", true);

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
