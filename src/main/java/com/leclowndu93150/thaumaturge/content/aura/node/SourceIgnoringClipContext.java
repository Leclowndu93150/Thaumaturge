package com.leclowndu93150.thaumaturge.content.aura.node;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SourceIgnoringClipContext extends ClipContext {
    private final BlockPos source;

    public SourceIgnoringClipContext(
            BlockPos source, Vec3 from, Vec3 to, ClipContext.Block block, ClipContext.Fluid fluid) {
        super(from, to, block, fluid, CollisionContext.empty());
        this.source = source.immutable();
    }

    @Override
    public VoxelShape getBlockShape(BlockState blockState, BlockGetter level, BlockPos pos) {
        return pos.equals(source) ? Shapes.empty() : super.getBlockShape(blockState, level, pos);
    }
}
