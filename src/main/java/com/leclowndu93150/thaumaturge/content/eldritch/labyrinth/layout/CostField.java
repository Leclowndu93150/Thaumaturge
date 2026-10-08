package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

final class CostField {
    private static final int PITCH = 4;
    private static final int BASE_COST = 10;
    private static final int NOISE_COST = 30;
    private static final int JITTER = 4;

    private final int[] costs;

    private CostField(int[] costs) {
        this.costs = costs;
    }

    static CostField create(int width, int depth, float noise, RandomSource random) {
        int latticeWidth = width / PITCH + 2;
        int latticeDepth = depth / PITCH + 2;
        float[] lattice = new float[latticeWidth * latticeDepth];
        for (int i = 0; i < lattice.length; i++) {
            lattice[i] = random.nextFloat();
        }
        int[] costs = new int[width * depth];
        for (int z = 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {
                int lx = x / PITCH;
                int lz = z / PITCH;
                float fx = (x % PITCH) / (float) PITCH;
                float fz = (z % PITCH) / (float) PITCH;
                float top = Mth.lerp(fx, lattice[lx + lz * latticeWidth], lattice[lx + 1 + lz * latticeWidth]);
                float bottom =
                        Mth.lerp(fx, lattice[lx + (lz + 1) * latticeWidth], lattice[lx + 1 + (lz + 1) * latticeWidth]);
                float value = Mth.lerp(fz, top, bottom);
                costs[x + z * width] = BASE_COST + Math.round(value * noise * NOISE_COST) + random.nextInt(JITTER);
            }
        }
        return new CostField(costs);
    }

    int cost(int cell) {
        return costs[cell];
    }
}
