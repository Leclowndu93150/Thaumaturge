package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LegacyItemLift {
    public static final float LEGACY_CENTER_Y = 0.35F;
    private static final long QUAD_SEED = 42L;
    private static final int MAX_MODEL_BOUNDS_CACHE_ENTRIES = 512;
    private static final float MAX_REASONABLE_RENDER_BOUND = 64.0F;
    private static final Long2ObjectOpenHashMap<ModelBounds> MODEL_BOUNDS_CACHE = new Long2ObjectOpenHashMap<>();

    public record ModelBounds(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        public float centerX() {
            return (minX + maxX) * 0.5F;
        }

        public float centerY() {
            return (minY + maxY) * 0.5F;
        }

        public float centerZ() {
            return (minZ + maxZ) * 0.5F;
        }

        public float sizeY() {
            return maxY - minY;
        }

        public float sizeZ() {
            return maxZ - minZ;
        }
    }

    private LegacyItemLift() {}

    public static float centerLift(ItemStack stack, ItemDisplayContext context) {
        ModelBounds bounds = modelBounds(stack, context);
        return bounds == null ? LEGACY_CENTER_Y : LEGACY_CENTER_Y - bounds.centerY();
    }

    public static ModelBounds modelBounds(ItemStack stack, ItemDisplayContext context) {
        if (stack.isEmpty()) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
        long key = cacheKey(stack, context, model);
        ModelBounds cached = MODEL_BOUNDS_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        RenderBounds rendered = renderedModelBounds(minecraft, stack, context);
        ModelBounds bounds = rendered.usable() ? rendered.modelBounds() : bakedBounds(model, context);
        if (bounds == null) {
            return null;
        }

        if (MODEL_BOUNDS_CACHE.size() >= MAX_MODEL_BOUNDS_CACHE_ENTRIES) {
            var iterator = MODEL_BOUNDS_CACHE.keySet().iterator();
            if (iterator.hasNext()) {
                iterator.nextLong();
                iterator.remove();
            }
        }
        MODEL_BOUNDS_CACHE.put(key, bounds);
        return bounds;
    }

    /**
     * Returns the lift needed to put the lowest rendered vertex at Y=0.
     *
     * <p>The first lookup renders the item into a bounds-only vertex consumer. This follows Minecraft's actual item
     * rendering path, so special/BEWLR renderers and third-party custom item renderers can be measured too. The result
     * is cached by resolved model identity, item components, and display context, so subsequent frames are a cheap
     * cache lookup.
     *
     * <p>If a custom renderer fails or emits unusable bounds, this falls back to the baked-model quad calculation. If
     * neither path can provide valid geometry, zero lift is used as a safe final fallback.
     */
    public static float bottomLift(ItemStack stack, ItemDisplayContext context) {
        if (stack.isEmpty()) {
            return 0.0F;
        }

        ModelBounds bounds = modelBounds(stack, context);
        return bounds == null ? 0.0F : -bounds.minY();
    }

    /** Clears cached measurements after models are rebuilt during a resource reload. */
    public static void clearBottomLiftCache() {
        MODEL_BOUNDS_CACHE.clear();
    }

    private static RenderBounds renderedModelBounds(Minecraft minecraft, ItemStack stack, ItemDisplayContext context) {
        RenderBounds bounds = new RenderBounds();
        Map<RenderType, VertexConsumer> consumers = new IdentityHashMap<>();
        MultiBufferSource measuringBuffers =
                renderType -> consumers.computeIfAbsent(renderType, ignored -> new BoundsVertexConsumer(bounds));
        PoseStack poseStack = new PoseStack();

        try {
            minecraft
                    .getItemRenderer()
                    .renderStatic(
                            stack,
                            context,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            measuringBuffers,
                            minecraft.level,
                            0);
        } catch (RuntimeException | LinkageError ignored) {
            return bounds;
        }
        return bounds;
    }

    private static ModelBounds bakedBounds(BakedModel model, ItemDisplayContext context) {
        RandomSource random = RandomSource.create(QUAD_SEED);
        PoseStack poseStack = new PoseStack();
        model = model.applyTransform(context, poseStack, false);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        Matrix4f matrix = poseStack.last().pose();

        RenderBounds bounds = new RenderBounds();
        accumulate(model.getQuads(null, null, random, ModelData.EMPTY, null), matrix, bounds);
        for (Direction direction : Direction.values()) {
            accumulate(model.getQuads(null, direction, random, ModelData.EMPTY, null), matrix, bounds);
        }
        return bounds.usable() ? bounds.modelBounds() : null;
    }

    private static long cacheKey(ItemStack stack, ItemDisplayContext context, BakedModel model) {
        int stackState = 31 * ItemStack.hashItemAndComponents(stack) + context.ordinal();
        return ((long) System.identityHashCode(model) << 32) ^ Integer.toUnsignedLong(stackState);
    }

    private static void accumulate(List<BakedQuad> quads, Matrix4f matrix, RenderBounds bounds) {
        Vector3f corner = new Vector3f();
        for (BakedQuad quad : quads) {
            int[] vertices = quad.getVertices();
            int stride = vertices.length / 4;
            for (int vertex = 0; vertex < 4; vertex++) {
                int base = vertex * stride;
                corner.set(
                        Float.intBitsToFloat(vertices[base]),
                        Float.intBitsToFloat(vertices[base + 1]),
                        Float.intBitsToFloat(vertices[base + 2]));
                matrix.transformPosition(corner);
                bounds.include(corner.x, corner.y, corner.z);
            }
        }
    }

    private static final class RenderBounds {
        private float minX = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private float minZ = Float.POSITIVE_INFINITY;
        private float maxZ = Float.NEGATIVE_INFINITY;
        private boolean invalid;

        private void include(float x, float y, float z) {
            if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z)) {
                invalid = true;
                return;
            }
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }

        private boolean usable() {
            return !invalid
                    && Float.isFinite(minX)
                    && Float.isFinite(maxX)
                    && Float.isFinite(minY)
                    && Float.isFinite(maxY)
                    && Float.isFinite(minZ)
                    && Float.isFinite(maxZ)
                    && minX <= maxX
                    && minY <= maxY
                    && minZ <= maxZ
                    && Math.abs(minX) <= MAX_REASONABLE_RENDER_BOUND
                    && Math.abs(maxX) <= MAX_REASONABLE_RENDER_BOUND
                    && Math.abs(minY) <= MAX_REASONABLE_RENDER_BOUND
                    && Math.abs(maxY) <= MAX_REASONABLE_RENDER_BOUND
                    && Math.abs(minZ) <= MAX_REASONABLE_RENDER_BOUND
                    && Math.abs(maxZ) <= MAX_REASONABLE_RENDER_BOUND;
        }

        private ModelBounds modelBounds() {
            return new ModelBounds(minX, maxX, minY, maxY, minZ, maxZ);
        }
    }

    private static final class BoundsVertexConsumer implements VertexConsumer {
        private final RenderBounds bounds;

        private BoundsVertexConsumer(RenderBounds bounds) {
            this.bounds = bounds;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            bounds.include(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }
    }
}
