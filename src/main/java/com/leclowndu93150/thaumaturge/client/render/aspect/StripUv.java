package com.leclowndu93150.thaumaturge.client.render.aspect;

public final class StripUv {
    public static final float V0 = 0.0F;
    public static final float V1 = 1.0F;

    private StripUv() {}

    public static float u0(int frame, int frameCount) {
        return frame / (float) frameCount;
    }

    public static float u1(int frame, int frameCount) {
        return (frame + 1) / (float) frameCount;
    }
}
