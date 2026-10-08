package com.leclowndu93150.thaumaturge.client.model.connected;

import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jspecify.annotations.Nullable;

public abstract class AbstractConnectedModel implements IDynamicBakedModel {
    private static final ModelProperty<ConnectedQuads> QUADS = new ModelProperty<>();
    private final TextureAtlasSprite particle;
    private final boolean ambientOcclusion;
    private final ItemTransforms transforms;
    private final ChunkRenderTypeSet renderTypes;

    protected AbstractConnectedModel(
            TextureAtlasSprite particle, boolean ambientOcclusion, ItemTransforms transforms, RenderType renderType) {
        this.particle = particle;
        this.ambientOcclusion = ambientOcclusion;
        this.transforms = transforms;
        this.renderTypes = ChunkRenderTypeSet.of(renderType);
    }

    protected abstract ConnectedQuads collect(BlockGetter level, BlockPos pos, BlockState state);

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        return data.derive().with(QUADS, collect(level, pos, state)).build();
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random,
            ModelData data,
            @Nullable RenderType renderType) {
        ConnectedQuads quads = data.get(QUADS);
        if (quads == null)
            quads = collect(
                    EmptyBlockGetter.INSTANCE,
                    BlockPos.ZERO,
                    state == null ? Blocks.OAK_STAIRS.defaultBlockState() : state);
        return quads.get(side);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        return renderTypes;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
