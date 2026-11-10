package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.api.aura.IAuraChunk;
import com.leclowndu93150.thaumaturge.api.transaction.SnapshotJournal;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public final class AuraData implements IAuraChunk {
    public static final MapCodec<AuraData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.SHORT.optionalFieldOf("base", (short) 0).forGetter(AuraData::getBase),
                    Codec.FLOAT.optionalFieldOf("vis", 0.0F).forGetter(AuraData::getVis),
                    Codec.FLOAT.optionalFieldOf("flux", 0.0F).forGetter(AuraData::getFlux),
                    Codec.BOOL.optionalFieldOf("initialized", false).forGetter(AuraData::isInitialized),
                    Codec.INT.optionalFieldOf("rift_pressure_seconds", 0).forGetter(data -> data.riftPressureSeconds))
            .apply(instance, AuraData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AuraData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.SHORT,
            AuraData::getBase,
            ByteBufCodecs.FLOAT,
            AuraData::getVis,
            ByteBufCodecs.FLOAT,
            AuraData::getFlux,
            ByteBufCodecs.BOOL,
            AuraData::isInitialized,
            AuraData::new);

    private static final float MAX_POOL = 32766.0F;
    private static final int RIFT_BUILDUP_SECONDS = 90;
    private static final int VIS_SLOT = 0;
    private static final int FLUX_SLOT = 1;

    private final SnapshotJournal<Float> visJournal = new SnapshotJournal<>() {
        @Override
        protected Float createSnapshot() {
            return pools[VIS_SLOT];
        }

        @Override
        protected void revertToSnapshot(Float snapshot) {
            pools[VIS_SLOT] = snapshot;
        }
    };
    private final float[] pools = new float[2];
    private short base;
    private boolean initialized;
    private int riftPressureSeconds;
    private ChunkPos chunkPos = new ChunkPos(0, 0);

    public AuraData() {
        this((short) 0, 0.0F, 0.0F);
    }

    public AuraData(short base, float vis, float flux) {
        this(base, vis, flux, false);
    }

    private AuraData(short base, float vis, float flux, boolean initialized) {
        this(base, vis, flux, initialized, 0);
    }

    private AuraData(short base, float vis, float flux, boolean initialized, int riftPressureSeconds) {
        this.base = base;
        store(VIS_SLOT, vis);
        store(FLUX_SLOT, flux);
        this.initialized = initialized || base != 0;
        this.riftPressureSeconds = Mth.clamp(riftPressureSeconds, 0, RIFT_BUILDUP_SECONDS);
    }

    private static float clampPool(float value) {
        return Mth.clamp(value, 0.0F, MAX_POOL);
    }

    private void store(int slot, float value) {
        pools[slot] = clampPool(value);
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public short getBase() {
        return base;
    }

    public void setBase(short newBase) {
        initialized = true;
        base = newBase;
    }

    @Override
    public float getVis() {
        return pools[VIS_SLOT];
    }

    public void setVis(float vis) {
        store(VIS_SLOT, vis);
    }

    public void setVis(float vis, TransactionContext transaction) {
        visJournal.updateSnapshots(transaction);
        store(VIS_SLOT, vis);
    }

    @Override
    public float getFlux() {
        return pools[FLUX_SLOT];
    }

    public void setFlux(float flux) {
        store(FLUX_SLOT, flux);
    }

    boolean updateRiftPressure(boolean overloaded, boolean entityTicking) {
        int previous = riftPressureSeconds;
        if (!overloaded) {
            riftPressureSeconds = 0;
        } else if (entityTicking && !hasRiftPressure()) {
            riftPressureSeconds++;
        }
        return riftPressureSeconds != previous;
    }

    boolean hasRiftPressure() {
        return riftPressureSeconds == RIFT_BUILDUP_SECONDS;
    }

    @Override
    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public void setChunkPos(ChunkPos pos) {
        chunkPos = pos;
    }
}
