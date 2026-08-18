package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent index used by the node locator command. */
public final class NodeLocationIndex extends SavedData {
    private static final String DATA_NAME = "thaumaturge_node_locations";
    private static final double REACHED_DISTANCE_SQ = 10.0 * 10.0;
    private static final SavedData.Factory<NodeLocationIndex> FACTORY =
            new SavedData.Factory<>(NodeLocationIndex::new, NodeLocationIndex::load, DataFixTypes.LEVEL);

    private final Map<NodeType, Set<Long>> nodes = new EnumMap<>(NodeType.class);
    private final Map<UUID, Set<Long>> reachedNodes = new HashMap<>();
    private final Map<UUID, Long> lastLocatedNodes = new HashMap<>();

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
            lastLocatedNodes.values().removeIf(value -> value == packed);
            reachedNodes.values().forEach(positions -> positions.remove(packed));
            setDirty();
        }
    }

    public Optional<BlockPos> findNearest(UUID playerId, BlockPos origin, NodeType type) {
        markLastLocatedReached(playerId, origin);
        Set<Long> reached = reachedNodes.getOrDefault(playerId, Set.of());
        Optional<Long> nearest = nodes.get(type).stream()
                .filter(pos -> !reached.contains(pos))
                .min(Comparator.comparingDouble(pos -> BlockPos.of(pos).distSqr(origin)));
        nearest.ifPresent(pos -> {
            lastLocatedNodes.put(playerId, pos);
            setDirty();
        });
        return nearest.map(BlockPos::of);
    }

    private void markLastLocatedReached(UUID playerId, BlockPos origin) {
        Long last = lastLocatedNodes.get(playerId);
        if (last == null || BlockPos.of(last).distSqr(origin) > REACHED_DISTANCE_SQ) {
            return;
        }
        reachedNodes.computeIfAbsent(playerId, ignored -> new HashSet<>()).add(last);
        lastLocatedNodes.remove(playerId);
        setDirty();
    }

    private static NodeLocationIndex load(CompoundTag tag, HolderLookup.Provider registries) {
        NodeLocationIndex index = new NodeLocationIndex();
        ListTag nodeList = tag.getList("Nodes", Tag.TAG_COMPOUND);
        for (Tag value : nodeList) {
            CompoundTag node = (CompoundTag) value;
            parseType(node.getString("Type"))
                    .ifPresent(type -> index.nodes.get(type).add(node.getLong("Pos")));
        }
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (Tag value : players) {
            CompoundTag player = (CompoundTag) value;
            if (!player.hasUUID("Id")) {
                continue;
            }
            UUID id = player.getUUID("Id");
            Set<Long> reached = new HashSet<>();
            for (Tag packed : player.getList("Reached", Tag.TAG_STRING)) {
                try {
                    reached.add(Long.parseLong(packed.getAsString()));
                } catch (NumberFormatException ignored) {
                    // Ignore malformed legacy/user-edited entries.
                }
            }
            if (!reached.isEmpty()) {
                index.reachedNodes.put(id, reached);
            }
            if (player.contains("Last", Tag.TAG_LONG)) {
                index.lastLocatedNodes.put(id, player.getLong("Last"));
            }
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

        ListTag players = new ListTag();
        Set<UUID> playerIds = new HashSet<>(reachedNodes.keySet());
        playerIds.addAll(lastLocatedNodes.keySet());
        for (UUID id : playerIds) {
            CompoundTag player = new CompoundTag();
            player.putUUID("Id", id);
            ListTag reached = new ListTag();
            reachedNodes.getOrDefault(id, Set.of()).stream()
                    .map(String::valueOf)
                    .map(StringTag::valueOf)
                    .forEach(reached::add);
            player.put("Reached", reached);
            Long last = lastLocatedNodes.get(id);
            if (last != null) {
                player.putLong("Last", last);
            }
            players.add(player);
        }
        tag.put("Players", players);
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
