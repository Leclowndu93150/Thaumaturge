package com.leclowndu93150.thaumaturge.content.device;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

/** Hopper-fed grate: it accepts insertion only from above while open, then drops the stack below. */
public final class BlockItemGrate extends BaseEntityBlock {
    public static final MapCodec<BlockItemGrate> CODEC = simpleCodec(BlockItemGrate::new);
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    public BlockItemGrate(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(OPEN, true));
    }

    @Override
    protected MapCodec<BlockItemGrate> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityItemGrate(pos, state);
    }

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moved) {
        boolean open = !level.hasNeighborSignal(pos);
        if (open != state.getValue(OPEN)) {
            level.setBlock(pos, state.setValue(OPEN, open), UPDATE_CLIENTS);
            if (open && level.getBlockEntity(pos) instanceof BlockEntityItemGrate grate) grate.eject();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }
}
