package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockJarCreative extends AbstractBlockJar {
    public static final MapCodec<BlockJarCreative> CODEC = simpleCodec(BlockJarCreative::new);

    public BlockJarCreative(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockJarCreative> codec() {
        return CODEC;
    }

    @Override
    public BlockEntityType<BlockEntityJarCreative> jarType() {
        return TTBlockEntities.JAR_CREATIVE.get();
    }
}
