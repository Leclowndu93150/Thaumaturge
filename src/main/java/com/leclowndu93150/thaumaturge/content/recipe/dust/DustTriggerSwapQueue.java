package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class DustTriggerSwapQueue {
    public static final MapCodec<DustTriggerSwapQueue> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(PendingSwap.CODEC.listOf().optionalFieldOf("pending", List.of()).forGetter(q -> List.copyOf(q.pending))).apply(i, DustTriggerSwapQueue::fromCodec));

    private static final Map<ResourceKey<Level>, Set<ChunkPos>> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Set<Long>> BLOCKED = new ConcurrentHashMap<>();

    private final List<PendingSwap> pending = new ArrayList<>();

    public DustTriggerSwapQueue() {}

    private static DustTriggerSwapQueue fromCodec(List<PendingSwap> entries) {
        DustTriggerSwapQueue queue = new DustTriggerSwapQueue();
        queue.pending.addAll(entries);
        return queue;
    }

    public List<PendingSwap> entries() {
        return this.pending;
    }

    public void add(PendingSwap entry) {
        this.pending.add(entry);
    }

    public boolean isEmpty() {
        return this.pending.isEmpty();
    }

    public Iterator<PendingSwap> iterator() {
        return this.pending.iterator();
    }

    public static void enqueueDrop(ServerLevel level, BlockPos pos, BlockState originalState, ItemStack result, int delayTicks) {
        enqueueInternal(level, PendingSwap.dropItem(pos, originalState, result, delayTicks));
    }

    public static void enqueuePlace(ServerLevel level, BlockPos pos, BlockState originalState, BlockState placed, int delayTicks) {
        enqueueInternal(level, PendingSwap.placeBlock(pos, originalState, placed, delayTicks));
    }

    public static void enqueueClear(ServerLevel level, BlockPos pos, BlockState originalState, int delayTicks) {
        enqueueInternal(level, PendingSwap.clearOnly(pos, originalState, delayTicks));
    }

    private static void enqueueInternal(ServerLevel level, PendingSwap entry) {
        ChunkPos cp = ChunkPos.containing(entry.pos());
        LevelChunk chunk = level.getChunk(cp.x(), cp.z());
        DustTriggerSwapQueue queue = chunk.getData(TCAttachments.DUST_TRIGGER_QUEUE.get());
        queue.add(entry);
        chunk.markUnsaved();
        ACTIVE.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(cp);
        markBlocked(level, entry.pos());
    }

    public static Set<ChunkPos> activeChunks(ServerLevel level) {
        Set<ChunkPos> set = ACTIVE.get(level.dimension());
        if (set == null) {
            return Set.of();
        }
        return new HashSet<>(set);
    }

    public static void markChunkActive(ServerLevel level, ChunkPos pos) {
        ACTIVE.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(pos);
    }

    public static void markChunkClear(ServerLevel level, ChunkPos pos) {
        Set<ChunkPos> set = ACTIVE.get(level.dimension());
        if (set != null) {
            set.remove(pos);
        }
    }

    public static void markBlocked(ServerLevel level, BlockPos pos) {
        BLOCKED.computeIfAbsent(level.dimension(), k -> ConcurrentHashMap.newKeySet()).add(pos.asLong());
    }

    public static void clearBlocked(ServerLevel level, BlockPos pos) {
        Set<Long> set = BLOCKED.get(level.dimension());
        if (set != null) {
            set.remove(pos.asLong());
        }
    }

    public static boolean isBlocked(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel sl)) {
            return false;
        }
        Set<Long> set = BLOCKED.get(sl.dimension());
        return set != null && set.contains(pos.asLong());
    }
}
