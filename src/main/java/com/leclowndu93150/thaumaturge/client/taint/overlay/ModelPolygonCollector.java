package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;

public final class ModelPolygonCollector implements VertexConsumer {
    private static final int VERTICES_PER_POLYGON = 4;

    private final List<Vertex[]> polygons = new ArrayList<>();
    private Vertex[] pending = new Vertex[VERTICES_PER_POLYGON];
    private int pendingCount;
    private float x;
    private float y;
    private float z;

    public List<Vertex[]> polygons() {
        return polygons;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        pending[pendingCount++] = new Vertex(x, y, z, u, v);
        if (pendingCount == VERTICES_PER_POLYGON) {
            polygons.add(pending);
            pending = new Vertex[VERTICES_PER_POLYGON];
            pendingCount = 0;
        }
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        return this;
    }

    public record Vertex(float x, float y, float z, float u, float v) {}
}
