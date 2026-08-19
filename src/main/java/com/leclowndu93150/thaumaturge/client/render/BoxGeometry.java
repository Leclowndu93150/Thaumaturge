package com.leclowndu93150.thaumaturge.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public final class BoxGeometry {

    private BoxGeometry() {}

    public static void box(PoseStack.Pose pose, VertexConsumer buffer, float x0, float y0, float z0, float x1, float y1, float z1, int u, int v, int dx, int dy, int dz, int texW, int texH, int tint, int light, boolean mirror) {
        float xa = mirror ? x1 : x0;
        float xb = mirror ? x0 : x1;
        float[] v1 = {xa, y1, z0};
        float[] v2 = {xb, y1, z0};
        float[] v3 = {xb, y0, z0};
        float[] v4 = {xa, y0, z0};
        float[] v5 = {xa, y1, z1};
        float[] v6 = {xb, y1, z1};
        float[] v7 = {xb, y0, z1};
        float[] v8 = {xa, y0, z1};
        quad(pose, buffer, tint, light, texW, texH, mirror, v6, v2, v3, v7, u + dz + dx, v + dz, u + dz + dx + dz, v + dz + dy);
        quad(pose, buffer, tint, light, texW, texH, mirror, v1, v5, v8, v4, u, v + dz, u + dz, v + dz + dy);
        quad(pose, buffer, tint, light, texW, texH, mirror, v6, v5, v1, v2, u + dz, v, u + dz + dx, v + dz);
        quad(pose, buffer, tint, light, texW, texH, mirror, v3, v4, v8, v7, u + dz + dx, v + dz, u + dz + dx + dx, v);
        quad(pose, buffer, tint, light, texW, texH, mirror, v2, v1, v4, v3, u + dz, v + dz, u + dz + dx, v + dz + dy);
        quad(pose, buffer, tint, light, texW, texH, mirror, v5, v6, v7, v8, u + dz + dx + dz, v + dz, u + dz + dx + dz + dx, v + dz + dy);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int tint, int light, int texW, int texH, boolean mirror, float[] a, float[] b, float[] c, float[] e, float uA, float vA, float uB, float vB) {
        float[][] verts = {a, b, c, e};
        float[][] uvs = {{uB / texW, vA / texH}, {uA / texW, vA / texH}, {uA / texW, vB / texH}, {uB / texW, vB / texH}};
        if (mirror) {
            float[][] rv = {verts[3], verts[2], verts[1], verts[0]};
            float[][] ru = {uvs[3], uvs[2], uvs[1], uvs[0]};
            verts = rv;
            uvs = ru;
        }
        float d1x = verts[1][0] - verts[0][0];
        float d1y = verts[1][1] - verts[0][1];
        float d1z = verts[1][2] - verts[0][2];
        float d2x = verts[1][0] - verts[2][0];
        float d2y = verts[1][1] - verts[2][1];
        float d2z = verts[1][2] - verts[2][2];
        float nx = d2y * d1z - d2z * d1y;
        float ny = d2z * d1x - d2x * d1z;
        float nz = d2x * d1y - d2y * d1x;
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1.0E-6F) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        for (int i = 0; i < 4; i++) {
            buffer.addVertex(pose, verts[i][0], verts[i][1], verts[i][2]).setColor(tint).setUv(uvs[i][0], uvs[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        }
    }
}
