package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TubeGeometry {
    public static final int CORE_HIT = 6;

    private static final double PIXELS = 16.0;
    private static final double CENTER = 8.0;
    private static final double SURFACE_EPSILON = 1.0E-4;
    private static final double PIPE_CORE_HALF = 2.0;
    private static final double FILTER_CORE_HALF = 3.0;
    private static final double BUFFER_CORE_HALF = 4.0;
    private static final VoxelShape FLANGE = Block.box(5.0, 0.0, 5.0, 11.0, 2.0, 11.0);
    private static final VoxelShape ARM = Shapes.or(FLANGE, Block.box(6.0, 2.0, 6.0, 10.0, 6.0, 10.0));
    private static final VoxelShape RESTRICTED_ARM = Shapes.or(FLANGE, Block.box(7.0, 2.0, 7.0, 9.0, 6.0, 9.0));
    private static final VoxelShape INLET_MARKER = Block.box(5.0, 3.0, 5.0, 11.0, 5.0, 11.0);

    public static final TubeGeometry PIPE = new TubeGeometry(PIPE_CORE_HALF, ARM, Shapes.empty());
    public static final TubeGeometry RESTRICTED = new TubeGeometry(PIPE_CORE_HALF, RESTRICTED_ARM, Shapes.empty());
    public static final TubeGeometry FILTER = new TubeGeometry(FILTER_CORE_HALF, ARM, Shapes.empty());
    public static final TubeGeometry BUFFER = new TubeGeometry(BUFFER_CORE_HALF, ARM, Shapes.empty());
    public static final TubeGeometry ONEWAY = new TubeGeometry(PIPE_CORE_HALF, ARM, INLET_MARKER);

    private final double coreHalf;
    private final VoxelShape core;
    private final Map<Direction, VoxelShape> arms;
    private final Map<Direction, VoxelShape> inletMarkers;

    private TubeGeometry(double coreHalf, VoxelShape downArm, VoxelShape downInletMarker) {
        this.coreHalf = coreHalf;
        this.core = Block.box(
                CENTER - coreHalf,
                CENTER - coreHalf,
                CENTER - coreHalf,
                CENTER + coreHalf,
                CENTER + coreHalf,
                CENTER + coreHalf);
        this.arms = DeviceShapes.facingShapesFromDown(downArm);
        this.inletMarkers = DeviceShapes.facingShapesFromDown(downInletMarker);
    }

    public VoxelShape shape(BlockState state) {
        VoxelShape shape = core;
        for (Direction direction : Direction.values()) {
            if (state.getValue(BlockEssentiaTransport.propertyFor(direction))) {
                shape = Shapes.or(shape, arms.get(direction));
            }
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            Direction inlet = state.getValue(BlockStateProperties.FACING).getOpposite();
            if (state.getValue(BlockEssentiaTransport.propertyFor(inlet))) {
                shape = Shapes.or(shape, inletMarkers.get(inlet));
            }
        }
        return shape.optimize();
    }

    public int subHit(Vec3 local) {
        double dx = local.x - CENTER / PIXELS;
        double dy = local.y - CENTER / PIXELS;
        double dz = local.z - CENTER / PIXELS;
        double limit = coreHalf / PIXELS + SURFACE_EPSILON;
        if (Math.abs(dx) <= limit && Math.abs(dy) <= limit && Math.abs(dz) <= limit) {
            return CORE_HIT;
        }
        return Direction.getNearest((float) dx, (float) dy, (float) dz).ordinal();
    }
}
