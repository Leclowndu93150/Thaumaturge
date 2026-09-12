package com.leclowndu93150.thaumaturge.content.taint.ecology;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/** Tracks biome columns that were converted by a runtime taint outbreak rather than worldgen. */
public final class TaintBiomeState extends SavedData {
    private static final String DATA_NAME = "thaumaturge_taint_biomes";
    private static final String DYNAMIC_COLUMNS_TAG = "dynamic_columns";

    private static final SavedData.Factory<TaintBiomeState> FACTORY =
            new SavedData.Factory<>(TaintBiomeState::new, TaintBiomeState::load, DataFixTypes.LEVEL);

    private final Set<Long> dynamicColumns = new HashSet<>();

    public static TaintBiomeState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public boolean isDynamic(BlockPos pos) {
        return dynamicColumns.contains(columnKey(pos));
    }

    public void markDynamic(BlockPos pos) {
        if (dynamicColumns.add(columnKey(pos))) {
            setDirty();
        }
    }

    public void clearDynamic(BlockPos pos) {
        if (dynamicColumns.remove(columnKey(pos))) {
            setDirty();
        }
    }

    private static long columnKey(BlockPos pos) {
        return ChunkPos.asLong(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getZ()));
    }

    private static TaintBiomeState load(CompoundTag tag, HolderLookup.Provider registries) {
        TaintBiomeState state = new TaintBiomeState();
        for (long key : tag.getLongArray(DYNAMIC_COLUMNS_TAG)) {
            state.dynamicColumns.add(key);
        }
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] columns = new long[dynamicColumns.size()];
        int i = 0;
        for (long key : dynamicColumns) {
            columns[i++] = key;
        }
        tag.putLongArray(DYNAMIC_COLUMNS_TAG, columns);
        return tag;
    }
}
