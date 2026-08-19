package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockJarVoid extends BlockJar {
    private static final Identifier SIDE_TEXTURE = Identifier.fromNamespaceAndPath("thaumaturge", "block/jar_side_void");
    private static final Identifier TOP_TEXTURE = Identifier.fromNamespaceAndPath("thaumaturge", "block/jar_top_void");

    @Override
    public Identifier jarSideTexture() {
        return SIDE_TEXTURE;
    }

    @Override
    public Identifier jarTopTexture() {
        return TOP_TEXTURE;
    }

    public static final MapCodec<BlockJarVoid> CODEC = simpleCodec(BlockJarVoid::new);

    public BlockJarVoid(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlockJar> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityJarVoid(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.JAR_VOID.get(), BlockEntityJarVoid::serverTick);
    }
}
