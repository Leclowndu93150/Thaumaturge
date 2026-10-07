package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshLoader;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

public final class CrystalUnbakedModel implements CustomUnbakedBlockStateModel {
    public static final CrystalUnbakedModel INSTANCE = new CrystalUnbakedModel();
    public static final MapCodec<CrystalUnbakedModel> CODEC = MapCodec.unit(INSTANCE);
    public static final Identifier MODEL_LOCATION = Identifier.fromNamespaceAndPath(TTIds.MODID, "models/mesh/crystal.ttmesh");
    public static final Identifier SPRITE_LOCATION = Identifier.fromNamespaceAndPath(TTIds.MODID, "block/crystal");
    public static final Identifier DEBUG_NAME = Identifier.fromNamespaceAndPath(TTIds.MODID, "crystal");

    private CrystalUnbakedModel() {}

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        TTMesh mesh = loadMesh();
        Material material = new Material(SPRITE_LOCATION);
        Material.Baked baked = baker.materials().get(material, () -> DEBUG_NAME.toString());
        return new CrystalBakedModel(mesh, baked);
    }

    @Override
    public void resolveDependencies(Resolver resolver) {}

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    private static TTMesh loadMesh() {
        try {
            return TTMeshLoader.load(Minecraft.getInstance().getResourceManager(), MODEL_LOCATION);
        } catch (IOException e) {
            throw new RuntimeException("Could not load crystal mesh at " + MODEL_LOCATION, e);
        }
    }
}
