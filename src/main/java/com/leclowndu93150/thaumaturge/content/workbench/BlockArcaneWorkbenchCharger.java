package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockArcaneWorkbenchCharger extends Block {
    private static final VoxelShape SHAPE = Shapes.or(box(0.0, 0.0, 0.0, 2.0, 11.0, 2.0), box(14.0, 0.0, 0.0, 16.0, 11.0, 2.0), box(0.0, 0.0, 14.0, 2.0, 11.0, 16.0),
            box(14.0, 0.0, 14.0, 16.0, 11.0, 16.0), box(6.0, 9.0, 6.0, 10.0, 15.0, 10.0), box(1.0, 11.0, 1.0, 3.0, 13.0, 3.0), box(3.0, 11.0, 3.0, 5.0, 13.0, 5.0), box(5.0, 11.0, 5.0, 7.0, 13.0, 7.0),
            box(13.0, 11.0, 1.0, 15.0, 13.0, 3.0), box(11.0, 11.0, 3.0, 13.0, 13.0, 5.0), box(9.0, 11.0, 5.0, 11.0, 13.0, 7.0), box(1.0, 11.0, 13.0, 3.0, 13.0, 15.0),
            box(3.0, 11.0, 11.0, 5.0, 13.0, 13.0), box(5.0, 11.0, 9.0, 7.0, 13.0, 11.0), box(13.0, 11.0, 13.0, 15.0, 13.0, 15.0), box(11.0, 11.0, 11.0, 13.0, 13.0, 13.0),
            box(9.0, 11.0, 9.0, 11.0, 13.0, 11.0));

    public BlockArcaneWorkbenchCharger(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return willSurvive(level, pos);
    }

    private boolean willSurvive(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(TTBlockTags.ARCANE_WORKBENCH_CHARGER_HOSTS);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (!canSurvive(state, level, pos))
            return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && !DeviceGate.passes(player, TTIds.rl("workbench_charger"))) {
            return InteractionResult.SUCCESS_SERVER;
        }
        BlockPos host = pos.below();
        return level.getBlockState(host).useWithoutItem(level, player, hit.withPosition(host));
    }
}
