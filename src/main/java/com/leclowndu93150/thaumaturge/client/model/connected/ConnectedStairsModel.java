package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public record ConnectedStairsModel(
        FrameKit side, FrameKit top, FrameKit bottom, ResourceLocation particle, Optional<TagKey<Block>> group)
        implements IUnbakedGeometry<ConnectedStairsModel> {
    public static final ResourceLocation TYPE = TTIds.rl("connected_stairs");
    public static final MapCodec<ConnectedStairsModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    FrameKit.CODEC.fieldOf("side").forGetter(ConnectedStairsModel::side),
                    FrameKit.CODEC.fieldOf("top").forGetter(ConnectedStairsModel::top),
                    FrameKit.CODEC.fieldOf("bottom").forGetter(ConnectedStairsModel::bottom),
                    ResourceLocation.CODEC.fieldOf("particle").forGetter(ConnectedStairsModel::particle),
                    TagKey.hashedCodec(Registries.BLOCK)
                            .optionalFieldOf("connects_with")
                            .forGetter(ConnectedStairsModel::group))
            .apply(instance, ConnectedStairsModel::new));

    private static final Direction[] FACES = Direction.values();

    @Override
    public BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker models,
            Function<Material, TextureAtlasSprite> sprites,
            ModelState modelState,
            ItemOverrides overrides) {
        ConnectedBakeContext baker = new ConnectedBakeContext(models, sprites);
        StairCellQuads[] quads = new StairCellQuads[FACES.length];
        for (Direction face : FACES) {
            quads[face.ordinal()] = StairCellQuads.bake(
                    baker, face == Direction.UP ? top : face == Direction.DOWN ? bottom : side, face);
        }
        return new ConnectedStairsBlockStateModel(
                quads,
                ConnectedQuadBaker.material(baker, particle),
                group.orElse(null),
                context.getTransforms(),
                RenderType.solid());
    }
}
