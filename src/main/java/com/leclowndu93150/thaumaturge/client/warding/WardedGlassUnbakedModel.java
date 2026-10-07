package com.leclowndu93150.thaumaturge.client.warding;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class WardedGlassUnbakedModel implements CustomUnbakedBlockStateModel {
    public static final WardedGlassUnbakedModel INSTANCE = new WardedGlassUnbakedModel();
    public static final MapCodec<WardedGlassUnbakedModel> CODEC = MapCodec.unit(INSTANCE);
    public static final Identifier MODEL_TYPE = TTIds.rl("warded_glass");

    private static final String TEXTURE_PATH = "block/warded_glass";
    private static final Vector3fc FROM = new Vector3f(0.0F, 0.0F, 0.0F);
    private static final Vector3fc TO = new Vector3f(16.0F, 16.0F, 16.0F);
    private static final Direction[] FACES = Direction.values();

    private WardedGlassUnbakedModel() {}

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        Material.Baked particle = bakeMaterial(baker, TEXTURE_PATH);
        BakedQuad[][] quads = new BakedQuad[FACES.length][WardedGlassBakedModel.TILE_COUNT];
        int flags = 0;
        for (int tile = 0; tile < WardedGlassBakedModel.TILE_COUNT; tile++) {
            Material.Baked sprite = bakeMaterial(baker, TEXTURE_PATH + "_" + (tile + 1));
            for (Direction face : FACES) {
                CuboidFace cuboidFace = new CuboidFace(face, CuboidFace.NO_TINT, TEXTURE_PATH, null, Quadrant.R0);
                BakedQuad quad = FaceBakery.bakeQuad(baker, FROM, TO, cuboidFace, sprite, face, BlockModelRotation.IDENTITY, null, true, 0);
                quads[face.ordinal()][tile] = quad;
                flags |= quad.materialInfo().flags();
            }
        }
        return new WardedGlassBakedModel(quads, particle, flags);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {}

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    private static Material.Baked bakeMaterial(ModelBaker baker, String path) {
        Identifier texture = TTIds.rl(path);
        return baker.materials().get(new Material(texture), texture::toString);
    }
}
