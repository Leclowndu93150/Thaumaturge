package com.leclowndu93150.thaumaturge.client.taint.overlay.pattern;

import java.util.Arrays;
import java.util.List;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public record TaintCoverage(int width, int height, int scale, float[] alpha, int[] owner, List<SurfaceFace> faces) {
    private static final float HALF_PIXEL = 0.5F;
    private static final int NO_FACE = -1;

    public static TaintCoverage of(ModelUvLayout layout, @Nullable TexturePixels base, int scale) {
        int width = layout.width() * scale;
        int height = layout.height() * scale;
        float[] alpha = new float[width * height];
        int[] owner = new int[width * height];
        Arrays.fill(owner, NO_FACE);
        List<SurfaceFace> faces = layout.faces();
        for (int i = 0; i < faces.size(); i++) {
            UvRect rect = faces.get(i).rect();
            for (int y = rect.y() * scale; y < (rect.y() + rect.height()) * scale; y++) {
                for (int x = rect.x() * scale; x < (rect.x() + rect.width()) * scale; x++) {
                    int index = y * width + x;
                    if (owner[index] == NO_FACE) {
                        owner[index] = i;
                        alpha[index] =
                                base == null ? 1.0F : base.alphaAt((x + HALF_PIXEL) / width, (y + HALF_PIXEL) / height);
                    }
                }
            }
        }
        return new TaintCoverage(width, height, scale, alpha, owner, faces);
    }

    public float alpha(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height ? alpha[y * width + x] : 0.0F;
    }

    public boolean covers(int x, int y) {
        return alpha(x, y) > 0.0F;
    }

    public @Nullable SurfaceFace face(int x, int y) {
        int index = x >= 0 && y >= 0 && x < width && y < height ? owner[y * width + x] : NO_FACE;
        return index == NO_FACE ? null : faces.get(index);
    }

    public Vector3f surfacePoint(SurfaceFace face, int x, int y, Vector3f out) {
        return face.point((x + HALF_PIXEL) / scale, (y + HALF_PIXEL) / scale, out);
    }

    public int area() {
        int area = 0;
        for (float value : alpha) {
            if (value > 0.0F) {
                area++;
            }
        }
        return area;
    }
}
