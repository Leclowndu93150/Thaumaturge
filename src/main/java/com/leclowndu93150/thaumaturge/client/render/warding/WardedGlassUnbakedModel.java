package com.leclowndu93150.thaumaturge.client.render.warding;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public final class WardedGlassUnbakedModel implements IUnbakedGeometry<WardedGlassUnbakedModel> {
    private static final String PARTICLE_SLOT = "particle";

    private WardedGlassUnbakedModel() {}

    @Override
    public BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState,
            ItemOverrides overrides) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[WardedGlassBakedModel.TEXTURE_COUNT];
        for (int i = 0; i < sprites.length; i++) {
            sprites[i] = spriteGetter.apply(context.getMaterial(textureSlot(i)));
        }
        return new WardedGlassBakedModel(sprites, spriteGetter.apply(context.getMaterial(PARTICLE_SLOT)), modelState);
    }

    public static String textureSlot(int index) {
        return "ctm_" + (index + 1);
    }

    public static final class Loader implements IGeometryLoader<WardedGlassUnbakedModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public WardedGlassUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context)
                throws JsonParseException {
            return new WardedGlassUnbakedModel();
        }
    }
}
