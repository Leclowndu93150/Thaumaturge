package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
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

public record ConnectedTilesModel(ResourceLocation texture) implements IUnbakedGeometry<ConnectedTilesModel> {
    public static final ResourceLocation TYPE = TTIds.rl("connected_tiles");
    public static final MapCodec<ConnectedTilesModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(ConnectedTilesModel::texture))
            .apply(instance, ConnectedTilesModel::new));

    @Override
    public BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker models,
            Function<Material, TextureAtlasSprite> sprites,
            ModelState modelState,
            ItemOverrides overrides) {
        ConnectedBakeContext baker = new ConnectedBakeContext(models, sprites);
        List<TextureAtlasSprite> tiles = new ArrayList<>(TileFaceQuads.TILE_COUNT);
        for (int tile = 1; tile <= TileFaceQuads.TILE_COUNT; tile++) {
            tiles.add(ConnectedQuadBaker.material(baker, texture.withSuffix("_" + tile)));
        }
        return new ConnectedBlockStateModel(
                TileFaceQuads.bake(baker, tiles),
                ConnectedQuads.EMPTY,
                ConnectedQuadBaker.material(baker, texture),
                true,
                null,
                context.getTransforms(),
                RenderType.translucent());
    }
}
