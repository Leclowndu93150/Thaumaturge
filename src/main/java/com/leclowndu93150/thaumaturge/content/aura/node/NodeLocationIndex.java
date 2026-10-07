package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class NodeLocationIndex extends SavedData {
    private static final double REACHED_DISTANCE_SQ = 100.0;
    public static final Codec<NodeLocationIndex> CODEC = Entry.CODEC.listOf().fieldOf("nodes").codec().xmap(NodeLocationIndex::new,
            index -> index.nodes.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> new Entry(BlockPos.of(entry.getKey()), entry.getValue())).toList());
    public static final SavedDataType<NodeLocationIndex> TYPE = new SavedDataType<>(TTIds.rl("node_locations"), NodeLocationIndex::new, CODEC, DataFixTypes.LEVEL);

    private final Map<Long, NodeType> nodes = new HashMap<>();

    private NodeLocationIndex() {}

    private NodeLocationIndex(List<Entry> entries) {
        for (Entry entry : entries) {
            nodes.put(entry.pos().asLong(), entry.type());
        }
    }

    public static NodeLocationIndex get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void register(BlockPos pos, NodeType type) {
        if (nodes.put(pos.asLong(), type) != type) {
            setDirty();
        }
    }

    public void remove(BlockPos pos) {
        if (nodes.remove(pos.asLong()) != null) {
            setDirty();
        }
    }

    public Optional<BlockPos> findNearest(BlockPos origin, NodeType type) {
        return nodes.entrySet().stream().filter(entry -> entry.getValue() == type).map(entry -> BlockPos.of(entry.getKey())).filter(pos -> pos.distSqr(origin) > REACHED_DISTANCE_SQ)
                .min(Comparator.comparingDouble((BlockPos pos) -> pos.distSqr(origin)).thenComparingLong(BlockPos::asLong));
    }

    public Optional<BlockPos> findNearestAny(BlockPos origin, double maxDistance) {
        double maxDistanceSq = maxDistance * maxDistance;
        BlockPos nearest = null;
        double nearestSq = Double.MAX_VALUE;
        for (long packed : nodes.keySet()) {
            BlockPos pos = BlockPos.of(packed);
            double distSq = pos.distSqr(origin);
            if (distSq <= maxDistanceSq && distSq < nearestSq) {
                nearest = pos;
                nearestSq = distSq;
            }
        }
        return Optional.ofNullable(nearest);
    }

    private record Entry(BlockPos pos, NodeType type) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder
                .create(instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos), NodeType.CODEC.fieldOf("type").forGetter(Entry::type)).apply(instance, Entry::new));
    }
}
