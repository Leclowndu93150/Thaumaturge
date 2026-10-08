package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public record ConnectedCornersModel(ResourceLocation sprites, ResourceLocation particle)
        implements IUnbakedGeometry<ConnectedCornersModel> {
    public static final ResourceLocation TYPE = TTIds.rl("connected_corners");
    public static final MapCodec<ConnectedCornersModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("sprites").forGetter(ConnectedCornersModel::sprites),
                    ResourceLocation.CODEC.fieldOf("particle").forGetter(ConnectedCornersModel::particle))
            .apply(instance, ConnectedCornersModel::new));

    @Override
    public BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker models,
            Function<Material, TextureAtlasSprite> sprites,
            ModelState modelState,
            ItemOverrides overrides) {
        ConnectedBakeContext baker = new ConnectedBakeContext(models, sprites);
        return new ConnectedBlockStateModel(
                CornerFaceQuads.bake(baker, sprites()),
                ConnectedQuads.EMPTY,
                ConnectedQuadBaker.material(baker, particle),
                true,
                null,
                context.getTransforms(),
                RenderType.translucent());
    }

    @Override
    public void resolveParents(
            Function<ResourceLocation, net.minecraft.client.resources.model.UnbakedModel> models,
            IGeometryBakingContext context) {}
}
