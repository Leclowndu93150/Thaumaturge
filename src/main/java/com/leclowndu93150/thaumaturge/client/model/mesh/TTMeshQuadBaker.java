package com.leclowndu93150.thaumaturge.client.model.mesh;

import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class TTMeshQuadBaker {
    private static final float CONTRACT_EPS = 1.0F / 256.0F;
    private static final float CONTRACT_DIVISOR = 256.0F;

    private TTMeshQuadBaker() {}

    public static void bakePart(TTMeshPart part, Material.Baked baked, int tintIndex, Matrix4f transform, BakedQuad.MaterialInfo info, List<BakedQuad> output) {
        bakePart(part, baked, tintIndex, transform, info, false, output);
    }

    public static void bakePart(TTMeshPart part, Material.Baked baked, int tintIndex, Matrix4f transform, BakedQuad.MaterialInfo info, boolean flipV, List<BakedQuad> output) {
        Matrix3f normalTransform = transform.normal(new Matrix3f());
        for (int quad = 0; quad < part.quadCount(); quad++) {
            output.add(bakeQuad(part, quad, baked, transform, normalTransform, info, flipV));
        }
    }

    private static BakedQuad bakeQuad(TTMeshPart part, int quad, Material.Baked baked, Matrix4f transform, Matrix3f normalTransform, BakedQuad.MaterialInfo info, boolean flipV) {
        Vector3fc[] positions = new Vector3fc[4];
        float[] us = new float[4];
        float[] vs = new float[4];
        int[] normals = new int[4];
        for (int i = 0; i < 4; i++) {
            int vertex = quad * 4 + i;
            Vector3f pos = new Vector3f(part.positions()[vertex * 3], part.positions()[vertex * 3 + 1], part.positions()[vertex * 3 + 2]);
            Vector3f normal = new Vector3f(part.normals()[vertex * 3], part.normals()[vertex * 3 + 1], part.normals()[vertex * 3 + 2]);
            normals[i] = BakedNormals.pack(normal.mul(normalTransform).normalize());
            float u = 0.0F;
            float v = 0.0F;
            if (part.uvs() != null) {
                u = part.uvs()[vertex * 2];
                v = part.uvs()[vertex * 2 + 1];
            }
            pos.mulPosition(transform);
            positions[i] = pos;
            us[i] = baked.sprite().getU(u);
            vs[i] = baked.sprite().getV(flipV ? 1.0F - v : v);
        }
        contractUvs(us, vs, baked.sprite());
        long[] packedUvs = new long[4];
        for (int i = 0; i < 4; i++) {
            packedUvs[i] = UVPair.pack(us[i], vs[i]);
        }
        Direction facing = computeFaceDirection(positions);
        BakedNormals bakedNormals = BakedNormals.of(normals[0], normals[1], normals[2], normals[3]);
        return new BakedQuad(positions[0], positions[1], positions[2], positions[3], packedUvs[0], packedUvs[1], packedUvs[2], packedUvs[3], facing, info, bakedNormals, BakedColors.DEFAULT);
    }

    private static void contractUvs(float[] us, float[] vs, TextureAtlasSprite sprite) {
        float texelsU = sprite.contents().width() / (sprite.getU1() - sprite.getU0());
        float texelsV = sprite.contents().height() / (sprite.getV1() - sprite.getV0());
        float texels = Math.max(texelsU, texelsV);
        float minShift = 1.0F / (texels * CONTRACT_DIVISOR);
        float centerU = (us[0] + us[1] + us[2] + us[3]) / 4.0F;
        float centerV = (vs[0] + vs[1] + vs[2] + vs[3]) / 4.0F;
        for (int i = 0; i < 4; i++) {
            us[i] = contract(us[i], centerU, minShift);
            vs[i] = contract(vs[i], centerV, minShift);
        }
    }

    private static float contract(float value, float center, float minShift) {
        float contracted = value * (1.0F - CONTRACT_EPS) + center * CONTRACT_EPS;
        if (Math.abs(value - contracted) >= minShift) {
            return contracted;
        }
        float toCenter = center - value;
        if (Math.abs(toCenter) < minShift) {
            return (value + center) / 2.0F;
        }
        return value + (toCenter < 0.0F ? -minShift : minShift);
    }

    public static Direction computeFaceDirection(Vector3fc[] positions) {
        Vector3f edge1 = new Vector3f(positions[1]).sub(positions[0]);
        Vector3f edge2 = new Vector3f(positions[2]).sub(positions[0]);
        Vector3f normal = new Vector3f();
        edge1.cross(edge2, normal);
        if (normal.lengthSquared() < 1.0E-6F) {
            return Direction.UP;
        }
        normal.normalize();
        return Direction.getApproximateNearest(normal.x, normal.y, normal.z);
    }
}
