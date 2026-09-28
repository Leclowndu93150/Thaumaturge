package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;

public final class VeinTaintOverlayPattern implements TaintOverlayPattern {
    private static final int WASH = ARGB32.color(56, 110, 48, 140);
    private static final int WASH_SHADOW = ARGB32.color(96, 70, 30, 92);
    private static final int VEIN_SHADE = ARGB32.color(224, 70, 30, 86);
    private static final int VEIN_MID = ARGB32.color(224, 90, 40, 110);
    private static final int VEIN_LIT = ARGB32.color(224, 112, 52, 134);
    private static final int PUSTULE_RIM = ARGB32.color(210, 122, 38, 118);
    private static final int[] PUSTULE_CORE = {
        ARGB32.color(255, 220, 116, 210),
        ARGB32.color(255, 204, 94, 194),
        ARGB32.color(255, 204, 94, 194),
        ARGB32.color(255, 186, 76, 176)
    };
    private static final float CELLS_PER_WIDTH = 12.0F;
    private static final int MIN_SPACING = 4;
    private static final float JITTER = 0.8F;
    private static final float VEIN_WIDTH = 0.95F;
    private static final float DENSITY_FLOOR = 0.35F;
    private static final float DENSITY_GAIN = 1.1F;
    private static final int DENSITY_SCALE = 11;
    private static final int AREA_PER_PUSTULE = 700;
    private static final int MAX_PUSTULES = 3;
    private static final int PUSTULE_MIN_AREA = 48;
    private static final int PUSTULE_SIZE = 4;

    @Override
    public void paint(NativeImage image, ModelUvLayout layout, long seed) {
        int spacing = Math.max(MIN_SPACING, Math.round(layout.width() / CELLS_PER_WIDTH));
        boolean[][] vein = new boolean[layout.height()][layout.width()];
        for (int y = 0; y < layout.height(); y++) {
            for (int x = 0; x < layout.width(); x++) {
                vein[y][x] = isVein(x, y, spacing, seed);
            }
        }
        for (UvRect face : layout.faces()) {
            for (int y = face.y(); y < face.y() + face.height(); y++) {
                for (int x = face.x(); x < face.x() + face.width(); x++) {
                    setPixel(image, x, y, color(vein, x, y));
                }
            }
        }
        paintPustules(image, layout, seed);
    }

    private static int color(boolean[][] vein, int x, int y) {
        if (!vein[y][x]) {
            return at(vein, x - 1, y) || at(vein, x, y - 1) ? WASH_SHADOW : WASH;
        }
        boolean lit = !at(vein, x, y - 1) || !at(vein, x - 1, y);
        boolean shade = !at(vein, x, y + 1) || !at(vein, x + 1, y);
        if (lit && !shade) {
            return VEIN_LIT;
        }
        return shade && !lit ? VEIN_SHADE : VEIN_MID;
    }

    private static boolean at(boolean[][] vein, int x, int y) {
        return y >= 0 && y < vein.length && x >= 0 && x < vein[y].length && vein[y][x];
    }

    private static boolean isVein(int x, int y, int spacing, long seed) {
        float px = x + 0.5F;
        float py = y + 0.5F;
        int cellX = Math.floorDiv(x, spacing);
        int cellY = Math.floorDiv(y, spacing);
        float d1 = Float.MAX_VALUE;
        float d2 = Float.MAX_VALUE;
        for (int gy = cellY - 1; gy <= cellY + 1; gy++) {
            for (int gx = cellX - 1; gx <= cellX + 1; gx++) {
                float sx = gx * spacing + spacing / 2.0F + (hash01(gx, gy, seed) - 0.5F) * spacing * JITTER;
                float sy = gy * spacing + spacing / 2.0F + (hash01(gx, gy, seed + 7) - 0.5F) * spacing * JITTER;
                float d = Mth.sqrt((sx - px) * (sx - px) + (sy - py) * (sy - py));
                if (d < d1) {
                    d2 = d1;
                    d1 = d;
                } else if (d < d2) {
                    d2 = d;
                }
            }
        }
        return d2 - d1
                < VEIN_WIDTH * Math.min(1.0F, DENSITY_FLOOR + DENSITY_GAIN * valueNoise(x, y, DENSITY_SCALE, seed + 3));
    }

    private static void paintPustules(NativeImage image, ModelUvLayout layout, long seed) {
        List<UvRect> hosts = new ArrayList<>();
        int hostArea = 0;
        int totalArea = 0;
        for (UvRect face : layout.faces()) {
            totalArea += face.area();
            if (face.area() >= PUSTULE_MIN_AREA && face.width() >= PUSTULE_SIZE && face.height() >= PUSTULE_SIZE) {
                hosts.add(face);
                hostArea += face.area();
            }
        }
        if (hosts.isEmpty()) {
            return;
        }
        int count = Mth.clamp(Math.round(totalArea / (float) AREA_PER_PUSTULE), 1, MAX_PUSTULES);
        for (int i = 0; i < count; i++) {
            UvRect face = pickWeighted(hosts, hostArea, hash01(i, hosts.size(), seed + 19));
            int ox = face.x() + (int) (hash01(face.x() + i, face.y(), seed + 13) * (face.width() - PUSTULE_SIZE + 1));
            int oy = face.y() + (int) (hash01(face.x(), face.y() + i, seed + 17) * (face.height() - PUSTULE_SIZE + 1));
            paintPustule(image, ox, oy);
        }
    }

    private static UvRect pickWeighted(List<UvRect> faces, int totalArea, float roll) {
        float remaining = roll * totalArea;
        for (UvRect face : faces) {
            remaining -= face.area();
            if (remaining <= 0.0F) {
                return face;
            }
        }
        return faces.getLast();
    }

    private static void paintPustule(NativeImage image, int ox, int oy) {
        for (int dy = 0; dy < PUSTULE_SIZE; dy++) {
            for (int dx = 0; dx < PUSTULE_SIZE; dx++) {
                boolean corner = (dx == 0 || dx == PUSTULE_SIZE - 1) && (dy == 0 || dy == PUSTULE_SIZE - 1);
                boolean core = dx > 0 && dx < PUSTULE_SIZE - 1 && dy > 0 && dy < PUSTULE_SIZE - 1;
                if (core) {
                    setPixel(image, ox + dx, oy + dy, PUSTULE_CORE[(dy - 1) * 2 + dx - 1]);
                } else if (!corner) {
                    setPixel(image, ox + dx, oy + dy, PUSTULE_RIM);
                }
            }
        }
    }

    private static void setPixel(NativeImage image, int x, int y, int argb) {
        image.setPixelRGBA(x, y, ABGR32.fromArgb32(argb));
    }

    private static float valueNoise(int x, int y, int scale, long seed) {
        float gx = x / (float) scale;
        float gy = y / (float) scale;
        int x0 = Mth.floor(gx);
        int y0 = Mth.floor(gy);
        float fx = smooth(gx - x0);
        float fy = smooth(gy - y0);
        float top = Mth.lerp(fx, hash01(x0, y0, seed), hash01(x0 + 1, y0, seed));
        float bottom = Mth.lerp(fx, hash01(x0, y0 + 1, seed), hash01(x0 + 1, y0 + 1, seed));
        return Mth.lerp(fy, top, bottom);
    }

    private static float smooth(float t) {
        return t * t * (3.0F - 2.0F * t);
    }

    private static float hash01(int x, int y, long seed) {
        long n = x * 374761393L + y * 668265263L + seed * 2147483647L;
        n = (n ^ (n >>> 13)) * 1274126177L;
        n ^= n >>> 16;
        return (n & 0xFFFF) / 65535.0F;
    }
}
