package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.api.aura.IAuraChunk;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class AuraData implements IAuraChunk {
    public static final MapCodec<AuraData> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance
                    .group(Codec.SHORT.optionalFieldOf("base", (short) 0).forGetter(AuraData::getBase), Codec.FLOAT.optionalFieldOf("vis", 0.0F).forGetter(AuraData::getVis),
                            Codec.FLOAT.optionalFieldOf("flux", 0.0F).forGetter(AuraData::getFlux), Codec.BOOL.optionalFieldOf("initialized", false).forGetter(AuraData::isInitialized),
                            Codec.LONG.optionalFieldOf("baseline_revision", 0L).forGetter(data -> data.baselineRevision),
                            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.LONG).optionalFieldOf("baseline_receipts", Map.of()).forGetter(data -> data.baselineReceipts))
                    .apply(instance, AuraData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AuraData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.SHORT, AuraData::getBase, ByteBufCodecs.FLOAT, AuraData::getVis,
            ByteBufCodecs.FLOAT, AuraData::getFlux, ByteBufCodecs.BOOL, AuraData::isInitialized, AuraData::new);

    private static final float MAX_POOL = 32766.0F;
    private static final int VIS_SLOT = 0;
    private static final int FLUX_SLOT = 1;

    private final VisJournal visJournal = new VisJournal();
    private final FluxJournal fluxJournal = new FluxJournal();
    private final BaselineJournal baselineJournal = new BaselineJournal();
    private final Map<UUID, Long> baselineReceipts = new HashMap<>();
    private long baselineRevision;
    private boolean baselinePending;
    private final float[] pools = new float[2];
    private short base;
    private boolean initialized;
    private ChunkPos chunkPos = new ChunkPos(0, 0);

    public AuraData() {
        this((short) 0, 0.0F, 0.0F);
    }

    public AuraData(short base, float vis, float flux) {
        this(base, vis, flux, false);
    }

    private AuraData(short base, float vis, float flux, boolean initialized) {
        this.base = base;
        store(VIS_SLOT, vis);
        store(FLUX_SLOT, flux);
        this.initialized = initialized || base != 0;
    }

    private AuraData(short base, float vis, float flux, boolean initialized, long revision, Map<UUID, Long> receipts) {
        this(base, vis, flux, initialized);
        baselineRevision = revision;
        baselineReceipts.putAll(receipts);
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
        baselineRevision++;
    }

    long baselineRevision() {
        return baselineRevision;
    }

    boolean baselinePending() {
        return baselinePending;
    }

    long lastBaselineOperation(UUID authority) {
        return baselineReceipts.getOrDefault(authority, 0L);
    }

    boolean canRecordBaseline(UUID authority) {
        return baselineReceipts.containsKey(authority) || baselineReceipts.size() < 128;
    }

    void recalculateBase(short newBase, UUID authority, long sequence, TransactionContext transaction) {
        baselineJournal.updateSnapshots(transaction);
        baselinePending = true;
        setBase(newBase);
        baselineReceipts.put(authority, sequence);
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

    public void setFlux(float flux, TransactionContext transaction) {
        fluxJournal.updateSnapshots(transaction);
        store(FLUX_SLOT, flux);
    }

    @Override
    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public void setChunkPos(ChunkPos pos) {
        chunkPos = pos;
    }

    private record BaselineSnapshot(short base, boolean initialized, long revision, Map<UUID, Long> receipts, boolean pending) {
    }

    private final class BaselineJournal extends SnapshotJournal<BaselineSnapshot> {
        @Override
        protected BaselineSnapshot createSnapshot() {
            return new BaselineSnapshot(base, initialized, baselineRevision, Map.copyOf(baselineReceipts), baselinePending);
        }

        @Override
        protected void revertToSnapshot(BaselineSnapshot snapshot) {
            base = snapshot.base();
            initialized = snapshot.initialized();
            baselineRevision = snapshot.revision();
            baselineReceipts.clear();
            baselineReceipts.putAll(snapshot.receipts());
            baselinePending = snapshot.pending();
        }

        @Override
        protected void onRootCommit(BaselineSnapshot original) {
            baselinePending = false;
        }
    }

    private final class FluxJournal extends SnapshotJournal<Float> {
        @Override
        protected Float createSnapshot() {
            return pools[FLUX_SLOT];
        }

        @Override
        protected void revertToSnapshot(Float snapshot) {
            pools[FLUX_SLOT] = snapshot;
        }
    }

    private final class VisJournal extends SnapshotJournal<Float> {
        @Override
        protected Float createSnapshot() {
            return pools[VIS_SLOT];
        }

        @Override
        protected void revertToSnapshot(Float snapshot) {
            pools[VIS_SLOT] = snapshot;
        }
    }
}
