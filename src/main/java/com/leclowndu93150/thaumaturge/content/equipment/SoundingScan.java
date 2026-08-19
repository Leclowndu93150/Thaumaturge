package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public final class SoundingScan {
    private static final int RANGE_BASE = 4;
    private static final int RANGE_PER_LEVEL = 4;
    private static final double DELAY_PER_DISTANCE = 3.0;

    private SoundingScan() {}

    public static void perform(ServerLevel level, ServerPlayer viewer, BlockPos source, int soundingLevel) {
        int range = RANGE_BASE + soundingLevel * RANGE_PER_LEVEL;
        Set<BlockPos> ores = new HashSet<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -range; xx <= range; xx++) {
            for (int yy = -range; yy <= range; yy++) {
                for (int zz = -range; zz <= range; zz++) {
                    cursor.set(source.getX() + xx, source.getY() + yy, source.getZ() + zz);
                    if (EnchantMining.isOre(level, cursor)) {
                        ores.add(cursor.immutable());
                    }
                }
            }
        }

        while (!ores.isEmpty()) {
            BlockPos start = ores.iterator().next();
            ores.remove(start);
            List<BlockPos> group = floodGroup(level, start, ores);
            int color = OreScanColors.of(level.getBlockState(start));
            double cx = 0.0;
            double cy = 0.0;
            double cz = 0.0;
            for (BlockPos p : group) {
                cx += p.getX() + 0.5;
                cy += p.getY() + 0.5;
                cz += p.getZ() + 0.5;
            }
            cx /= group.size();
            cy /= group.size();
            cz /= group.size();
            double distance = Math.sqrt(source.distToCenterSqr(cx, cy, cz));
            Effects.scanGlyph(viewer, cx, cy, cz, color, (int) (distance * DELAY_PER_DISTANCE));
        }
    }

    private static List<BlockPos> floodGroup(ServerLevel level, BlockPos start, Set<BlockPos> remaining) {
        List<BlockPos> group = new ArrayList<>();
        group.add(start);
        BlockState match = level.getBlockState(start);
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos neighbour = current.offset(x, y, z);
                        if (remaining.contains(neighbour) && level.getBlockState(neighbour).equals(match)) {
                            remaining.remove(neighbour);
                            group.add(neighbour);
                            queue.add(neighbour);
                        }
                    }
                }
            }
        }
        return group;
    }
}
