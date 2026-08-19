package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEldritchStructure extends Block {
    public static final MapCodec<BlockEldritchStructure> CODEC = simpleCodec(BlockEldritchStructure::new);

    private static final int SWEEP_XZ = 3;
    private static final int SWEEP_Y = 2;
    private static final float COLLAPSE_EXPLOSION = 1.0F;

    public BlockEldritchStructure(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockEldritchStructure> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (int xx = -SWEEP_XZ; xx <= SWEEP_XZ; xx++) {
            for (int yy = -SWEEP_Y; yy <= SWEEP_Y; yy++) {
                for (int zz = -SWEEP_XZ; zz <= SWEEP_XZ; zz++) {
                    BlockPos target = pos.offset(xx, yy, zz);
                    if (serverLevel.getBlockState(target).is(TCBlockTags.ELDRITCH_OBELISK_PARTS)) {
                        serverLevel.removeBlock(target, false);
                    }
                }
            }
        }
        serverLevel.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, COLLAPSE_EXPLOSION, Level.ExplosionInteraction.NONE);
    }
}
