package com.leclowndu93150.thaumaturge.client.model.mesh;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.NamedRenderTypeManager;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.joml.Matrix4f;

public final class TTMeshGeometry implements IUnbakedGeometry<TTMeshGeometry> {
    private static final float CENTER_OFFSET = 0.5F;
    private static final int NO_TINT = -1;
    private static final String PARTICLE_SLOT = "particle";
    private static final ResourceLocation DEFAULT_RENDER_TYPE = ResourceLocation.withDefaultNamespace("cutout");

    private final ResourceLocation model;
    private final boolean flipV;
    private final boolean cornerSpace;
    private final ResourceLocation renderType;
    private final Set<String> translucentSlots;

    public TTMeshGeometry(
            ResourceLocation model,
            boolean flipV,
            boolean cornerSpace,
            ResourceLocation renderType,
            Set<String> translucentSlots) {
        this.model = model;
        this.flipV = flipV;
        this.cornerSpace = cornerSpace;
        this.renderType = renderType;
        this.translucentSlots = Set.copyOf(translucentSlots);
    }

    @Override
    public BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState,
            ItemOverrides overrides) {
        Transformation rootTransform = context.getRootTransform();
        if (!rootTransform.isIdentity()) {
            modelState = UnbakedGeometryHelper.composeRootTransformIntoModelState(modelState, rootTransform);
        }
        TTMesh mesh = loadMesh();
        Matrix4f transform = new Matrix4f()
                .translate(CENTER_OFFSET, CENTER_OFFSET, CENTER_OFFSET)
                .mul(modelState.getRotation().getMatrix());
        if (cornerSpace) {
            transform.translate(-CENTER_OFFSET, -CENTER_OFFSET, -CENTER_OFFSET);
        }

        RenderTypeGroup renderTypes = NamedRenderTypeManager.get(renderType);
        TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial(PARTICLE_SLOT));
        CompositeModel.Baked.Builder builder =
                CompositeModel.Baked.builder(context, particle, overrides, context.getTransforms());
        RenderTypeGroup translucent = NamedRenderTypeManager.get(ResourceLocation.withDefaultNamespace("translucent"));

        List<BakedQuad> quads = new ArrayList<>();
        for (TTMeshPart part : mesh.parts()) {
            if (!context.isComponentVisible(part.name(), true)) continue;
            String slot = part.materialSlot();
            if (!context.hasMaterial(slot)) {
                slot = PARTICLE_SLOT;
            }
            TextureAtlasSprite sprite = spriteGetter.apply(context.getMaterial(slot));
            quads.clear();
            TTMeshQuadBaker.bakePart(part, sprite, NO_TINT, transform, flipV, true, 0, 0, quads);
            builder.addQuads(translucentSlots.contains(slot) ? translucent : renderTypes, quads);
        }
        return builder.build();
    }

    private TTMesh loadMesh() {
        try {
            return TTMeshLoader.load(Minecraft.getInstance().getResourceManager(), model);
        } catch (IOException e) {
            throw new RuntimeException("Could not load mesh model at " + model, e);
        }
    }

    public static final class Loader implements IGeometryLoader<TTMeshGeometry> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public TTMeshGeometry read(JsonObject jsonObject, JsonDeserializationContext context)
                throws JsonParseException {
            ResourceLocation model = ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "model"));
            boolean flipV = GsonHelper.getAsBoolean(jsonObject, "flip_v", false);
            boolean cornerSpace = GsonHelper.getAsBoolean(jsonObject, "corner_space", false);
            ResourceLocation renderType = jsonObject.has("render_type")
                    ? ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "render_type"))
                    : DEFAULT_RENDER_TYPE;
            Set<String> translucentSlots = new HashSet<>();
            if (jsonObject.has("translucent_slots")) {
                for (var slot : GsonHelper.getAsJsonArray(jsonObject, "translucent_slots")) {
                    translucentSlots.add(GsonHelper.convertToString(slot, "translucent slot"));
                }
            }
            return new TTMeshGeometry(model, flipV, cornerSpace, renderType, translucentSlots);
        }
    }
}
