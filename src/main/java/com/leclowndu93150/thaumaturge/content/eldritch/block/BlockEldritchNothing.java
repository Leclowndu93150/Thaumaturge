package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchNothing extends Block implements EntityBlock {
    public static final MapCodec<BlockEldritchNothing> CODEC = simpleCodec(BlockEldritchNothing::new);
    public static final BooleanProperty EXPOSED = BooleanProperty.create("exposed");

    private static final VoxelShape COLLISION = Block.box(2.0, 2.0, 2.0, 14.0, 14.0, 14.0);

    public BlockEldritchNothing(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(EXPOSED, Boolean.FALSE));
    }

    @Override
    protected MapCodec<BlockEldritchNothing> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EXPOSED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEldritchNothing(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        if (EldritchVoidContact.isEnclosed(level, pos)) {
            level.setBlock(pos, EldritchVoidContact.dormantState(), Block.UPDATE_ALL);
        } else if (!state.getValue(EXPOSED)) {
            level.setBlock(pos, state.setValue(EXPOSED, Boolean.TRUE), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        EldritchVoidContact.touch(level, entity);
    }
}
