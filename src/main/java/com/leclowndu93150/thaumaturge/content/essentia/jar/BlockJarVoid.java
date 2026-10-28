package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockJarVoid extends AbstractBlockJar {
    public static final MapCodec<BlockJarVoid> CODEC = simpleCodec(BlockJarVoid::new);
    private static final ResourceLocation SIDE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "block/jar_side_void");
    private static final ResourceLocation TOP_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "block/jar_top_void");

    public BlockJarVoid(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public ResourceLocation jarSideTexture() {
        return SIDE_TEXTURE;
    }

    @Override
    public ResourceLocation jarTopTexture() {
        return TOP_TEXTURE;
    }

    @Override
    protected MapCodec<BlockJarVoid> codec() {
        return CODEC;
    }

    @Override
    public BlockEntityType<BlockEntityJarVoid> jarType() {
        return TTBlockEntities.JAR_VOID.get();
    }
}
