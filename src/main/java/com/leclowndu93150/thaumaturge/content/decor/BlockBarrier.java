package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockBarrier extends Block {
    public static final MapCodec<BlockBarrier> CODEC = simpleCodec(BlockBarrier::new);

    public BlockBarrier(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockBarrier> codec() {
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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(context instanceof EntityCollisionContext entityContext)) {
            return Shapes.empty();
        }
        Entity entity = entityContext.getEntity();
        if (!(entity instanceof LivingEntity) || entity instanceof Player) {
            return Shapes.empty();
        }
        for (Entity passenger : entity.getIndirectPassengers()) {
            if (passenger instanceof Player) {
                return Shapes.empty();
            }
        }
        if (!(level instanceof Level realLevel)) {
            return Shapes.block();
        }
        int down = 1;
        if (!realLevel.getBlockState(pos.below(down)).is(TCBlocks.PAVING_STONE_BARRIER.get())) {
            down++;
        }
        return realLevel.hasNeighborSignal(pos.below(down)) ? Shapes.empty() : Shapes.block();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(TCBlocks.PAVING_STONE_BARRIER.get()) && !below.is(this)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return true;
    }
}
