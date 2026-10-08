package com.leclowndu93150.thaumaturge.content.aura.relay;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class RelaySightline {
    private RelaySightline() {}

    static boolean isClear(Level level, BlockPos from, BlockPos to) {
        Vec3 start = Vec3.atCenterOf(from);
        Vec3 end = Vec3.atCenterOf(to);
        Boolean blocked = BlockGetter.traverseBlocks(
                start,
                end,
                level,
                (context, pos) -> blocks(context, pos, from, to, start, end) ? Boolean.TRUE : null,
                context -> Boolean.FALSE);
        return !blocked;
    }

    private static boolean blocks(Level level, BlockPos pos, BlockPos from, BlockPos to, Vec3 start, Vec3 end) {
        if (pos.equals(from) || pos.equals(to)) {
            return false;
        }
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        return !shape.isEmpty() && shape.clip(start, end, pos) != null;
    }
}
