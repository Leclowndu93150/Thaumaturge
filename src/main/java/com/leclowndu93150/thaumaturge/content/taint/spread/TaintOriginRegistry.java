package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;

public final class TaintOriginRegistry extends SavedData {
    public static final Codec<TaintOriginRegistry> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(BlockPos.CODEC.listOf().fieldOf("seeds").forGetter(TaintOriginRegistry::snapshot))
                    .apply(builder, TaintOriginRegistry::new));

    public static final SavedData.Factory<TaintOriginRegistry> FACTORY =
            new SavedData.Factory<>(TaintOriginRegistry::new, TaintOriginRegistry::load, DataFixTypes.LEVEL);

    private static final String DATA_KEY = "data";
    private static final String NAME = "thaumaturge_taint_seeds";

    private final Set<BlockPos> origins = new LinkedHashSet<>();
    private final Long2ObjectMap<List<BlockPos>> byChunk = new Long2ObjectOpenHashMap<>();

    public TaintOriginRegistry() {}

    private TaintOriginRegistry(List<BlockPos> saved) {
        for (BlockPos pos : saved) {
            index(pos.immutable());
        }
    }

    public static TaintOriginRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    private static TaintOriginRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(NbtOps.INSTANCE, tag.get(DATA_KEY)).result().orElseGet(TaintOriginRegistry::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(DATA_KEY, CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow());
        return tag;
    }

    public void add(BlockPos pos) {
        if (index(pos.immutable())) {
            setDirty();
        }
    }

    public void move(@Nullable BlockPos previous, BlockPos current) {
        if (previous != null && !previous.equals(current)) {
            remove(previous);
        }
        add(current);
    }

    public void remove(BlockPos pos) {
        if (!origins.remove(pos)) {
            return;
        }
        long key = ChunkPos.asLong(pos);
        List<BlockPos> bucket = byChunk.get(key);
        if (bucket != null) {
            bucket.remove(pos);
            if (bucket.isEmpty()) {
                byChunk.remove(key);
            }
        }
        setDirty();
    }

    public @Nullable List<BlockPos> inChunk(int chunkX, int chunkZ) {
        return byChunk.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    private boolean index(BlockPos pos) {
        if (!origins.add(pos)) {
            return false;
        }
        byChunk.computeIfAbsent(ChunkPos.asLong(pos), key -> new ArrayList<>()).add(pos);
        return true;
    }

    private List<BlockPos> snapshot() {
        return List.copyOf(origins);
    }
}
