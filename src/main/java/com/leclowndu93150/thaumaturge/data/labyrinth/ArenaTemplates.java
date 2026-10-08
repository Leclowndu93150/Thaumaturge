package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

final class ArenaTemplates {
    static final ResourceLocation TAINT = TTIds.rl("labyrinth/arena/taint");

    private static final int SIZE = 17;
    private static final int HEIGHT = 2;
    private static final int CENTER = 8;
    private static final double TAINT_INNER = 3.0;
    private static final double TAINT_OUTER = 8.0;
    private static final int SOIL_PERIOD = 4;
    private static final int ROCK_PERIOD = 11;
    private static final int HASH_X = 31;
    private static final int HASH_Z = 17;

    private ArenaTemplates() {}

    static Map<ResourceLocation, SiteCanvas> all() {
        return Map.of(TAINT, taint());
    }

    private static SiteCanvas taint() {
        SiteCanvas canvas = new SiteCanvas(SIZE, HEIGHT, SIZE);
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                double distance = Math.hypot(x - CENTER, z - CENTER);
                if (distance < TAINT_INNER || distance > TAINT_OUTER) {
                    continue;
                }
                int hash = x * HASH_X + z * HASH_Z + x * z;
                if (hash % ROCK_PERIOD == 0) {
                    canvas.set(x, 0, z, TTBlocks.TAINT_ROCK.get().defaultBlockState());
                    canvas.set(x, 1, z, TTBlocks.TAINT_ROCK.get().defaultBlockState());
                } else if (hash % SOIL_PERIOD == 0) {
                    canvas.set(x, 0, z, TTBlocks.TAINT_SOIL.get().defaultBlockState());
                }
            }
        }
        return canvas;
    }
}
