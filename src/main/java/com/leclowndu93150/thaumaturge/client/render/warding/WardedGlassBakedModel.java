package com.leclowndu93150.thaumaturge.client.render.warding;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/** TC4's forty-seven-texture connected model, evaluated from neighbouring warded glass blocks. */
public final class WardedGlassBakedModel implements IDynamicBakedModel {
    public static final int TEXTURE_COUNT = 47;
    private static final ModelProperty<int[]> CONNECTIONS = new ModelProperty<>();
    private static final int[] CONNECTED_TEXTURES = {
        0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0,
        0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4,
        5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43,
        38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25,
        40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27,
        14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14,
        4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39,
        33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11
    };

    private static final Direction[] FACES = Direction.values();
    private static final Vector3f FROM = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final Vector3f TO = new Vector3f(16.0F, 16.0F, 16.0F);

    private final BakedQuad[][] quads;
    private final TextureAtlasSprite particle;
    private final ChunkRenderTypeSet renderTypes = ChunkRenderTypeSet.of(RenderType.translucent());

    public WardedGlassBakedModel(TextureAtlasSprite[] sprites, TextureAtlasSprite particle, ModelState modelState) {
        this.particle = particle;
        this.quads = new BakedQuad[FACES.length][TEXTURE_COUNT];
        FaceBakery bakery = new FaceBakery();
        for (Direction face : FACES) {
            for (int texture = 0; texture < TEXTURE_COUNT; texture++) {
                BlockElementFace elementFace = new BlockElementFace(
                        face, BlockElementFace.NO_TINT, "ctm", new BlockFaceUV(new float[] {0, 0, 16, 16}, 0));
                quads[face.ordinal()][texture] =
                        bakery.bakeQuad(FROM, TO, elementFace, sprites[texture], face, modelState, null, true);
            }
        }
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        int[] connections = new int[FACES.length];
        for (Direction face : FACES) {
            connections[face.ordinal()] = CONNECTED_TEXTURES[connectionMask(level, pos, face)];
        }
        return modelData.derive().with(CONNECTIONS, connections).build();
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random,
            ModelData data,
            @Nullable RenderType renderType) {
        if (side == null || state == null) {
            return Collections.emptyList();
        }
        int[] connections = data.get(CONNECTIONS);
        int texture = connections == null ? 0 : connections[side.ordinal()];
        return List.of(quads[side.ordinal()][texture]);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        return renderTypes;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    private static int connectionMask(BlockAndTintGetter level, BlockPos pos, Direction face) {
        int mask = 0;
        for (int bit = 0; bit < 8; bit++) {
            if (level.getBlockState(offset(pos, face, bit)).is(TCBlocks.WARDED_GLASS.get())) {
                mask |= 1 << bit;
            }
        }
        return mask;
    }

    private static BlockPos offset(BlockPos pos, Direction face, int bit) {
        return switch (face) {
            case DOWN, UP ->
                switch (bit) {
                    case 0 -> pos.offset(-1, 0, -1);
                    case 1 -> pos.north();
                    case 2 -> pos.offset(1, 0, -1);
                    case 3 -> pos.west();
                    case 4 -> pos.east();
                    case 5 -> pos.offset(-1, 0, 1);
                    case 6 -> pos.south();
                    default -> pos.offset(1, 0, 1);
                };
            case NORTH ->
                switch (bit) {
                    case 0 -> pos.offset(1, 1, 0);
                    case 1 -> pos.above();
                    case 2 -> pos.offset(-1, 1, 0);
                    case 3 -> pos.east();
                    case 4 -> pos.west();
                    case 5 -> pos.offset(1, -1, 0);
                    case 6 -> pos.below();
                    default -> pos.offset(-1, -1, 0);
                };
            case SOUTH ->
                switch (bit) {
                    case 0 -> pos.offset(-1, 1, 0);
                    case 1 -> pos.above();
                    case 2 -> pos.offset(1, 1, 0);
                    case 3 -> pos.west();
                    case 4 -> pos.east();
                    case 5 -> pos.offset(-1, -1, 0);
                    case 6 -> pos.below();
                    default -> pos.offset(1, -1, 0);
                };
            case WEST ->
                switch (bit) {
                    case 0 -> pos.offset(0, 1, -1);
                    case 1 -> pos.above();
                    case 2 -> pos.offset(0, 1, 1);
                    case 3 -> pos.north();
                    case 4 -> pos.south();
                    case 5 -> pos.offset(0, -1, -1);
                    case 6 -> pos.below();
                    default -> pos.offset(0, -1, 1);
                };
            case EAST ->
                switch (bit) {
                    case 0 -> pos.offset(0, 1, 1);
                    case 1 -> pos.above();
                    case 2 -> pos.offset(0, 1, -1);
                    case 3 -> pos.south();
                    case 4 -> pos.north();
                    case 5 -> pos.offset(0, -1, 1);
                    case 6 -> pos.below();
                    default -> pos.offset(0, -1, -1);
                };
        };
    }
}
