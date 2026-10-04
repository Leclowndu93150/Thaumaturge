package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshQuadBaker;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;

public final class CrystalQuadBaker {
    private static final int FULLBRIGHT = 240;

    private CrystalQuadBaker() {}

    public static void bakePart(
            TCMeshPart part, TextureAtlasSprite sprite, int tintIndex, Matrix4f transform, List<BakedQuad> output) {
        TCMeshQuadBaker.bakePart(part, sprite, tintIndex, transform, false, false, FULLBRIGHT, FULLBRIGHT, output);
    }
}
