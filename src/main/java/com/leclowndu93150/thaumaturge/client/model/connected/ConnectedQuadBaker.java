package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public final class ConnectedQuadBaker {
    public static final float FACE_SIZE = 16.0F;

    private ConnectedQuadBaker() {}

    public static TextureAtlasSprite material(ConnectedBakeContext baker, ResourceLocation texture) {
        return baker.sprite(texture);
    }

    public static BakedQuad bake(
            ConnectedBakeContext baker,
            TextureAtlasSprite material,
            Direction face,
            float inset,
            float minU,
            float minV,
            float maxU,
            float maxV,
            Uvs uvs) {
        Vector3f from = new Vector3f();
        Vector3f to = new Vector3f(FACE_SIZE, FACE_SIZE, FACE_SIZE);
        switch (face) {
            case DOWN -> {
                from.set(minU, 0.0F, FACE_SIZE - maxV);
                to.set(maxU, FACE_SIZE, FACE_SIZE - minV);
            }
            case UP -> {
                from.set(minU, 0.0F, minV);
                to.set(maxU, FACE_SIZE, maxV);
            }
            case NORTH -> {
                from.set(FACE_SIZE - maxU, FACE_SIZE - maxV, 0.0F);
                to.set(FACE_SIZE - minU, FACE_SIZE - minV, FACE_SIZE);
            }
            case SOUTH -> {
                from.set(minU, FACE_SIZE - maxV, 0.0F);
                to.set(maxU, FACE_SIZE - minV, FACE_SIZE);
            }
            case WEST -> {
                from.set(0.0F, FACE_SIZE - maxV, minU);
                to.set(FACE_SIZE, FACE_SIZE - minV, maxU);
            }
            case EAST -> {
                from.set(0.0F, FACE_SIZE - maxV, FACE_SIZE - maxU);
                to.set(FACE_SIZE, FACE_SIZE - minV, FACE_SIZE - minU);
            }
        }
        switch (face) {
            case DOWN -> from.y = inset;
            case UP -> to.y = FACE_SIZE - inset;
            case NORTH -> from.z = inset;
            case SOUTH -> to.z = FACE_SIZE - inset;
            case WEST -> from.x = inset;
            case EAST -> to.x = FACE_SIZE - inset;
        }
        BlockElementFace element = new BlockElementFace(
                inset > 0.0F ? null : face,
                BlockElementFace.NO_TINT,
                material.contents().name().toString(),
                new BlockFaceUV(new float[] {uvs.minU(), uvs.minV(), uvs.maxU(), uvs.maxV()}, 0));
        return new FaceBakery().bakeQuad(from, to, element, material, face, BlockModelRotation.X0_Y0, null, true);
    }

    public static BakedQuad bakeWhole(
            ConnectedBakeContext baker, TextureAtlasSprite material, Direction face, float inset, Uvs uvs) {
        return bake(baker, material, face, inset, 0.0F, 0.0F, FACE_SIZE, FACE_SIZE, uvs);
    }

    public record Uvs(float minU, float minV, float maxU, float maxV) {}
}
