package com.leclowndu93150.thaumaturge.content.research.note;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class HexGrid {
    public static final int[][] NEIGHBOURS = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};

    private HexGrid() {}

    public record Hex(int q, int r) {
        public static final Codec<Hex> CODEC = RecordCodecBuilder
                .create(instance -> instance.group(Codec.INT.fieldOf("q").forGetter(Hex::q), Codec.INT.fieldOf("r").forGetter(Hex::r)).apply(instance, Hex::new));

        public Hex neighbour(int direction) {
            return new Hex(q + NEIGHBOURS[direction][0], r + NEIGHBOURS[direction][1]);
        }

        public float pixelX(float size) {
            return size * 1.5F * q;
        }

        public float pixelY(float size) {
            return (float) (size * Math.sqrt(3.0) * (r + q / 2.0));
        }
    }

    public static int distance(Hex a, Hex b) {
        int dq = a.q() - b.q();
        int dr = a.r() - b.r();
        return (Math.abs(dq) + Math.abs(dr) + Math.abs(dq + dr)) / 2;
    }

    public static List<Hex> ring(int radius) {
        List<Hex> result = new ArrayList<>();
        Hex hex = new Hex(NEIGHBOURS[4][0] * radius, NEIGHBOURS[4][1] * radius);
        for (int side = 0; side < 6; side++) {
            for (int step = 0; step < radius; step++) {
                result.add(hex);
                hex = hex.neighbour(side);
            }
        }
        return result;
    }

    public static List<Hex> disk(int radius) {
        List<Hex> result = new ArrayList<>();
        result.add(new Hex(0, 0));
        for (int r = 1; r <= radius; r++) {
            result.addAll(ring(r));
        }
        return result;
    }

    public static List<Hex> distributeRing(int radius, int entries, RandomSource random) {
        List<Hex> ring = ring(radius);
        List<Hex> result = new ArrayList<>();
        float spacing = ring.size() / (float) entries;
        float pos = random.nextInt(ring.size());
        for (int i = 0; i < entries; i++) {
            result.add(ring.get(Math.round(pos) % ring.size()));
            pos += spacing;
        }
        return result;
    }

    public static Hex pixelToHex(float x, float y, float size) {
        float q = x / (1.5F * size);
        float r = (float) (y / (size * Math.sqrt(3.0))) - q / 2.0F;
        return roundHex(q, r);
    }

    private static Hex roundHex(float q, float r) {
        float s = -q - r;
        int rq = Math.round(q);
        int rr = Math.round(r);
        int rs = Math.round(s);
        float dq = Math.abs(rq - q);
        float dr = Math.abs(rr - r);
        float ds = Math.abs(rs - s);
        if (dq > dr && dq > ds) {
            rq = -rr - rs;
        } else if (dr > ds) {
            rr = -rq - rs;
        }
        return new Hex(rq, rr);
    }

    public static Hex clampToDisk(Hex hex, int radius) {
        if (distance(new Hex(0, 0), hex) <= radius) {
            return hex;
        }
        return new Hex(Mth.clamp(hex.q(), -radius, radius), Mth.clamp(hex.r(), -radius, radius));
    }
}
