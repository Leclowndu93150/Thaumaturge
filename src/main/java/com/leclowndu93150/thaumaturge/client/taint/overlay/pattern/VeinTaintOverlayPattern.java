package com.leclowndu93150.thaumaturge.client.taint.overlay.pattern;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.Arrays;
import net.minecraft.util.FastColor;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class VeinTaintOverlayPattern implements TaintOverlayPattern {
    private static final float CELL = 5.0F;
    private static final float JITTER = 0.9F;
    private static final float HALF = 0.5F;
    private static final float WARP_AMPLITUDE = 0.8F;
    private static final float WARP_SCALE = 3.5F;
    private static final float VEIN_HALF_WIDTH = 0.6F;
    private static final float GLOW_STEP = 0.5F;
    private static final float INTENSITY_SCALE = 11.0F;
    private static final float INTENSITY_LOW = 0.15F;
    private static final float INTENSITY_HIGH = 0.6F;
    private static final float KEEP_MIN = 0.6F;
    private static final float MIN_SLANT = 1.0E-4F;
    private static final int NEIGHBOURS = 27;
    private static final int EDGE_MIX = 31;
    private static final int WASH_RED = 26;
    private static final int WASH_GREEN = 14;
    private static final int WASH_BLUE = 34;
    private static final float WASH_ALPHA = 0.22F;
    private static final int GLOW_RED = 27;
    private static final int GLOW_GREEN = 0;
    private static final int GLOW_BLUE = 42;
    private static final float[] GLOW_ALPHA = {0.0F, 0.64F, 0.46F, 0.12F};
    private static final float GLOW_NOISE = 0.25F;
    private static final float SHADOW_CORNER = 0.52F;
    private static final float SHADOW_EDGE = 0.72F;
    private static final float SHADOW_SOFT = 0.88F;
    private static final float INTERIOR = 0.95F;
    private static final int HIGHLIGHT_EDGE = 6;
    private static final int HIGHLIGHT_CORNER = 12;
    private static final float MAX_CHANNEL = 255.0F;
    private static final int CHANNEL_LIMIT = 255;
    private static final int JITTER_Y_SALT = 7;
    private static final int JITTER_Z_SALT = 13;
    private static final int WARP_X_SALT = 31;
    private static final int WARP_Y_SALT = 37;
    private static final int WARP_Z_SALT = 41;
    private static final int INTENSITY_SALT = 3;
    private static final int EDGE_SALT = 11;
    private static final int GLOW_NOISE_SALT = 23;

    @Override
    public NativeImage paint(TaintCoverage coverage, TexturePixels veinFill, long seed) {
        int width = coverage.width();
        int height = coverage.height();
        boolean[] vein = new boolean[width * height];
        float[] keptDistance = new float[width * height];
        Arrays.fill(keptDistance, Float.MAX_VALUE);
        float[] seeds = new float[NEIGHBOURS * 3];
        int[] cells = new int[NEIGHBOURS * 3];
        float[] distances = new float[NEIGHBOURS];
        Vector3f point = new Vector3f();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                SurfaceFace face = coverage.face(x, y);
                if (face != null && coverage.covers(x, y)) {
                    sampleField(
                            coverage.surfacePoint(face, x, y, point),
                            face.normal(),
                            seed,
                            seeds,
                            cells,
                            distances,
                            vein,
                            keptDistance,
                            y * width + x);
                }
            }
        }
        NativeImage overlay = new NativeImage(width, height, true);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float alpha = coverage.alpha(x, y);
                if (alpha > 0.0F) {
                    int index = y * width + x;
                    int color = vein[index]
                            ? veinColor(vein, width, height, x, y, veinFill)
                            : washColor(keptDistance[index], x, y, seed);
                    overlay.setPixelRGBA(
                            x,
                            y,
                            FastColor.ABGR32.fromArgb32(ARGB32.color(Math.round(ARGB32.alpha(color) * alpha), color)));
                }
            }
        }
        return overlay;
    }

    private static void sampleField(
            Vector3f p,
            Vector3fc normal,
            long seed,
            float[] seeds,
            int[] cells,
            float[] distances,
            boolean[] vein,
            float[] keptDistance,
            int index) {
        float wx = p.x() / WARP_SCALE;
        float wy = p.y() / WARP_SCALE;
        float wz = p.z() / WARP_SCALE;
        float qx = p.x() + (valueNoise(wx, wy, wz, seed + WARP_X_SALT) - HALF) * 2.0F * WARP_AMPLITUDE;
        float qy = p.y() + (valueNoise(wx, wy, wz, seed + WARP_Y_SALT) - HALF) * 2.0F * WARP_AMPLITUDE;
        float qz = p.z() + (valueNoise(wx, wy, wz, seed + WARP_Z_SALT) - HALF) * 2.0F * WARP_AMPLITUDE;
        int cellX = Mth.floor(qx / CELL);
        int cellY = Mth.floor(qy / CELL);
        int cellZ = Mth.floor(qz / CELL);
        int nearest = 0;
        int n = 0;
        for (int gz = cellZ - 1; gz <= cellZ + 1; gz++) {
            for (int gy = cellY - 1; gy <= cellY + 1; gy++) {
                for (int gx = cellX - 1; gx <= cellX + 1; gx++) {
                    float sx = (gx + HALF + (hash01(gx, gy, gz, seed) - HALF) * JITTER) * CELL;
                    float sy = (gy + HALF + (hash01(gx, gy, gz, seed + JITTER_Y_SALT) - HALF) * JITTER) * CELL;
                    float sz = (gz + HALF + (hash01(gx, gy, gz, seed + JITTER_Z_SALT) - HALF) * JITTER) * CELL;
                    seeds[n * 3] = sx;
                    seeds[n * 3 + 1] = sy;
                    seeds[n * 3 + 2] = sz;
                    cells[n * 3] = gx;
                    cells[n * 3 + 1] = gy;
                    cells[n * 3 + 2] = gz;
                    distances[n] = (sx - qx) * (sx - qx) + (sy - qy) * (sy - qy) + (sz - qz) * (sz - qz);
                    if (distances[n] < distances[nearest]) {
                        nearest = n;
                    }
                    n++;
                }
            }
        }
        float keep = KEEP_MIN
                + (1.0F - KEEP_MIN)
                        * smoothstep(
                                INTENSITY_LOW,
                                INTENSITY_HIGH,
                                valueNoise(
                                        p.x() / INTENSITY_SCALE,
                                        p.y() / INTENSITY_SCALE,
                                        p.z() / INTENSITY_SCALE,
                                        seed + INTENSITY_SALT));
        float best = Float.MAX_VALUE;
        boolean bestKept = false;
        float bestKeptDistance = Float.MAX_VALUE;
        for (int j = 0; j < NEIGHBOURS; j++) {
            if (j == nearest) {
                continue;
            }
            float ax = seeds[j * 3] - seeds[nearest * 3];
            float ay = seeds[j * 3 + 1] - seeds[nearest * 3 + 1];
            float az = seeds[j * 3 + 2] - seeds[nearest * 3 + 2];
            float length = Mth.sqrt(ax * ax + ay * ay + az * az);
            float cosine = (ax * normal.x() + ay * normal.y() + az * normal.z()) / length;
            float inPlane = (distances[j] - distances[nearest])
                    / (2.0F * length)
                    / Mth.sqrt(Math.max(MIN_SLANT, 1.0F - cosine * cosine));
            boolean kept = edgeHash(cells, nearest, j, seed) < keep;
            if (inPlane < best) {
                best = inPlane;
                bestKept = kept;
            }
            if (kept && inPlane < bestKeptDistance) {
                bestKeptDistance = inPlane;
            }
        }
        vein[index] = bestKept && best < VEIN_HALF_WIDTH;
        keptDistance[index] = bestKeptDistance;
    }

    private static float edgeHash(int[] cells, int a, int b, long seed) {
        boolean swap = compareCells(cells, a, b) > 0;
        int first = swap ? b : a;
        int second = swap ? a : b;
        return hash01(
                cells[first * 3] * EDGE_MIX + cells[second * 3],
                cells[first * 3 + 1] * EDGE_MIX + cells[second * 3 + 1],
                cells[first * 3 + 2] * EDGE_MIX + cells[second * 3 + 2],
                seed + EDGE_SALT);
    }

    private static int compareCells(int[] cells, int a, int b) {
        for (int axis = 0; axis < 3; axis++) {
            int compare = Integer.compare(cells[a * 3 + axis], cells[b * 3 + axis]);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

    private static int veinColor(boolean[] vein, int width, int height, int x, int y, TexturePixels fill) {
        int base = fill.wrapped(x, y);
        int dark = (at(vein, width, height, x + 1, y) ? 0 : 1) + (at(vein, width, height, x, y + 1) ? 0 : 1);
        int lit = (at(vein, width, height, x - 1, y) ? 0 : 1) + (at(vein, width, height, x, y - 1) ? 0 : 1);
        if (dark == 2) {
            return scaled(base, SHADOW_CORNER);
        }
        if (dark == 1) {
            return scaled(base, lit == 0 ? SHADOW_EDGE : SHADOW_SOFT);
        }
        if (lit == 2) {
            return lifted(base, HIGHLIGHT_CORNER);
        }
        return lit == 1 ? lifted(base, HIGHLIGHT_EDGE) : scaled(base, INTERIOR);
    }

    private static int washColor(float keptDistance, int x, int y, long seed) {
        int band = Mth.ceil((keptDistance - VEIN_HALF_WIDTH) / GLOW_STEP);
        float glow = band >= 1 && band < GLOW_ALPHA.length
                ? GLOW_ALPHA[band] * (1.0F - GLOW_NOISE * hash01(x, y, 0, seed + GLOW_NOISE_SALT))
                : 0.0F;
        float alpha = WASH_ALPHA + glow - WASH_ALPHA * glow;
        float washWeight = WASH_ALPHA * (1.0F - glow) / alpha;
        float glowWeight = glow / alpha;
        return ARGB32.color(
                Math.round(alpha * MAX_CHANNEL),
                Math.round(WASH_RED * washWeight + GLOW_RED * glowWeight),
                Math.round(WASH_GREEN * washWeight + GLOW_GREEN * glowWeight),
                Math.round(WASH_BLUE * washWeight + GLOW_BLUE * glowWeight));
    }

    private static boolean at(boolean[] vein, int width, int height, int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height && vein[y * width + x];
    }

    private static int scaled(int color, float factor) {
        return ARGB32.color(
                ARGB32.alpha(color), (int) (ARGB32.red(color) * factor), (int) (ARGB32.green(color) * factor), (int)
                        (ARGB32.blue(color) * factor));
    }

    private static int lifted(int color, int amount) {
        return ARGB32.color(
                ARGB32.alpha(color),
                Math.min(CHANNEL_LIMIT, ARGB32.red(color) + amount),
                Math.min(CHANNEL_LIMIT, ARGB32.green(color) + amount),
                Math.min(CHANNEL_LIMIT, ARGB32.blue(color) + amount));
    }

    private static float smoothstep(float low, float high, float value) {
        float t = Mth.clamp((value - low) / (high - low), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float valueNoise(float x, float y, float z, long seed) {
        int x0 = Mth.floor(x);
        int y0 = Mth.floor(y);
        int z0 = Mth.floor(z);
        float fx = smooth(x - x0);
        float fy = smooth(y - y0);
        float fz = smooth(z - z0);
        float near = Mth.lerp(
                fy,
                Mth.lerp(fx, hash01(x0, y0, z0, seed), hash01(x0 + 1, y0, z0, seed)),
                Mth.lerp(fx, hash01(x0, y0 + 1, z0, seed), hash01(x0 + 1, y0 + 1, z0, seed)));
        float far = Mth.lerp(
                fy,
                Mth.lerp(fx, hash01(x0, y0, z0 + 1, seed), hash01(x0 + 1, y0, z0 + 1, seed)),
                Mth.lerp(fx, hash01(x0, y0 + 1, z0 + 1, seed), hash01(x0 + 1, y0 + 1, z0 + 1, seed)));
        return Mth.lerp(fz, near, far);
    }

    private static float smooth(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    private static float hash01(int x, int y, int z, long seed) {
        long n = x * 374761393L + y * 668265263L + z * 1440662683L + seed * 2147483647L;
        n = (n ^ (n >>> 13)) * 1274126177L;
        n ^= n >>> 16;
        return (n & 0xFFFF) / 65535.0F;
    }
}
