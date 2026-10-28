package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockJar extends AbstractBlockJar {
    public static final MapCodec<BlockJar> CODEC = simpleCodec(BlockJar::new);

    public BlockJar(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockJar> codec() {
        return CODEC;
    }

    @Override
    public BlockEntityType<AbstractBlockEntityJar> jarType() {
        return TTBlockEntities.JAR.get();
    }
}
