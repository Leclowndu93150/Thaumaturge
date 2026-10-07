package com.leclowndu93150.thaumaturge.content.golem.press;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

class GolemPressShapesTest {
    @Test
    void controllerAndPartsUseLocalModelShapesInEveryRotation() {
        BlockGetter level = mock(BlockGetter.class);
        Map<BlockPos, BlockState> states = new HashMap<>();
        when(level.getBlockState(any(BlockPos.class)))
                .thenAnswer(call -> states.getOrDefault(call.getArgument(0), Blocks.AIR.defaultBlockState()));
        BlockPos core = new BlockPos(8, 4, 8);

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            states.clear();
            Direction right = facing.getClockWise();
            Direction back = facing.getOpposite();
            BlockPos[] positions = {
                core.above(),
                core.relative(right),
                core.relative(back),
                core.relative(right).relative(back)
            };
            Block[] blocks = {
                TTBlocks.PLACEHOLDER_IRON_BARS.get(),
                TTBlocks.PLACEHOLDER_TABLE.get(),
                TTBlocks.PLACEHOLDER_CAULDRON.get(),
                TTBlocks.PLACEHOLDER_ANVIL.get()
            };
            // Prime each part's shape before its controller exists.
            for (int i = 0; i < positions.length; i++) {
                states.put(positions[i], blocks[i].defaultBlockState());
                states.get(positions[i]).getShape(level, positions[i]);
            }
            states.put(
                    core, TTBlocks.GOLEM_BUILDER.get().defaultBlockState().setValue(BlockGolemBuilder.FACING, facing));
            VoxelShape base = states.get(core).getShape(level, core);
            assertLocal(base);
            assertNull(
                    base.clip(new Vec3(0.5, 1.5, -1), new Vec3(0.5, 1.5, 2), BlockPos.ZERO),
                    "Controller must not select the entire multiblock");

            for (BlockPos pos : positions) {
                BlockState state = states.get(pos);
                VoxelShape outline = state.getShape(level, pos);
                assertLocal(outline);
                assertTrue(
                        Shapes.joinIsNotEmpty(Shapes.block(), outline, BooleanOp.NOT_SAME),
                        "Placeholder must not retain a full-cube shape");
                assertFalse(Shapes.joinIsNotEmpty(
                        outline, state.getCollisionShape(level, pos, CollisionContext.empty()), BooleanOp.NOT_SAME));
            }
            VoxelShape upper = states.get(core.above()).getShape(level, core.above());
            assertNull(
                    upper.clip(new Vec3(0.5, 0.1, -1), new Vec3(0.5, 0.1, 2), BlockPos.ZERO),
                    "Empty space beneath the press head must not be selectable");
            assertNotNull(
                    upper.clip(new Vec3(0.5, 0.8, -1), new Vec3(0.5, 0.8, 2), BlockPos.ZERO),
                    "Press head must remain selectable");
        }
    }

    private static void assertLocal(VoxelShape shape) {
        assertFalse(shape.isEmpty());
        AABB bounds = shape.bounds();
        assertTrue(bounds.minX >= 0 && bounds.minY >= 0 && bounds.minZ >= 0);
        assertTrue(bounds.maxX <= 1 && bounds.maxY <= 1 && bounds.maxZ <= 1);
    }
}
