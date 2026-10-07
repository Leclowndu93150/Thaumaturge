package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GolemPressTests {
    private GolemPressTests() {}

    public static void register(TTTestRegistrar r) {
        r.add("golem/press_shapes", 20, helper -> {
            BlockPos corePos = helper.absolutePos(new BlockPos(2, 1, 2));
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                Direction right = facing.getClockWise();
                Direction back = facing.getOpposite();
                BlockPos[] positions = {corePos.above(), corePos.relative(right), corePos.relative(back), corePos.relative(right).relative(back)};
                Block[] blocks = {TTBlocks.PLACEHOLDER_IRON_BARS.get(), TTBlocks.PLACEHOLDER_TABLE.get(), TTBlocks.PLACEHOLDER_CAULDRON.get(), TTBlocks.PLACEHOLDER_ANVIL.get()};
                // Query before the controller exists to catch stale cached shapes.
                for (int i = 0; i < positions.length; i++) {
                    helper.getLevel().setBlock(positions[i], blocks[i].defaultBlockState(), Block.UPDATE_ALL);
                    blocks[i].defaultBlockState().getShape(helper.getLevel(), positions[i]);
                }
                helper.getLevel().setBlock(corePos, TTBlocks.GOLEM_BUILDER.get().defaultBlockState().setValue(BlockGolemBuilder.FACING, facing), Block.UPDATE_ALL);
                for (BlockPos pos : positions) {
                    BlockState state = helper.getLevel().getBlockState(pos);
                    VoxelShape outline = state.getShape(helper.getLevel(), pos);
                    VoxelShape collision = state.getCollisionShape(helper.getLevel(), pos, CollisionContext.empty());
                    if (outline.isEmpty() || outline == Shapes.block() || collision.isEmpty()) {
                        helper.fail("Press part has an empty or full-cube shape facing " + facing + " at " + pos);
                        return;
                    }
                }
                VoxelShape upper = helper.getLevel().getBlockState(corePos.above()).getShape(helper.getLevel(), corePos.above());
                if (upper.clip(new Vec3(0.5, 0.1, -1), new Vec3(0.5, 0.1, 2), BlockPos.ZERO) != null) {
                    helper.fail("Empty space beneath the press head can be selected facing " + facing);
                    return;
                }
                helper.getLevel().setBlock(corePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                for (BlockPos pos : positions) {
                    helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            helper.succeed();
        });
    }
}
