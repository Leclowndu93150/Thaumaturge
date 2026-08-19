package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshLoader;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public final class GolemMeshes {
    private static final Map<Identifier, TCMesh> CACHE = new ConcurrentHashMap<>();

    private GolemMeshes() {}

    public static TCMesh get(Identifier meshLocation) {
        return CACHE.computeIfAbsent(meshLocation, location -> {
            try {
                return TCMeshLoader.load(Minecraft.getInstance().getResourceManager(), location);
            } catch (IOException e) {
                Thaumaturge.LOGGER.error("Failed to load mesh {}", location, e);
                return TCMesh.EMPTY;
            }
        });
    }

    public static void renderPart(TCMeshPart part, PoseStack.Pose pose, VertexConsumer buffer, int light, int color) {
        float[] positions = part.positions();
        float[] uvs = part.uvs();
        float[] normals = part.normals();
        int vertexCount = part.quadCount() * 4;
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            float u = 0.0F;
            float v = 0.0F;
            if (uvs != null) {
                u = uvs[vertex * 2];
                v = 1.0F - uvs[vertex * 2 + 1];
            }
            float nx = normals[vertex * 3];
            float ny = normals[vertex * 3 + 1];
            float nz = normals[vertex * 3 + 2];
            buffer.addVertex(pose, positions[vertex * 3], positions[vertex * 3 + 1], positions[vertex * 3 + 2]).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(pose, nx, ny, nz);
        }
    }
}
