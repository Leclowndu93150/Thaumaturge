package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public record ModelUvLayout(int width, int height, List<UvRect> faces) {
    private static final int MIN_SIZE = 16;
    private static final int MAX_SIZE = 512;
    private static final int DEFAULT_SIZE = 64;
    private static final float MIN_SPAN = 1.0E-6F;
    private static final float PIXELS_PER_BLOCK = 16.0F;
    private static final ModelUvLayout FULL_CANVAS =
            new ModelUvLayout(DEFAULT_SIZE, DEFAULT_SIZE, List.of(new UvRect(0, 0, DEFAULT_SIZE, DEFAULT_SIZE)));

    public static ModelUvLayout of(Model model) {
        if (!(model instanceof ModelRootParts rootParts)) {
            return FULL_CANVAS;
        }
        ModelPolygonCollector collector = new ModelPolygonCollector();
        PoseStack.Pose identity = new PoseStack().last();
        for (ModelPart part : rootParts.thaumaturge$rootParts()) {
            part.visit(
                    new PoseStack(),
                    (pose, path, index, cube) ->
                            cube.compile(identity, collector, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1));
        }
        List<ModelPolygonCollector.Vertex[]> polygons = collector.polygons();
        Map<Integer, Integer> widthVotes = new HashMap<>();
        Map<Integer, Integer> heightVotes = new HashMap<>();
        for (ModelPolygonCollector.Vertex[] v : polygons) {
            vote(widthVotes, distance(v[0], v[1]), Math.abs(v[0].u() - v[1].u()));
            vote(heightVotes, distance(v[1], v[2]), Math.abs(v[1].v() - v[2].v()));
        }
        int width = textureSize(widthVotes);
        int height = textureSize(heightVotes);
        Set<UvRect> faces = new LinkedHashSet<>();
        for (ModelPolygonCollector.Vertex[] polygon : polygons) {
            UvRect rect = rect(polygon, width, height);
            if (rect != null) {
                faces.add(rect);
            }
        }
        return faces.isEmpty() ? FULL_CANVAS : new ModelUvLayout(width, height, List.copyOf(faces));
    }

    private static void vote(Map<Integer, Integer> votes, float length, float span) {
        if (span > MIN_SPAN && length > 0.0F) {
            votes.merge(Math.round(length / span), 1, Integer::sum);
        }
    }

    private static int textureSize(Map<Integer, Integer> votes) {
        int best = DEFAULT_SIZE;
        int bestCount = 0;
        for (Map.Entry<Integer, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > bestCount) {
                best = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        return Mth.clamp(Mth.smallestEncompassingPowerOfTwo(Math.max(1, best)), MIN_SIZE, MAX_SIZE);
    }

    private static float distance(ModelPolygonCollector.Vertex a, ModelPolygonCollector.Vertex b) {
        float dx = a.x() - b.x();
        float dy = a.y() - b.y();
        float dz = a.z() - b.z();
        return Mth.sqrt(dx * dx + dy * dy + dz * dz) * PIXELS_PER_BLOCK;
    }

    private static UvRect rect(ModelPolygonCollector.Vertex[] vertices, int width, int height) {
        float minU = Float.MAX_VALUE;
        float minV = Float.MAX_VALUE;
        float maxU = -Float.MAX_VALUE;
        float maxV = -Float.MAX_VALUE;
        for (ModelPolygonCollector.Vertex vertex : vertices) {
            minU = Math.min(minU, vertex.u());
            maxU = Math.max(maxU, vertex.u());
            minV = Math.min(minV, vertex.v());
            maxV = Math.max(maxV, vertex.v());
        }
        int x0 = Mth.clamp(Math.round(minU * width), 0, width);
        int x1 = Mth.clamp(Math.round(maxU * width), 0, width);
        int y0 = Mth.clamp(Math.round(minV * height), 0, height);
        int y1 = Mth.clamp(Math.round(maxV * height), 0, height);
        return x1 > x0 && y1 > y0 ? new UvRect(x0, y0, x1 - x0, y1 - y0) : null;
    }
}
