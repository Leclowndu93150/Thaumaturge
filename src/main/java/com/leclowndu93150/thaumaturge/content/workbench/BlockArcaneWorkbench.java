package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.DeviceGate;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockArcaneWorkbench extends BaseEntityBlock {
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof BlockEntityArcaneWorkbench workbench) {
            Containers.dropContents(level, pos, workbench.getInventory());
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    public static final MapCodec<BlockArcaneWorkbench> CODEC = simpleCodec(BlockArcaneWorkbench::new);

    public static final VoxelShape SHAPE = Shapes.or(box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0), box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0), box(1.0, 4.0, 6.0, 15.0, 8.0, 10.0), box(6.0, 4.0, 1.0, 10.0, 8.0, 6.0),
            box(6.0, 4.0, 10.0, 10.0, 8.0, 15.0));

    public BlockArcaneWorkbench(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityArcaneWorkbench(blockPos, blockState);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && !DeviceGate.passes(player, TTIds.rl("gotdream"))) {
            return InteractionResult.SUCCESS_SERVER;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityArcaneWorkbench be) {
            player.openMenu(be, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }
}
