package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockThaumatoriumTop extends BaseEntityBlock {
    public static final MapCodec<BlockThaumatoriumTop> CODEC = simpleCodec(BlockThaumatoriumTop::new);
    private static final VoxelShape SHAPE = Shapes.box(0.0, -1.0, 0.0, 1.0, 1.0, 1.0);

    public BlockThaumatoriumTop(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockThaumatoriumTop> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityThaumatoriumTop(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(TCBlocks.THAUMATORIUM.get())) {
            return below.getBlock() instanceof BlockThaumatorium thaumatorium
                    ? thaumatorium.useWithoutItem(below, level, pos.below(), player, hitResult)
                    : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {

            if (level.getBlockState(pos.below()).is(TCBlocks.THAUMATORIUM.get())) {
                level.destroyBlock(pos.below(), true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
