package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class ConnectedBlockStateModel extends AbstractConnectedModel {
    private static final Direction[] FACES = Direction.values();

    private final ConnectedFaceQuads quads;
    private final ConnectedQuads fixed;
    private final @Nullable TagKey<Block> group;

    public ConnectedBlockStateModel(
            ConnectedFaceQuads quads,
            ConnectedQuads fixed,
            TextureAtlasSprite particle,
            boolean ambientOcclusion,
            @Nullable TagKey<Block> group,
            ItemTransforms transforms,
            RenderType renderType) {
        super(particle, ambientOcclusion, transforms, renderType);
        this.quads = quads;
        this.fixed = fixed;
        this.group = group;
    }

    @Override
    protected ConnectedQuads collect(BlockGetter level, BlockPos pos, BlockState state) {
        ConnectedQuads.Builder builder = new ConnectedQuads.Builder().addAll(fixed);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction face : FACES) {
            if (quads.connects(face)) {
                quads.addFace(face, FaceConnections.mask(level, pos, state, face, group, cursor), builder);
            }
        }
        return builder.build();
    }
}
