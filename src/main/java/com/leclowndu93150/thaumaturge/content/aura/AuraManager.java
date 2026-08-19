package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

public final class AuraManager {
    public static final int AURA_CEILING = 500;
    public static final Identifier AURA_PRESERVE_RESEARCH = TCIds.rl("aura_preserve");

    private static final Map<ResourceKey<Level>, Set<ChunkPos>> LOADED_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, BlockPos> RIFT_TRIGGER = new ConcurrentHashMap<>();

    private AuraManager() {}

    public static @Nullable AuraData getAuraChunk(ServerLevel level, ChunkPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
        if (chunk == null) {
            return null;
        }
        return chunk.getData(TCAttachments.AURA.get());
    }

    public static @Nullable AuraData getAuraChunk(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return getAuraChunk(serverLevel, new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
    }

    public static float getTotalAura(Level level, BlockPos pos) {
        AuraData ac = getAuraChunk(level, pos);
        return ac != null ? ac.getVis() + ac.getFlux() : 0.0F;
    }

    public static float getFluxSaturation(Level level, BlockPos pos) {
        AuraData ac = getAuraChunk(level, pos);
        if (ac == null || ac.getBase() == 0) {
            return 0.0F;
        }
        return ac.getFlux() / ac.getBase();
    }

    public static float getVis(Level level, BlockPos pos) {
        AuraData ac = getAuraChunk(level, pos);
        return ac != null ? ac.getVis() : 0.0F;
    }

    public static float getFlux(Level level, BlockPos pos) {
        AuraData ac = getAuraChunk(level, pos);
        return ac != null ? ac.getFlux() : 0.0F;
    }

    public static int getAuraBase(Level level, BlockPos pos) {
        AuraData ac = getAuraChunk(level, pos);
        return ac != null ? ac.getBase() : 0;
    }

    public static boolean shouldPreserveAura(Level level, @Nullable Player player, BlockPos pos) {
        int base = getAuraBase(level, pos);
        if (base == 0) {
            return false;
        }
        boolean researched = player == null || KnowledgeAccess.of(player).isResearchComplete(AURA_PRESERVE_RESEARCH);
        return researched && getVis(level, pos) / base < 0.1F;
    }

    public static void addVis(Level level, BlockPos pos, float amount) {
        if (amount < 0.0F) {
            return;
        }
        AuraData ac = getAuraChunk(level, pos);
        modifyVisInChunk(level, pos, ac, amount, true);
    }

    public static void addFlux(Level level, BlockPos pos, float amount) {
        if (amount < 0.0F) {
            return;
        }
        AuraData ac = getAuraChunk(level, pos);
        modifyFluxInChunk(level, pos, ac, amount, true);
    }

    public static void polluteAura(Level level, BlockPos pos, float amount, boolean showEffect) {
        addFlux(level, pos, amount);
        if (!level.isClientSide() && showEffect && amount > 0F) {
            Effects.pollution((ServerLevel) level, pos).send();
        }
    }

    public static float drainVis(Level level, BlockPos pos, float amount, boolean simulate) {
        AuraData ac = getAuraChunk(level, pos);
        if (ac == null) {
            return 0.0F;
        }
        float take = Math.min(amount, ac.getVis());
        boolean ok = modifyVisInChunk(level, pos, ac, -take, !simulate);
        return ok ? take : 0.0F;
    }

    public static float drainFlux(Level level, BlockPos pos, float amount, boolean simulate) {
        AuraData ac = getAuraChunk(level, pos);
        if (ac == null) {
            return 0.0F;
        }
        float take = Math.min(amount, ac.getFlux());
        boolean ok = modifyFluxInChunk(level, pos, ac, -take, !simulate);
        return ok ? take : 0.0F;
    }

    public static boolean modifyVisInChunk(Level level, BlockPos pos, @Nullable AuraData ac, float amount, boolean doit) {
        if (ac == null) {
            return false;
        }
        if (doit) {
            ac.setVis(Math.max(0.0F, ac.getVis() + amount));
            markChunkDirty(level, pos);
        }
        return true;
    }

    public static boolean modifyFluxInChunk(Level level, BlockPos pos, @Nullable AuraData ac, float amount, boolean doit) {
        if (ac == null) {
            return false;
        }
        if (doit) {
            ac.setFlux(Math.max(0.0F, ac.getFlux() + amount));
            markChunkDirty(level, pos);
        }
        return true;
    }

    static void markChunkDirty(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk != null) {
            chunk.markUnsaved();
        }
    }

    static void markChunkDirty(ServerLevel level, ChunkPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
        if (chunk != null) {
            chunk.markUnsaved();
        }
    }

    public static void onChunkLoaded(ServerLevel level, ChunkPos pos) {
        LOADED_CHUNKS.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(pos);
    }

    public static void onChunkUnloaded(ServerLevel level, ChunkPos pos) {
        Set<ChunkPos> set = LOADED_CHUNKS.get(level.dimension());
        if (set != null) {
            set.remove(pos);
        }
    }

    public static Set<ChunkPos> loadedChunksSnapshot(ServerLevel level) {
        Set<ChunkPos> set = LOADED_CHUNKS.get(level.dimension());
        if (set == null) {
            return Set.of();
        }
        return new HashSet<>(set);
    }

    public static void queueRiftTrigger(ServerLevel level, BlockPos pos) {
        RIFT_TRIGGER.put(level.dimension(), pos);
    }

    public static @Nullable BlockPos pollRiftTrigger(ServerLevel level) {
        return RIFT_TRIGGER.remove(level.dimension());
    }
}
