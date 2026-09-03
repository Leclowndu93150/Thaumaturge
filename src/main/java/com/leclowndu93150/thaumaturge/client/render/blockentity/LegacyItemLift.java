package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
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

    private LegacyItemLift() {}

    public static float centerLift(ItemStack stack, ItemDisplayContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, null, 0);
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
        if (bounds[0] > bounds[1]) {
            return LEGACY_CENTER_Y;
        }
        return LEGACY_CENTER_Y - (bounds[0] + bounds[1]) * 0.5F;
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
}
