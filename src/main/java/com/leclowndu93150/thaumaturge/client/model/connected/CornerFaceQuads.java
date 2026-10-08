package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class CornerFaceQuads implements ConnectedFaceQuads {
    private static final float HALF = ConnectedQuadBaker.FACE_SIZE / 2.0F;
    private static final ConnectedQuadBaker.Uvs FULL_UVS =
            new ConnectedQuadBaker.Uvs(0.0F, 0.0F, ConnectedQuadBaker.FACE_SIZE, ConnectedQuadBaker.FACE_SIZE);
    private static final Direction[] FACES = Direction.values();

    private final BakedQuad[][] quads;

    private CornerFaceQuads(BakedQuad[][] quads) {
        this.quads = quads;
    }

    public static CornerFaceQuads bake(ConnectedBakeContext baker, ResourceLocation directory) {
        ResourceLocation[] spriteIds = FaceCorners.sprites(directory);
        TextureAtlasSprite[] materials = new TextureAtlasSprite[spriteIds.length];
        for (int slot = 0; slot < spriteIds.length; slot++) {
            materials[slot] = ConnectedQuadBaker.material(baker, spriteIds[slot]);
        }

        BakedQuad[][] quads = new BakedQuad[FACES.length][spriteIds.length];
        for (Direction face : FACES) {
            for (int corner = 0; corner < FaceCorners.COUNT; corner++) {
                float minU = HALF * FaceCorners.column(corner);
                float minV = HALF * FaceCorners.row(corner);
                for (int state = 0; state < FaceCorners.STATES; state++) {
                    int slot = FaceCorners.slot(corner, state);
                    quads[face.ordinal()][slot] = ConnectedQuadBaker.bake(
                            baker, materials[slot], face, 0.0F, minU, minV, minU + HALF, minV + HALF, FULL_UVS);
                }
            }
        }
        return new CornerFaceQuads(quads);
    }

    @Override
    public boolean connects(Direction face) {
        return true;
    }

    @Override
    public void addFace(Direction face, int connections, ConnectedQuads.Builder builder) {
        BakedQuad[] faceQuads = quads[face.ordinal()];
        for (int corner = 0; corner < FaceCorners.COUNT; corner++) {
            builder.addCulledFace(face, faceQuads[FaceCorners.slot(corner, FaceCorners.state(connections, corner))]);
        }
    }
}
