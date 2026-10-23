package com.leclowndu93150.thaumaturge.content.entity.ranger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class CrimsonPatrolSchedule extends SavedData {
    private static final long UNSCHEDULED = 0L;
    private static final String DATA_KEY = "data";
    private static final String NAME = "thaumaturge_crimson_patrols";

    public static final Codec<CrimsonPatrolSchedule> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(Codec.LONG.fieldOf("next_attempt").forGetter(schedule -> schedule.nextAttempt))
                    .apply(builder, CrimsonPatrolSchedule::new));
    public static final SavedData.Factory<CrimsonPatrolSchedule> FACTORY =
            new SavedData.Factory<>(CrimsonPatrolSchedule::new, CrimsonPatrolSchedule::load, DataFixTypes.LEVEL);

    private long nextAttempt;

    public CrimsonPatrolSchedule() {
        this(UNSCHEDULED);
    }

    private CrimsonPatrolSchedule(long nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public static CrimsonPatrolSchedule get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    private static CrimsonPatrolSchedule load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(NbtOps.INSTANCE, tag.get(DATA_KEY)).result().orElseGet(CrimsonPatrolSchedule::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(DATA_KEY, CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow());
        return tag;
    }

    public boolean isScheduled() {
        return nextAttempt != UNSCHEDULED;
    }

    public boolean isDue(long gameTime) {
        return gameTime >= nextAttempt;
    }

    public void schedule(long gameTime) {
        nextAttempt = gameTime;
        setDirty();
    }
}
