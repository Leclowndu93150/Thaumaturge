package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.content.world.space.BlocksMovedEvent;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;

public final class SableAssembly {
    private SableAssembly() {}

    public static void blocksMoved(ServerLevel level, AssemblyTransform transform, Iterable<BlockPos> blocks) {
        Map<BlockPos, BlockPos> moves = new HashMap<>();
        for (BlockPos pos : blocks) {
            moves.put(pos.immutable(), transform.apply(pos).immutable());
        }
        if (!moves.isEmpty()) {
            NeoForge.EVENT_BUS.post(new BlocksMovedEvent(level, transform.getLevel(), moves, transform.getRotation()));
        }
    }
}
