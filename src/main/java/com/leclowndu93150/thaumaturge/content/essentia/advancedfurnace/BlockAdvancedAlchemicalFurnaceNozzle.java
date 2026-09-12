package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Invisible output port for one side of an assembled Advanced Alchemical Furnace. */
public final class BlockAdvancedAlchemicalFurnaceNozzle extends BaseEntityBlock {
    public static final MapCodec<BlockAdvancedAlchemicalFurnaceNozzle> CODEC =
            simpleCodec(BlockAdvancedAlchemicalFurnaceNozzle::new);

    public BlockAdvancedAlchemicalFurnaceNozzle(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAdvancedAlchemicalFurnaceNozzle(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof BlockEntityAdvancedAlchemicalFurnaceNozzle nozzle
                && nozzle.controller() instanceof BlockEntityAdvancedAlchemicalFurnace furnace) {
            BlockEntityAdvancedAlchemicalFurnace.restoreStructure(level, furnace.getBlockPos(), pos);
            level.setBlock(
                    furnace.getBlockPos(),
                    com.leclowndu93150.thaumaturge.registry.TCBlocks.ALCHEMICAL_FURNACE
                            .get()
                            .defaultBlockState(),
                    Block.UPDATE_ALL);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
