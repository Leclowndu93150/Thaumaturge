package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.content.infusion.BlockPedestal;
import com.leclowndu93150.thaumaturge.content.infusion.MatrixEnvironment;
import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import com.leclowndu93150.thaumaturge.content.world.space.BlocksMovedEvent;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;

public final class SableAssembly {
    private SableAssembly() {}

    public static Set<BlockPos> withRunicMatrices(ServerLevel level, Iterable<BlockPos> blocks) {
        Set<BlockPos> selected = new LinkedHashSet<>();
        for (BlockPos pos : blocks) {
            selected.add(pos.immutable());
        }
        var matrices = new ArrayList<BlockPos>();
        for (BlockPos pedestal : selected) {
            if (!(level.getBlockState(pedestal).getBlock() instanceof BlockPedestal)) continue;
            BlockPos matrix = pedestal.above(2);
            if (!selected.contains(matrix)
                    && level.getBlockState(matrix).is(TTBlocks.INFUSION_MATRIX.get())
                    && level.getBlockState(pedestal.above()).isAir()
                    && selected.contains(pedestal.offset(-1, 0, -1))
                    && selected.contains(pedestal.offset(-1, 0, 1))
                    && selected.contains(pedestal.offset(1, 0, -1))
                    && selected.contains(pedestal.offset(1, 0, 1))
                    && MatrixEnvironment.validLocation(level, matrix)) {
                matrices.add(matrix);
            }
        }
        selected.addAll(matrices);
        return selected;
    }

    public static BlockPos[] runicConnections(ServerLevel level, BlockPos pos, BlockPos[] offsets, boolean removed) {
        var state = level.getBlockState(pos);
        BlockPos link = null;
        if ((state.is(TTBlocks.INFUSION_MATRIX.get()) || (removed && state.isAir()))
                && level.getBlockState(pos.below()).isAir()
                && level.getBlockState(pos.below(2)).getBlock() instanceof BlockPedestal) {
            link = new BlockPos(0, -2, 0);
        } else if ((state.getBlock() instanceof BlockPedestal || (removed && state.isAir()))
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.above(2)).is(TTBlocks.INFUSION_MATRIX.get())) {
            link = new BlockPos(0, 2, 0);
        }
        if (link == null) return offsets;
        BlockPos[] joined = Arrays.copyOf(offsets, offsets.length + 1);
        joined[offsets.length] = link;
        return joined;
    }

    public static void blocksMoved(ServerLevel level, AssemblyTransform transform, Iterable<BlockPos> blocks) {
        Map<BlockPos, BlockPos> moves = new HashMap<>();
        for (BlockPos pos : blocks) {
            moves.put(pos.immutable(), transform.apply(pos).immutable());
        }
        if (!moves.isEmpty()) {
            NeoForge.EVENT_BUS.post(new BlocksMovedEvent(level, transform.getLevel(), moves, transform.getRotation()));
        }
    }

    public static void wardsMoved(ServerLevel level, AssemblyTransform transform, Iterable<BlockPos> blocks) {
        for (BlockPos from : blocks) {
            WardHandler.carry(level, from, transform.getLevel(), transform.apply(from));
        }
    }
}
