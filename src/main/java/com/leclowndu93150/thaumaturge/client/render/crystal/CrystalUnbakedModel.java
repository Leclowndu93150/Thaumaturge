package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshLoader;
import java.io.IOException;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public final class CrystalUnbakedModel implements IUnbakedGeometry<CrystalUnbakedModel> {
    public static final ResourceLocation MODEL_LOCATION = TTIds.rl("models/mesh/crystal.ttmesh");
    private static final String PARTICLE_SLOT = "particle";

    private CrystalUnbakedModel() {}

    @Override
    public BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState,
            ItemOverrides overrides) {
        TTMesh mesh = loadMesh();
        TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial(PARTICLE_SLOT));
        return new CrystalBakedModel(mesh, particle);
    }

    private static TTMesh loadMesh() {
        try {
            return TTMeshLoader.load(Minecraft.getInstance().getResourceManager(), MODEL_LOCATION);
        } catch (IOException e) {
            throw new RuntimeException("Could not load crystal OBJ at " + MODEL_LOCATION, e);
        }
    }

    public static final class Loader implements IGeometryLoader<CrystalUnbakedModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public CrystalUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context)
                throws JsonParseException {
            return new CrystalUnbakedModel();
        }
    }
}
