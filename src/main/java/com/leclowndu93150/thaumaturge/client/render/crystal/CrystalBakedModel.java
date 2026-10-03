package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.leclowndu93150.thaumaturge.client.model.mesh.TCMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class CrystalBakedModel implements IDynamicBakedModel {
    public static final ModelProperty<Integer> FACE_MASK = new ModelProperty<>();

    private static final List<Integer> PART_INDICES = List.of(0, 1, 2, 3, 4, 5, 6, 7);
    private static final Direction[] FACES = Direction.values();
    private static final int PART_COUNT = 8;

    private final TCMesh mesh;
    private final TextureAtlasSprite particle;
    private final ChunkRenderTypeSet renderTypes;

    public CrystalBakedModel(TCMesh mesh, TextureAtlasSprite particle) {
        this.mesh = mesh;
        this.particle = particle;
        this.renderTypes = ChunkRenderTypeSet.of(RenderType.cutout());
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        int mask = 0;
        for (Direction face : FACES) {
            BlockPos neighbour = pos.relative(face);
            if (level.getBlockState(neighbour).isFaceSturdy(level, neighbour, face.getOpposite())) {
                mask |= 1 << face.ordinal();
            }
        }
        return modelData.derive().with(FACE_MASK, mask).build();
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random,
            ModelData data,
            @Nullable RenderType renderType) {
        if (side != null || state == null) {
            return Collections.emptyList();
        }
        Integer stored = data.get(FACE_MASK);
        int faceMask = stored == null ? 0 : stored;
        int growth = state.hasProperty(BlockCrystal.SIZE) ? state.getValue(BlockCrystal.SIZE) : 0;
        int partsPerFace = growth + 1;
        long seed = random.nextLong();
        List<BakedQuad> quads = new ArrayList<>();
        boolean any = false;
        for (Direction face : FACES) {
            if ((faceMask & (1 << face.ordinal())) == 0) {
                continue;
            }
            Matrix4f transform = CrystalFaceTransforms.forFace(face);
            List<Integer> shuffled = new ArrayList<>(PART_INDICES);
            Collections.shuffle(shuffled, new Random(seed + CrystalFaceTransforms.seedOffset(face)));
            for (int i = 0; i < partsPerFace; i++) {
                TCMeshPart part = mesh.parts().get(shuffled.get(i));
                CrystalQuadBaker.bakePart(part, particle, 0, transform, quads);
                any = true;
            }
        }
        if (!any) {
            int unsupportedSeed = (int) (seed & 0x7);
            Matrix4f transform = CrystalFaceTransforms.forFace(Direction.DOWN);
            TCMeshPart part = mesh.parts().get(unsupportedSeed % PART_COUNT);
            CrystalQuadBaker.bakePart(part, particle, 0, transform, quads);
        }
        return quads;
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
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    @Deprecated
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData modelData) {
        return particle;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
