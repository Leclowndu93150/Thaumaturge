package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class DustTriggerTickHandler {
    private static final int MAX_SWAPS_PER_LEVEL_TICK = 32;

    private DustTriggerTickHandler() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        if (!chunk.hasData(TTAttachments.DUST_TRIGGER_QUEUE.get())) {
            return;
        }
        DustTriggerSwapQueue queue = chunk.getData(TTAttachments.DUST_TRIGGER_QUEUE.get());
        if (queue.isEmpty()) {
            return;
        }
        DustTriggerSwapQueue.markChunkActive(level, chunk.getPos());
        for (PendingSwap entry : queue.entries()) {
            DustTriggerSwapQueue.markBlocked(level, entry.pos());
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            tickLevel(level);
        }
    }

    private static void tickLevel(ServerLevel level) {
        Set<ChunkPos> active = DustTriggerSwapQueue.activeChunks(level);
        if (active.isEmpty()) {
            return;
        }
        int budget = MAX_SWAPS_PER_LEVEL_TICK;
        for (ChunkPos cp : active) {
            if (!level.hasChunk(cp.x, cp.z)) {
                continue;
            }
            LevelChunk chunk = level.getChunk(cp.x, cp.z);
            DustTriggerSwapQueue queue = chunk.getData(TTAttachments.DUST_TRIGGER_QUEUE.get());
            if (queue.isEmpty()) {
                DustTriggerSwapQueue.markChunkClear(level, cp);
                continue;
            }
            budget -= tickChunk(level, chunk, queue, budget);
        }
    }

    private static int tickChunk(ServerLevel level, LevelChunk chunk, DustTriggerSwapQueue queue, int budget) {
        List<PendingSwap> entries = queue.entries();
        List<PendingSwap> next = new ArrayList<>(entries.size());
        List<BlockPos> changed = new ArrayList<>();
        int processed = 0;
        for (PendingSwap entry : entries) {
            int remaining = Math.max(0, entry.ticksRemaining() - 1);
            if (remaining == 0 && processed < budget) {
                processed++;
                if (complete(level, entry)) changed.add(entry.pos());
                continue;
            }
            // Due entries can wait at zero until the next tick without losing their order.
            next.add(entry.withTicks(remaining));
        }
        entries.clear();
        entries.addAll(next);
        if (!changed.isEmpty()) {
            Effects.bamf(level, changed).withSound().fancy().send();
        }
        chunk.setUnsaved(true);
        if (queue.isEmpty()) {
            DustTriggerSwapQueue.markChunkClear(level, chunk.getPos());
        }
        return processed;
    }

    private static boolean complete(ServerLevel level, PendingSwap entry) {
        BlockPos pos = entry.pos();
        if (!level.getBlockState(pos).is(entry.originalState().getBlock())) {
            DustTriggerSwapQueue.clearBlocked(level, pos);
            return false;
        }
        BlockState resultState = entry.stateResult();
        ItemStack resultStack = entry.stackResult();
        if (resultState != null) {
            level.setBlock(pos, resultState, 3);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if (resultStack != null && !resultStack.isEmpty()) {
                ItemStack copy = resultStack.copy();
                EntitySpecialItem drop =
                        new EntitySpecialItem(level, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, copy);
                drop.setDeltaMovement(0.0, 0.0, 0.0);
                drop.setDefaultPickUpDelay();
                level.addFreshEntity(drop);
            }
        }
        DustTriggerSwapQueue.clearBlocked(level, pos);
        return true;
    }
}
