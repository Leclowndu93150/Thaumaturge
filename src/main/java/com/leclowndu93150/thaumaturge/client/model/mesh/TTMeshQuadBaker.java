package com.leclowndu93150.thaumaturge.client.model.mesh;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class TTMeshQuadBaker {
    private static final float CONTRACT_EPS = 1.0F / 256.0F;
    private static final float CONTRACT_DIVISOR = 256.0F;
    private static final float NORMAL_EPS = 1.0E-6F;
    private static final int CORNERS_PER_QUAD = 4;

    private TTMeshQuadBaker() {}

    public static void bakePart(
            TTMeshPart part, TextureAtlasSprite sprite, int tintIndex, Matrix4f transform, List<BakedQuad> output) {
        bakePart(part, sprite, tintIndex, transform, false, false, 0, 0, output);
    }

    public static void bakePart(
            TTMeshPart part,
            TextureAtlasSprite sprite,
            int tintIndex,
            Matrix4f transform,
            boolean flipV,
            List<BakedQuad> output) {
        bakePart(part, sprite, tintIndex, transform, flipV, false, 0, 0, output);
    }

    public static void bakePart(
            TTMeshPart part,
            TextureAtlasSprite sprite,
            int tintIndex,
            Matrix4f transform,
            boolean flipV,
            boolean shade,
            int blockLight,
            int skyLight,
            List<BakedQuad> output) {
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        Matrix3f normalTransform = transform.normal(new Matrix3f());
        for (int quad = 0; quad < part.quadCount(); quad++) {
            output.add(bakeQuad(
                    part,
                    quad,
                    sprite,
                    transform,
                    normalTransform,
                    tintIndex,
                    flipV,
                    shade,
                    blockLight,
                    skyLight,
                    consumer));
        }
    }

    private static BakedQuad bakeQuad(
            TTMeshPart part,
            int quad,
            TextureAtlasSprite sprite,
            Matrix4f transform,
            Matrix3f normalTransform,
            int tintIndex,
            boolean flipV,
            boolean shade,
            int blockLight,
            int skyLight,
            QuadBakingVertexConsumer consumer) {
        Vector3f[] positions = new Vector3f[CORNERS_PER_QUAD];
        Vector3f[] normals = new Vector3f[CORNERS_PER_QUAD];
        float[] us = new float[CORNERS_PER_QUAD];
        float[] vs = new float[CORNERS_PER_QUAD];
        for (int i = 0; i < CORNERS_PER_QUAD; i++) {
            int vertex = quad * CORNERS_PER_QUAD + i;
            Vector3f pos = new Vector3f(
                    part.positions()[vertex * 3], part.positions()[vertex * 3 + 1], part.positions()[vertex * 3 + 2]);
            float u = 0.0F;
            float v = 0.0F;
            if (part.uvs() != null) {
                u = part.uvs()[vertex * 2];
                v = part.uvs()[vertex * 2 + 1];
            }
            pos.mulPosition(transform);
            positions[i] = pos;
            normals[i] = new Vector3f(
                            part.normals()[vertex * 3], part.normals()[vertex * 3 + 1], part.normals()[vertex * 3 + 2])
                    .mul(normalTransform);
            us[i] = sprite.getU(u);
            vs[i] = sprite.getV(flipV ? 1.0F - v : v);
        }
        contractUvs(us, vs, sprite);

        Vector3f normal = faceNormal(positions);
        Direction facing;
        if (normal.lengthSquared() < NORMAL_EPS) {
            facing = Direction.UP;
            normal.set(0.0F, 1.0F, 0.0F);
        } else {
            normal.normalize();
            facing = Direction.getNearest(normal.x, normal.y, normal.z);
        }

        consumer.setSprite(sprite);
        consumer.setDirection(facing);
        consumer.setTintIndex(tintIndex);
        consumer.setShade(shade);
        consumer.setHasAmbientOcclusion(shade);
        for (int i = 0; i < CORNERS_PER_QUAD; i++) {
            Vector3f vertexNormal = normals[i].lengthSquared() < NORMAL_EPS ? normal : normals[i].normalize();
            consumer.addVertex(positions[i].x, positions[i].y, positions[i].z)
                    .setColor(255, 255, 255, 255)
                    .setUv(us[i], vs[i])
                    .setUv2(blockLight, skyLight)
                    .setNormal(vertexNormal.x, vertexNormal.y, vertexNormal.z);
        }
        return consumer.bakeQuad();
    }

    private static void contractUvs(float[] us, float[] vs, TextureAtlasSprite sprite) {
        float texelsU = sprite.contents().width() / (sprite.getU1() - sprite.getU0());
        float texelsV = sprite.contents().height() / (sprite.getV1() - sprite.getV0());
        float texels = Math.max(texelsU, texelsV);
        float minShift = 1.0F / (texels * CONTRACT_DIVISOR);
        float centerU = (us[0] + us[1] + us[2] + us[3]) / 4.0F;
        float centerV = (vs[0] + vs[1] + vs[2] + vs[3]) / 4.0F;
        for (int i = 0; i < CORNERS_PER_QUAD; i++) {
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

    private static Vector3f faceNormal(Vector3fc[] positions) {
        Vector3f edge1 = new Vector3f(positions[1]).sub(positions[0]);
        Vector3f edge2 = new Vector3f(positions[2]).sub(positions[0]);
        Vector3f normal = new Vector3f();
        edge1.cross(edge2, normal);
        return normal;
    }
}
