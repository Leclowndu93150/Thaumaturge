package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEldritchNothingDormant extends Block {
    public static final MapCodec<BlockEldritchNothingDormant> CODEC = simpleCodec(BlockEldritchNothingDormant::new);

    public BlockEldritchNothingDormant(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEldritchNothingDormant> codec() {
        return CODEC;
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston) {
        if (!level.isClientSide() && !EldritchVoidContact.isEnclosed(level, pos)) {
            level.setBlock(pos, EldritchVoidContact.exposedState(), Block.UPDATE_ALL);
        }
    }
}
