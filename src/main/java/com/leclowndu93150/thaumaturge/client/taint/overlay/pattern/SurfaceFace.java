package com.leclowndu93150.thaumaturge.client.taint.overlay.pattern;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public record SurfaceFace(UvRect rect, Vector3fc origin, Vector3fc uAxis, Vector3fc vAxis, Vector3fc normal) {
    public Vector3f point(float u, float v, Vector3f out) {
        return out.set(origin).fma(u, uAxis).fma(v, vAxis);
    }
}
