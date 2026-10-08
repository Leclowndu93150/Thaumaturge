package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeCells;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class RoomTransform {
    private RoomTransform() {}

    public static BlockPos origin(BlockPos minCorner, int transform, Vec3i size) {
        return StructureTemplate.getZeroPositionWithTransform(
                minCorner, MazeCells.mirror(transform), MazeCells.rotation(transform), size.getX(), size.getZ());
    }

    public static BlockPos toWorld(BlockPos local, int transform, BlockPos origin) {
        return StructureTemplate.transform(
                        local, MazeCells.mirror(transform), MazeCells.rotation(transform), BlockPos.ZERO)
                .offset(origin);
    }
}
