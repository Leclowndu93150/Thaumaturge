package com.leclowndu93150.thaumaturge.client.model.connected;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public record ConnectedSheetModel(
        Optional<ResourceLocation> model, Map<Direction, ConnectedTexture> faces, Optional<TagKey<Block>> group)
        implements IUnbakedGeometry<ConnectedSheetModel> {
    public static final ResourceLocation TYPE = TTIds.rl("connected");
    public static final MapCodec<ConnectedSheetModel> CODEC = RecordCodecBuilder.<ConnectedSheetModel>mapCodec(
                    instance -> instance.group(
                                    ResourceLocation.CODEC
                                            .optionalFieldOf("model")
                                            .forGetter(ConnectedSheetModel::model),
                                    Codec.unboundedMap(Direction.CODEC, ConnectedTexture.CODEC)
                                            .fieldOf("faces")
                                            .forGetter(ConnectedSheetModel::faces),
                                    TagKey.hashedCodec(Registries.BLOCK)
                                            .optionalFieldOf("connects_with")
                                            .forGetter(ConnectedSheetModel::group))
                            .apply(instance, ConnectedSheetModel::new))
            .validate(ConnectedSheetModel::validate);

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final float BLOCK_PIXELS = 16.0F;

    public ConnectedSheetModel {
        Map<Direction, ConnectedTexture> ordered = new EnumMap<>(Direction.class);
        ordered.putAll(faces);
        faces = Collections.unmodifiableMap(ordered);
    }

    public static ConnectedSheetModel cube(ConnectedTexture texture, Optional<TagKey<Block>> group) {
        Map<Direction, ConnectedTexture> faces = new EnumMap<>(Direction.class);
        for (Direction face : DIRECTIONS) {
            faces.put(face, texture);
        }
        return new ConnectedSheetModel(Optional.empty(), faces, group);
    }

    private static DataResult<ConnectedSheetModel> validate(ConnectedSheetModel model) {
        if (model.model.isEmpty() && model.faces.size() < DIRECTIONS.length) {
            return DataResult.error(() -> "Connected model without a base model needs a texture for every face");
        }
        return DataResult.success(model);
    }

    @Override
    public BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker models,
            Function<Material, TextureAtlasSprite> sprites,
            ModelState modelState,
            ItemOverrides overrides) {
        ConnectedBakeContext baker = new ConnectedBakeContext(models, sprites);
        float[] insets = new float[DIRECTIONS.length];
        if (model.isEmpty()) {
            TextureAtlasSprite particle =
                    ConnectedQuadBaker.material(baker, faces.get(Direction.UP).texture());
            return new ConnectedBlockStateModel(
                    SheetFaceQuads.bake(baker, faces, insets),
                    ConnectedQuads.EMPTY,
                    particle,
                    true,
                    group.orElse(null),
                    context.getTransforms(),
                    RenderType.solid());
        }
        BakedModel part = models.bake(model.get(), BlockModelRotation.X0_Y0);
        ConnectedQuads.Builder fixed = new ConnectedQuads.Builder();
        for (Direction face : DIRECTIONS) {
            insets[face.ordinal()] = BLOCK_PIXELS;
            for (BakedQuad quad : part.getQuads(null, face, RandomSource.create(0))) {
                keepOrMeasure(quad, insets, fixed, face);
            }
        }
        for (BakedQuad quad : part.getQuads(null, null, RandomSource.create(0))) {
            keepOrMeasure(quad, insets, fixed, null);
        }
        for (Direction face : DIRECTIONS) {
            if (insets[face.ordinal()] >= BLOCK_PIXELS) {
                insets[face.ordinal()] = 0.0F;
            }
        }
        return new ConnectedBlockStateModel(
                SheetFaceQuads.bake(baker, faces, insets),
                fixed.build(),
                part.getParticleIcon(),
                part.useAmbientOcclusion(),
                group.orElse(null),
                context.getTransforms(),
                RenderType.solid());
    }

    private void keepOrMeasure(BakedQuad quad, float[] insets, ConnectedQuads.Builder fixed, @Nullable Direction cull) {
        Direction facing = quad.getDirection();
        if (faces.containsKey(facing)) {
            insets[facing.ordinal()] = Math.min(insets[facing.ordinal()], inset(position(quad), facing));
        } else if (cull != null) {
            fixed.addCulledFace(cull, quad);
        } else {
            fixed.addUnculledFace(quad);
        }
    }

    private static float inset(Vector3fc position, Direction facing) {
        float depth =
                switch (facing) {
                    case DOWN -> position.y();
                    case UP -> 1.0F - position.y();
                    case NORTH -> position.z();
                    case SOUTH -> 1.0F - position.z();
                    case WEST -> position.x();
                    case EAST -> 1.0F - position.x();
                };
        return Math.max(0.0F, depth * BLOCK_PIXELS);
    }

    private static Vector3f position(BakedQuad quad) {
        int[] data = quad.getVertices();
        return new Vector3f(
                Float.intBitsToFloat(data[0]), Float.intBitsToFloat(data[1]), Float.intBitsToFloat(data[2]));
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> models, IGeometryBakingContext context) {
        model.ifPresent(id -> models.apply(id).resolveParents(models));
    }
}
