package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
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
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class LegacyItemLift {
    public static final float LEGACY_CENTER_Y = 0.35F;
    private static final long QUAD_SEED = 42L;
    private static final int MAX_BOTTOM_LIFT_CACHE_ENTRIES = 512;
    private static final float MAX_REASONABLE_RENDER_BOUND = 64.0F;
    private static final Long2FloatOpenHashMap BOTTOM_LIFT_CACHE = new Long2FloatOpenHashMap();

    static {
        BOTTOM_LIFT_CACHE.defaultReturnValue(Float.NaN);
    }

    private LegacyItemLift() {}

    public static float centerLift(ItemStack stack, ItemDisplayContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
        float[] bounds = bakedYBounds(model, context);
        if (!usableBounds(bounds)) {
            return LEGACY_CENTER_Y;
        }
        return LEGACY_CENTER_Y - (bounds[0] + bounds[1]) * 0.5F;
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

        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
        long cacheKey = cacheKey(stack, context, model);
        float cached = BOTTOM_LIFT_CACHE.get(cacheKey);
        if (!Float.isNaN(cached)) {
            return cached;
        }

        float lift = renderedBottomLift(minecraft, stack, context);
        if (!Float.isFinite(lift)) {
            float[] bounds = bakedYBounds(model, context);
            lift = usableBounds(bounds) ? -bounds[0] : 0.0F;
        }

        cacheBottomLift(cacheKey, lift);
        return lift;
    }

    /** Clears cached measurements after models are rebuilt during a resource reload. */
    public static void clearBottomLiftCache() {
        BOTTOM_LIFT_CACHE.clear();
    }

    private static float renderedBottomLift(Minecraft minecraft, ItemStack stack, ItemDisplayContext context) {
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
            return Float.NaN;
        }

        return bounds.usable() ? -bounds.minY : Float.NaN;
    }

    private static float[] bakedYBounds(BakedModel model, ItemDisplayContext context) {
        RandomSource random = RandomSource.create(QUAD_SEED);
        PoseStack poseStack = new PoseStack();
        model.getTransforms().getTransform(context).apply(false, poseStack);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        Matrix4f matrix = poseStack.last().pose();

        float[] bounds = {Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        accumulateY(model.getQuads(null, null, random), matrix, bounds);
        for (Direction direction : Direction.values()) {
            accumulateY(model.getQuads(null, direction, random), matrix, bounds);
        }
        return bounds;
    }

    private static boolean usableBounds(float[] bounds) {
        return Float.isFinite(bounds[0])
                && Float.isFinite(bounds[1])
                && bounds[0] <= bounds[1]
                && Math.abs(bounds[0]) <= MAX_REASONABLE_RENDER_BOUND
                && Math.abs(bounds[1]) <= MAX_REASONABLE_RENDER_BOUND;
    }

    private static long cacheKey(ItemStack stack, ItemDisplayContext context, BakedModel model) {
        int stackState = 31 * ItemStack.hashItemAndComponents(stack) + context.ordinal();
        return ((long) System.identityHashCode(model) << 32) ^ Integer.toUnsignedLong(stackState);
    }

    private static void cacheBottomLift(long key, float lift) {
        if (BOTTOM_LIFT_CACHE.size() >= MAX_BOTTOM_LIFT_CACHE_ENTRIES) {
            var iterator = BOTTOM_LIFT_CACHE.keySet().iterator();
            if (iterator.hasNext()) {
                iterator.nextLong();
                iterator.remove();
            }
        }
        BOTTOM_LIFT_CACHE.put(key, lift);
    }

    private static void accumulateY(List<BakedQuad> quads, Matrix4f matrix, float[] bounds) {
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
                bounds[0] = Math.min(bounds[0], corner.y);
                bounds[1] = Math.max(bounds[1], corner.y);
            }
        }
    }

    private static final class RenderBounds {
        private float minY = Float.POSITIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private boolean invalid;

        private void include(float y) {
            if (!Float.isFinite(y)) {
                invalid = true;
                return;
            }
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        private boolean usable() {
            return !invalid
                    && Float.isFinite(minY)
                    && Float.isFinite(maxY)
                    && minY <= maxY
                    && Math.abs(minY) <= MAX_REASONABLE_RENDER_BOUND
                    && Math.abs(maxY) <= MAX_REASONABLE_RENDER_BOUND;
        }
    }

    private static final class BoundsVertexConsumer implements VertexConsumer {
        private final RenderBounds bounds;

        private BoundsVertexConsumer(RenderBounds bounds) {
            this.bounds = bounds;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            bounds.include(y);
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
