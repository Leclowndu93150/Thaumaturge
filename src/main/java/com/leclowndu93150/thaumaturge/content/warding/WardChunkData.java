package com.leclowndu93150.thaumaturge.content.warding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import org.jspecify.annotations.Nullable;

public final class WardChunkData {
    public static final MapCodec<WardChunkData> CODEC =
            Entry.CODEC.listOf().optionalFieldOf("wards", List.of()).xmap(WardChunkData::new, WardChunkData::entries);

    private final Map<BlockPos, UUID> owners = new ConcurrentHashMap<>();

    public WardChunkData() {}

    private WardChunkData(List<Entry> entries) {
        for (Entry entry : entries) {
            owners.put(entry.pos(), entry.owner());
        }
    }

    public boolean isEmpty() {
        return owners.isEmpty();
    }

    public boolean contains(BlockPos pos) {
        return owners.containsKey(pos);
    }

    public @Nullable UUID owner(BlockPos pos) {
        return owners.get(pos);
    }

    public void put(BlockPos pos, UUID owner) {
        owners.put(pos.immutable(), owner);
    }

    public boolean remove(BlockPos pos) {
        return owners.remove(pos) != null;
    }

    public Map<BlockPos, UUID> owners() {
        return owners;
    }

    private List<Entry> entries() {
        return owners.entrySet().stream()
                .map(entry -> new Entry(entry.getKey(), entry.getValue()))
                .toList();
    }

    private record Entry(BlockPos pos, UUID owner) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                        UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner))
                .apply(inst, Entry::new));
    }
}
