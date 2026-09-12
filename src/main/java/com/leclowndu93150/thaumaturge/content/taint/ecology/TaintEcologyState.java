package com.leclowndu93150.thaumaturge.content.taint.ecology;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent, dimension-local ecological taint pressure, stored only for contaminated chunks. */
public final class TaintEcologyState extends SavedData {
    private static final String DATA_NAME = "thaumaturge_taint_ecology";
    private static final String ENTRIES_TAG = "entries";
    private static final float MINIMUM_SATURATION = 0.0001F;
    private static final float DECAY_PER_TICK = 0.05F / 24000.0F;

    private static final SavedData.Factory<TaintEcologyState> FACTORY =
            new SavedData.Factory<>(TaintEcologyState::new, TaintEcologyState::load, DataFixTypes.LEVEL);

    private final Map<Long, Entry> entries = new HashMap<>();

    public static TaintEcologyState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public float getSaturation(BlockPos pos, long gameTime, float decayMultiplier) {
        long key = chunkKey(pos);
        Entry entry = entries.get(key);
        if (entry == null) {
            return 0.0F;
        }
        float saturation = decay(entry, gameTime, decayMultiplier);
        if (saturation < MINIMUM_SATURATION) {
            entries.remove(key);
            setDirty();
            return 0.0F;
        }
        return saturation;
    }

    public float addPressure(BlockPos pos, float amount, long gameTime, boolean activeSeed) {
        if (amount <= 0.0F) {
            return getSaturation(pos, gameTime, 1.0F);
        }
        long key = chunkKey(pos);
        Entry entry = entries.computeIfAbsent(key, ignored -> new Entry(0.0F, gameTime, 0L));
        decay(entry, gameTime, 1.0F);
        entry.saturation = Mth.clamp(entry.saturation + amount, 0.0F, 1.0F);
        if (activeSeed) {
            entry.lastActiveSeedTick = gameTime;
        }
        setDirty();
        return entry.saturation;
    }

    public float clean(BlockPos pos, float amount, long gameTime) {
        if (amount <= 0.0F) {
            return getSaturation(pos, gameTime, 1.0F);
        }
        long key = chunkKey(pos);
        Entry entry = entries.get(key);
        if (entry == null) {
            return 0.0F;
        }
        decay(entry, gameTime, 1.0F);
        entry.saturation = Math.max(0.0F, entry.saturation - amount);
        if (entry.saturation < MINIMUM_SATURATION) {
            entries.remove(key);
            setDirty();
            return 0.0F;
        }
        setDirty();
        return entry.saturation;
    }

    public void setSaturation(BlockPos pos, float saturation, long gameTime) {
        long key = chunkKey(pos);
        float clamped = Mth.clamp(saturation, 0.0F, 1.0F);
        if (clamped < MINIMUM_SATURATION) {
            if (entries.remove(key) != null) {
                setDirty();
            }
            return;
        }
        Entry entry = entries.computeIfAbsent(key, ignored -> new Entry(clamped, gameTime, 0L));
        entry.saturation = clamped;
        entry.lastUpdateTick = gameTime;
        setDirty();
    }

    private float decay(Entry entry, long gameTime, float multiplier) {
        long elapsed = Math.max(0L, gameTime - entry.lastUpdateTick);
        if (elapsed > 0L) {
            entry.saturation = Math.max(0.0F, entry.saturation - elapsed * DECAY_PER_TICK * multiplier);
            entry.lastUpdateTick = gameTime;
        }
        return entry.saturation;
    }

    private static long chunkKey(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static TaintEcologyState load(CompoundTag tag, HolderLookup.Provider registries) {
        TaintEcologyState state = new TaintEcologyState();
        ListTag entries = tag.getList(ENTRIES_TAG, Tag.TAG_COMPOUND);
        for (Tag rawEntry : entries) {
            CompoundTag saved = (CompoundTag) rawEntry;
            float saturation = Mth.clamp(saved.getFloat("saturation"), 0.0F, 1.0F);
            if (saturation >= MINIMUM_SATURATION) {
                state.entries.put(
                        saved.getLong("chunk"),
                        new Entry(saturation, saved.getLong("last_update"), saved.getLong("last_seed")));
            }
        }
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag savedEntries = new ListTag();
        Iterator<Map.Entry<Long, Entry>> iterator = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Entry> mapEntry = iterator.next();
            Entry entry = mapEntry.getValue();
            if (entry.saturation < MINIMUM_SATURATION) {
                iterator.remove();
                continue;
            }
            CompoundTag saved = new CompoundTag();
            saved.putLong("chunk", mapEntry.getKey());
            saved.putFloat("saturation", entry.saturation);
            saved.putLong("last_update", entry.lastUpdateTick);
            saved.putLong("last_seed", entry.lastActiveSeedTick);
            savedEntries.add(saved);
        }
        tag.put(ENTRIES_TAG, savedEntries);
        return tag;
    }

    private static final class Entry {
        private float saturation;
        private long lastUpdateTick;
        private long lastActiveSeedTick;

        private Entry(float saturation, long lastUpdateTick, long lastActiveSeedTick) {
            this.saturation = saturation;
            this.lastUpdateTick = lastUpdateTick;
            this.lastActiveSeedTick = lastActiveSeedTick;
        }
    }
}
