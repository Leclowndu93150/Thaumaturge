package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class HierophantSprites {
    private static final int FULL_LIGHT = 0xF000F0;
    private static final float TILE = 0.5F;

    private HierophantSprites() {}

    public static void floor(
            VertexConsumer buffer, Matrix4f matrix, float radius, float y, int tile, float alpha, boolean mirror) {
        quad(
                buffer,
                matrix,
                new Vector3f(-radius, y, -radius),
                new Vector3f(radius, y, -radius),
                new Vector3f(radius, y, radius),
                new Vector3f(-radius, y, radius),
                tile,
                alpha,
                mirror);
    }

    public static void flame(VertexConsumer buffer, Matrix4f matrix, float radius, float height, float alpha) {
        quad(
                buffer,
                matrix,
                new Vector3f(-radius, 0, 0),
                new Vector3f(radius, 0, 0),
                new Vector3f(radius, height, 0),
                new Vector3f(-radius, height, 0),
                3,
                alpha,
                false);
        quad(
                buffer,
                matrix,
                new Vector3f(0, 0, -radius),
                new Vector3f(0, 0, radius),
                new Vector3f(0, height, radius),
                new Vector3f(0, height, -radius),
                3,
                alpha,
                false);
    }

    public static void quad(
            VertexConsumer buffer,
            Matrix4f matrix,
            Vector3f a,
            Vector3f b,
            Vector3f c,
            Vector3f d,
            int tile,
            float alpha,
            boolean mirror) {
        final float u = (tile % 2) * TILE;
        final float v = (tile / 2) * TILE;
        final float left = mirror ? u + TILE : u;
        final float right = mirror ? u : u + TILE;
        final int color = ((int) (Mth.clamp(alpha, 0, 1) * 255) << 24) | 0xFFFFFF;
        vertex(buffer, matrix, a, left, v + TILE, color);
        vertex(buffer, matrix, b, right, v + TILE, color);
        vertex(buffer, matrix, c, right, v, color);
        vertex(buffer, matrix, d, left, v, color);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Vector3f point, float u, float v, int color) {
        buffer.addVertex(matrix, point.x, point.y, point.z)
                .setUv(u, v)
                .setColor(color)
                .setLight(FULL_LIGHT);
    }
}
