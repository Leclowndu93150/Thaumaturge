package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockJarCreative extends BlockJar {
    public static final MapCodec<BlockJarCreative> CODEC = simpleCodec(BlockJarCreative::new);

    public BlockJarCreative(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockJar> codec() {
        return CODEC;
    }
}
