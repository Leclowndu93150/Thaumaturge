package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshQuadBaker;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;

public final class CrystalQuadBaker {
    private static final int FULLBRIGHT = 240;

    private CrystalQuadBaker() {}

    public static void bakePart(
            TTMeshPart part, TextureAtlasSprite sprite, int tintIndex, Matrix4f transform, List<BakedQuad> output) {
        TTMeshQuadBaker.bakePart(part, sprite, tintIndex, transform, false, false, FULLBRIGHT, FULLBRIGHT, output);
    }
}
