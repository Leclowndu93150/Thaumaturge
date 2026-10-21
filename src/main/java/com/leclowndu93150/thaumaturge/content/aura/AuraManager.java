package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class AuraManager {
    public static final int AURA_CEILING = 500;
    public static final Identifier AURA_PRESERVE_RESEARCH = TTIds.rl("aura_preserve");
    private static final float PRESERVE_RATIO = 0.1F;
    private static final int CHUNK_SHIFT = 4;
    private static final double PARTICLE_RANGE = 64.0;
    private static final double BLOCK_CENTER = 0.5;
    private static final Map<ResourceKey<Level>, DimensionState> STATES = new ConcurrentHashMap<>();

    private AuraManager() {}

    public static void onChunkLoaded(ServerLevel level, ChunkPos pos) {
        STATES.computeIfAbsent(level.dimension(), key -> new DimensionState()).loaded.add(pos);
    }

    public static void onChunkUnloaded(ServerLevel level, ChunkPos pos) {
        DimensionState state = STATES.get(level.dimension());
        if (state != null) {
            state.loaded.remove(pos);
        }
    }

    public static Set<ChunkPos> loadedChunksSnapshot(ServerLevel level) {
        DimensionState state = STATES.get(level.dimension());
        return state == null ? new HashSet<>() : new HashSet<>(state.loaded);
    }

    public static void queueRiftTrigger(ServerLevel level, BlockPos pos) {
        STATES.computeIfAbsent(level.dimension(), key -> new DimensionState()).riftTrigger.set(pos.immutable());
    }

    public static @Nullable BlockPos pollRiftTrigger(ServerLevel level) {
        DimensionState state = STATES.get(level.dimension());
        return state == null ? null : state.riftTrigger.getAndSet(null);
    }

    public static float withdrawFlux(Level level, BlockPos pos, float amount, boolean simulate) {
        return Pool.FLUX.drain(level, pos, amount, simulate);
    }

    public static float withdrawFlux(ServerLevel level, BlockPos pos, float amount, TransactionContext transaction) {
        if (!level.getServer().isSameThread() || !Float.isFinite(amount) || amount <= 0.0F) {
            return 0.0F;
        }
        AuraData found = chunkAt(level, pos);
        float stock = found == null ? 0.0F : found.getFlux();
        if (!Float.isFinite(stock) || stock <= 0.0F) {
            return 0.0F;
        }
        float taken = Math.min(amount, stock);
        found.setFlux(stock - taken, transaction);
        BlockPos position = pos.immutable();
        new RootCommitJournal(() -> markChunkDirty(level, position)).updateSnapshots(transaction);
        return taken;
    }

    public static float withdrawVis(Level level, BlockPos pos, float amount, boolean simulate) {
        return Pool.VIS.drain(level, pos, amount, simulate);
    }

    public static float withdrawVis(Level level, BlockPos pos, float amount, TransactionContext transaction) {
        AuraData found = chunkAt(level, pos);
        float stock = found == null ? 0.0F : found.getVis();
        if (amount <= 0.0F || stock <= 0.0F) {
            return 0.0F;
        }
        float taken = Math.min(amount, stock);
        found.setVis(stock - taken, transaction);
        new RootCommitJournal(() -> markChunkDirty(level, pos)).updateSnapshots(transaction);
        return taken;
    }

    public static void creditFlux(Level level, BlockPos pos, float amount) {
        Pool.FLUX.credit(level, pos, amount);
    }

    public static void creditVis(Level level, BlockPos pos, float amount) {
        Pool.VIS.credit(level, pos, amount);
    }

    public static boolean adjustFlux(Level level, BlockPos pos, @Nullable AuraData data, float amount, boolean apply) {
        return Pool.FLUX.shift(level, pos, data, amount, apply);
    }

    public static boolean adjustVis(Level level, BlockPos pos, @Nullable AuraData data, float amount, boolean apply) {
        return Pool.VIS.shift(level, pos, data, amount, apply);
    }

    public static void taint(Level level, BlockPos pos, float amount, boolean showEffect) {
        creditFlux(level, pos, amount);
        if (showEffect && amount > 0.0F && level instanceof ServerLevel server) {
            emitFume(server, pos);
        }
    }

    public static @Nullable AuraData chunkAt(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return null;
        }
        return chunkAt(server, chunkOf(pos));
    }

    public static @Nullable AuraData chunkAt(ServerLevel level, ChunkPos pos) {
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk == null) {
            return null;
        }
        AuraData found = chunk.getData(TTAttachments.AURA.get());
        found.setChunkPos(pos);
        return found;
    }

    public static int baseCapacity(Level level, BlockPos pos) {
        AuraData found = chunkAt(level, pos);
        return found == null ? 0 : found.getBase();
    }

    public static float fluxRatio(Level level, BlockPos pos) {
        int capacity = baseCapacity(level, pos);
        return capacity == 0 ? 0.0F : fluxAt(level, pos) / capacity;
    }

    public static float combinedLevel(Level level, BlockPos pos) {
        return visAt(level, pos) + fluxAt(level, pos);
    }

    public static float fluxAt(Level level, BlockPos pos) {
        return Pool.FLUX.sample(chunkAt(level, pos));
    }

    public static float visAt(Level level, BlockPos pos) {
        return Pool.VIS.sample(chunkAt(level, pos));
    }

    public static boolean isPreservationDue(Level level, @Nullable Player player, BlockPos pos) {
        AuraData found = chunkAt(level, pos);
        if (found == null || found.getBase() == 0) {
            return false;
        }
        boolean starved = found.getVis() / found.getBase() < PRESERVE_RATIO;
        if (!starved) {
            return false;
        }
        return player == null || KnowledgeAccess.of(player).isResearchComplete(AURA_PRESERVE_RESEARCH);
    }

    public static void resetSession() {
        STATES.clear();
    }

    static void markChunkDirty(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server) {
            markChunkDirty(server, chunkOf(pos));
        }
    }

    static void markChunkDirty(ServerLevel level, ChunkPos pos) {
        LevelChunk chunk = loadedChunk(level, pos);
        if (chunk != null) {
            chunk.markUnsaved();
        }
    }

    private static @Nullable LevelChunk loadedChunk(ServerLevel level, ChunkPos pos) {
        return level.getChunkSource().getChunkNow(pos.x(), pos.z());
    }

    private static ChunkPos chunkOf(BlockPos pos) {
        return new ChunkPos(pos.getX() >> CHUNK_SHIFT, pos.getZ() >> CHUNK_SHIFT);
    }

    private static void emitFume(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + BLOCK_CENTER;
        double cy = pos.getY() + BLOCK_CENTER;
        double cz = pos.getZ() + BLOCK_CENTER;
        ClientboundSpawnParticlePayload fume = new ClientboundSpawnParticlePayload(TTParticles.POLLUTION_FUME.get(), cx, cy, cz);
        PacketDistributor.sendToPlayersNear(level, null, cx, cy, cz, PARTICLE_RANGE, fume);
    }

    private static final class DimensionState {
        private final Set<ChunkPos> loaded = ConcurrentHashMap.newKeySet();
        private final AtomicReference<BlockPos> riftTrigger = new AtomicReference<>();
    }

    private enum Pool {
        VIS {
            @Override
            float read(AuraData data) {
                return data.getVis();
            }

            @Override
            void store(AuraData data, float value) {
                data.setVis(value);
            }
        },
        FLUX {
            @Override
            float read(AuraData data) {
                return data.getFlux();
            }

            @Override
            void store(AuraData data, float value) {
                data.setFlux(value);
            }
        };

        abstract float read(AuraData data);

        abstract void store(AuraData data, float value);

        float sample(@Nullable AuraData data) {
            return data == null ? 0.0F : read(data);
        }

        void credit(Level level, BlockPos pos, float amount) {
            if (amount >= 0.0F) {
                shift(level, pos, chunkAt(level, pos), amount, true);
            }
        }

        boolean shift(Level level, BlockPos pos, @Nullable AuraData data, float amount, boolean apply) {
            if (data == null) {
                return false;
            }
            if (apply) {
                store(data, Math.max(0.0F, read(data) + amount));
                markChunkDirty(level, pos);
            }
            return true;
        }

        float drain(Level level, BlockPos pos, float amount, boolean simulate) {
            AuraData found = chunkAt(level, pos);
            if (found == null) {
                return 0.0F;
            }
            float stock = read(found);
            float taken = Math.max(0.0F, Math.min(amount, stock));
            if (simulate || taken <= 0.0F) {
                return taken;
            }
            store(found, stock - taken);
            markChunkDirty(level, pos);
            return taken;
        }
    }
}
