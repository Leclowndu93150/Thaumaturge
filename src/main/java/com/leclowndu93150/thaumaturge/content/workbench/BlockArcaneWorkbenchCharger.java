package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockArcaneWorkbenchCharger extends Block {
    public BlockArcaneWorkbenchCharger(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return willSurvive(level, pos);
    }

    private boolean willSurvive(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).getBlock() instanceof BlockArcaneWorkbench;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (!canSurvive(state, level, pos))
            return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && !DeviceGate.passes(player, TCIds.rl("workbench_charger"))) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos.below()) instanceof BlockEntityArcaneWorkbench be) {
            player.openMenu(be, buf -> buf.writeBlockPos(pos.below()));
        }
        return InteractionResult.CONSUME;
    }
}
