package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeCells;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomTransforms;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

final class Dihedral {
    private static final int COUNT = 8;

    private Dihedral() {}

    static boolean allowed(RoomTransforms transforms, int transform) {
        Rotation rotation = MazeCells.rotation(transform);
        boolean mirrored = MazeCells.mirror(transform) != Mirror.NONE;
        return transforms.rotations().contains(rotation) && (!mirrored || transforms.mirror());
    }

    static IntList allowedTransforms(RoomTransforms transforms) {
        IntList list = new IntArrayList(COUNT);
        for (int transform = 0; transform < COUNT; transform++) {
            if (allowed(transforms, transform)) {
                list.add(transform);
            }
        }
        return list;
    }

    static int width(int width, int depth, int transform) {
        return swaps(transform) ? depth : width;
    }

    static int depth(int width, int depth, int transform) {
        return swaps(transform) ? width : depth;
    }

    private static boolean swaps(int transform) {
        Rotation rotation = MazeCells.rotation(transform);
        return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
    }

    static BlockPos cell(int cellX, int cellZ, int width, int depth, int transform) {
        Mirror mirror = MazeCells.mirror(transform);
        Rotation rotation = MazeCells.rotation(transform);
        BlockPos moved = StructureTemplate.transform(new BlockPos(cellX, 0, cellZ), mirror, rotation, BlockPos.ZERO);
        return moved.offset(
                StructureTemplate.getZeroPositionWithTransform(BlockPos.ZERO, mirror, rotation, width, depth));
    }

    static Direction side(Direction side, int transform) {
        return MazeCells.orient(transform, side);
    }

    static int mask(RoomType room, int transform) {
        int mask = 0;
        for (RoomSocket socket : room.sockets()) {
            mask |= MazeCells.bit(side(socket.side(), transform));
        }
        return mask;
    }
}
