package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockThaumatoriumTop extends BaseEntityBlock {
    public static final MapCodec<BlockThaumatoriumTop> CODEC = simpleCodec(BlockThaumatoriumTop::new);

    private static final Map<Direction, VoxelShape> SHAPES = lowered();

    public BlockThaumatoriumTop(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private static Map<Direction, VoxelShape> lowered() {
        Map<Direction, VoxelShape> lowered = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            lowered.put(facing, BlockThaumatorium.shapeFacing(facing).move(0.0, -1.0, 0.0));
        }
        return lowered;
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
        BlockState below = level.getBlockState(pos.below());
        Direction facing = below.hasProperty(HorizontalDirectionalBlock.FACING) ? below.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        return SHAPES.get(facing);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityThaumatoriumTop(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(TTBlocks.THAUMATORIUM.get())) {
            return below.getBlock() instanceof BlockThaumatorium thaumatorium ? thaumatorium.useWithoutItem(below, level, pos.below(), player, hitResult) : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            BlockPos below = pos.below();
            if (level.getBlockState(below).is(TTBlocks.THAUMATORIUM.get())) {
                level.destroyBlock(below, false);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        if (level.getBlockState(pos.below()).is(TTBlocks.THAUMATORIUM.get())) {
            level.destroyBlock(pos.below(), true);
        }
    }
}
