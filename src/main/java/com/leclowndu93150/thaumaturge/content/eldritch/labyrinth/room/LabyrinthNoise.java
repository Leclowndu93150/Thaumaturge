package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

import net.minecraft.util.Mth;

public final class LabyrinthNoise {
    private static final long GOLDEN = 0x9E3779B97F4A7C15L;
    private static final long MIX_A = 0xBF58476D1CE4E5B9L;
    private static final long MIX_B = 0x94D049BB133111EBL;
    private static final long PRIME_X = 0x6A09E667F3BCC909L;
    private static final long PRIME_Y = 0xBB67AE8584CAA73BL;
    private static final long PRIME_Z = 0x3C6EF372FE94F82BL;
    private static final int UNIT_SHIFT = 40;
    private static final float UNIT_SCALE = 1.0F / (1 << 24);

    private LabyrinthNoise() {}

    private static long mix(long value) {
        long z = value + GOLDEN;
        z = (z ^ (z >>> 30)) * MIX_A;
        z = (z ^ (z >>> 27)) * MIX_B;
        return z ^ (z >>> 31);
    }

    public static long hash(long seed, int x, int y, int z, int salt) {
        return mix(seed ^ mix(x * PRIME_X ^ y * PRIME_Y ^ z * PRIME_Z ^ salt));
    }

    public static float unit(long seed, int x, int y, int z, int salt) {
        return (hash(seed, x, y, z, salt) >>> UNIT_SHIFT) * UNIT_SCALE;
    }

    public static float cluster(long seed, int x, int y, int z, int pitch, int salt) {
        int cellX = Math.floorDiv(x, pitch);
        int cellY = Math.floorDiv(y, pitch);
        int cellZ = Math.floorDiv(z, pitch);
        float fx = smooth((x - cellX * pitch) / (float) pitch);
        float fy = smooth((y - cellY * pitch) / (float) pitch);
        float fz = smooth((z - cellZ * pitch) / (float) pitch);
        float c000 = unit(seed, cellX, cellY, cellZ, salt);
        float c100 = unit(seed, cellX + 1, cellY, cellZ, salt);
        float c010 = unit(seed, cellX, cellY + 1, cellZ, salt);
        float c110 = unit(seed, cellX + 1, cellY + 1, cellZ, salt);
        float c001 = unit(seed, cellX, cellY, cellZ + 1, salt);
        float c101 = unit(seed, cellX + 1, cellY, cellZ + 1, salt);
        float c011 = unit(seed, cellX, cellY + 1, cellZ + 1, salt);
        float c111 = unit(seed, cellX + 1, cellY + 1, cellZ + 1, salt);
        float x00 = Mth.lerp(fx, c000, c100);
        float x10 = Mth.lerp(fx, c010, c110);
        float x01 = Mth.lerp(fx, c001, c101);
        float x11 = Mth.lerp(fx, c011, c111);
        return Mth.lerp(fz, Mth.lerp(fy, x00, x10), Mth.lerp(fy, x01, x11));
    }

    private static float smooth(float t) {
        return t * t * (3.0F - 2.0F * t);
    }
}
