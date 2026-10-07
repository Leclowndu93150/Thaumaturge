package com.leclowndu93150.thaumaturge.content.device;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class DeviceShapes {
    private static final BoxMapper QUARTER_X =
            box -> new AABB(box.minX, box.minZ, 1 - box.maxY, box.maxX, box.maxZ, 1 - box.minY);

    private static final BoxMapper QUARTER_Y =
            box -> new AABB(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX);

    private DeviceShapes() {}

    public static VoxelShape rotate(VoxelShape shape, int xQuarters, int yQuarters) {
        VoxelShape out = shape;
        for (int i = 0; i < xQuarters; i++) {
            out = transform(out, QUARTER_X);
        }
        for (int i = 0; i < yQuarters; i++) {
            out = transform(out, QUARTER_Y);
        }
        return out;
    }

    public static Map<Direction, VoxelShape> facingShapesFromNorth(VoxelShape northShape) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, northShape);
        shapes.put(Direction.EAST, rotate(northShape, 0, 1));
        shapes.put(Direction.SOUTH, rotate(northShape, 0, 2));
        shapes.put(Direction.WEST, rotate(northShape, 0, 3));
        shapes.put(Direction.DOWN, rotate(northShape, 1, 0));
        shapes.put(Direction.UP, rotate(northShape, 3, 0));
        return shapes;
    }

    public static Map<Direction, VoxelShape> facingShapesFromUp(VoxelShape upShape) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.UP, upShape);
        shapes.put(
                Direction.DOWN,
                transform(
                        upShape,
                        box -> new AABB(box.minX, 1 - box.maxY, 1 - box.maxZ, box.maxX, 1 - box.minY, 1 - box.minZ)));
        VoxelShape north =
                transform(upShape, box -> new AABB(box.minX, box.minZ, 1 - box.maxY, box.maxX, box.maxZ, 1 - box.minY));
        shapes.put(Direction.NORTH, north);
        shapes.put(
                Direction.SOUTH,
                transform(
                        north,
                        box -> new AABB(1 - box.maxX, box.minY, 1 - box.maxZ, 1 - box.minX, box.maxY, 1 - box.minZ)));
        shapes.put(
                Direction.WEST,
                transform(north, box -> new AABB(box.minZ, box.minY, 1 - box.maxX, box.maxZ, box.maxY, 1 - box.minX)));
        shapes.put(
                Direction.EAST,
                transform(north, box -> new AABB(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX)));
        return shapes;
    }

    public static Map<Direction, VoxelShape> facingShapesFromDown(VoxelShape downShape) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.DOWN, downShape);
        shapes.put(
                Direction.UP,
                transform(
                        downShape,
                        box -> new AABB(box.minX, 1 - box.maxY, 1 - box.maxZ, box.maxX, 1 - box.minY, 1 - box.minZ)));
        VoxelShape south = transform(
                downShape, box -> new AABB(box.minX, box.minZ, 1 - box.maxY, box.maxX, box.maxZ, 1 - box.minY));
        shapes.put(Direction.SOUTH, south);
        shapes.put(
                Direction.NORTH,
                transform(
                        south,
                        box -> new AABB(1 - box.maxX, box.minY, 1 - box.maxZ, 1 - box.minX, box.maxY, 1 - box.minZ)));
        shapes.put(
                Direction.WEST,
                transform(south, box -> new AABB(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX)));
        shapes.put(
                Direction.EAST,
                transform(south, box -> new AABB(box.minZ, box.minY, 1 - box.maxX, box.maxZ, box.maxY, 1 - box.minX)));
        return shapes;
    }

    private interface BoxMapper {
        AABB map(AABB box);
    }

    private static VoxelShape transform(VoxelShape shape, BoxMapper mapper) {
        VoxelShape out = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            out = Shapes.or(out, Shapes.create(mapper.map(box)));
        }
        return out;
    }
}
