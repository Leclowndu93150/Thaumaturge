package com.leclowndu93150.thaumaturge.content.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockCandle extends AbstractCandleBlock {
    private static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 9.75, 10.5);
    private static final double FLAME_Y_OFFSET = 0.63;

    private final DyeColor dye;

    public BlockCandle(DyeColor dye, Properties properties) {
        super(FLAME_Y_OFFSET, properties);
        this.dye = dye;
    }

    public DyeColor dye() {
        return dye;
    }

    @Override
    protected boolean isBurning(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canStabiliseInfusion(Level level, BlockPos pos) {
        return true;
    }
}
