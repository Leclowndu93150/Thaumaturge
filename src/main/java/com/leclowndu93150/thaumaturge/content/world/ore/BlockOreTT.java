package com.leclowndu93150.thaumaturge.content.world.ore;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockOreTT extends Block {
    public static final MapCodec<BlockOreTT> CODEC = simpleCodec(BlockOreTT::new);

    public BlockOreTT(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockOreTT> codec() {
        return CODEC;
    }
}
