package com.leclowndu93150.thaumaturge.client.taint.overlay.pattern;

import com.leclowndu93150.thaumaturge.client.taint.overlay.ModelPolygonCollector;
import com.leclowndu93150.thaumaturge.client.taint.overlay.ModelRootParts;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public record ModelUvLayout(int width, int height, List<SurfaceFace> faces) {
    private static final int MIN_SIZE = 16;
    private static final int MAX_SIZE = 512;
    private static final int DEFAULT_SIZE = 64;
    private static final float MIN_SPAN = 1.0E-6F;
    private static final float PIXELS_PER_BLOCK = 16.0F;
    private static final String PATH_SEPARATOR = "/";

    public static ModelUvLayout of(Model model) {
        List<ModelPolygonCollector.Vertex[]> polygons = new ArrayList<>();
        List<Matrix4f> poses = new ArrayList<>();
        if (model instanceof ModelRootParts rootParts) {
            PoseStack.Pose identity = new PoseStack().last();
            for (ModelPart root : rootParts.thaumaturge$rootParts()) {
                Map<String, Matrix4f> restPoses = new HashMap<>();
                root.visit(new PoseStack(), (pose, path, index, cube) -> {
                    Matrix4f restPose = restPoses.computeIfAbsent(path, key -> restPose(root, key));
                    ModelPolygonCollector collector = new ModelPolygonCollector();
                    cube.compile(identity, collector, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
                    for (ModelPolygonCollector.Vertex[] polygon : collector.polygons()) {
                        polygons.add(polygon);
                        poses.add(restPose);
                    }
                });
            }
        }
        Map<Integer, Integer> widthVotes = new HashMap<>();
        Map<Integer, Integer> heightVotes = new HashMap<>();
        for (ModelPolygonCollector.Vertex[] v : polygons) {
            vote(widthVotes, distance(v[0], v[1]), Math.abs(v[0].u() - v[1].u()));
            vote(heightVotes, distance(v[1], v[2]), Math.abs(v[1].v() - v[2].v()));
        }
        int width = textureSize(widthVotes);
        int height = textureSize(heightVotes);
        List<SurfaceFace> faces = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            SurfaceFace face = face(polygons.get(i), poses.get(i), width, height);
            if (face != null) {
                faces.add(face);
            }
        }
        return new ModelUvLayout(width, height, List.copyOf(faces));
    }

    private static Matrix4f restPose(ModelPart root, String path) {
        Matrix4f matrix = new Matrix4f();
        apply(matrix, root.getInitialPose());
        ModelPart part = root;
        for (String name : path.split(PATH_SEPARATOR)) {
            if (!name.isEmpty()) {
                part = part.getChild(name);
                apply(matrix, part.getInitialPose());
            }
        }
        return matrix;
    }

    private static void apply(Matrix4f matrix, PartPose pose) {
        matrix.translate(pose.x, pose.y, pose.z);
        if (pose.xRot != 0.0F || pose.yRot != 0.0F || pose.zRot != 0.0F) {
            matrix.rotate(new Quaternionf().rotationZYX(pose.zRot, pose.yRot, pose.xRot));
        }
    }

    private static @Nullable SurfaceFace face(ModelPolygonCollector.Vertex[] v, Matrix4f pose, int width, int height) {
        UvRect rect = rect(v, width, height);
        if (rect == null) {
            return null;
        }
        Vector3f p0 = pose.transformPosition(pixels(v[0]));
        Vector3f edge1 = pose.transformPosition(pixels(v[1])).sub(p0);
        Vector3f edge2 = pose.transformPosition(pixels(v[2])).sub(p0);
        float u0 = v[0].u() * width;
        float v0 = v[0].v() * height;
        float edge1U = v[1].u() * width - u0;
        float edge1V = v[1].v() * height - v0;
        float edge2U = v[2].u() * width - u0;
        float edge2V = v[2].v() * height - v0;
        float determinant = edge1U * edge2V - edge1V * edge2U;
        if (Math.abs(determinant) < MIN_SPAN) {
            return null;
        }
        Vector3f uAxis = new Vector3f(edge1)
                .mul(edge2V)
                .sub(new Vector3f(edge2).mul(edge1V))
                .div(determinant);
        Vector3f vAxis = new Vector3f(edge2)
                .mul(edge1U)
                .sub(new Vector3f(edge1).mul(edge2U))
                .div(determinant);
        Vector3f normal = uAxis.cross(vAxis, new Vector3f());
        if (normal.lengthSquared() < MIN_SPAN) {
            return null;
        }
        Vector3f origin = new Vector3f(p0).sub(new Vector3f(uAxis).mul(u0)).sub(new Vector3f(vAxis).mul(v0));
        return new SurfaceFace(rect, origin, uAxis, vAxis, normal.normalize());
    }

    private static Vector3f pixels(ModelPolygonCollector.Vertex vertex) {
        return new Vector3f(vertex.x(), vertex.y(), vertex.z()).mul(PIXELS_PER_BLOCK);
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

    private static @Nullable UvRect rect(ModelPolygonCollector.Vertex[] vertices, int width, int height) {
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
