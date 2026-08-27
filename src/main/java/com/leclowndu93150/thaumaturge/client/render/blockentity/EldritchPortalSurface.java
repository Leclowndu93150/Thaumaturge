package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.TCShaders;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class EldritchPortalSurface {
    public static final ResourceLocation TUNNEL_TEXTURE = TCIds.rl("textures/misc/tunnel.png");
    public static final ResourceLocation PARTICLE_FIELD_TEXTURE = TCIds.rl("textures/misc/particlefield.png");

    public static final RenderType SURFACE = RenderType.create(
            "tc_eldritch_portal_surface",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(TCShaders::portal))
                    .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                            .add(TUNNEL_TEXTURE, false, false)
                            .add(PARTICLE_FIELD_TEXTURE, true, false)
                            .build())
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    private EldritchPortalSurface() {}

    public static void quad(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            BlockPos worldPos,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3,
            float x4,
            float y4,
            float z4) {
        Matrix4f mat = pose.pose();
        float e1x = x2 - x1;
        float e1y = y2 - y1;
        float e1z = z2 - z1;
        float e2x = x3 - x1;
        float e2y = y3 - y1;
        float e2z = z3 - z1;
        float nx = Math.abs(e1y * e2z - e1z * e2y);
        float ny = Math.abs(e1z * e2x - e1x * e2z);
        float nz = Math.abs(e1x * e2y - e1y * e2x);
        int axis = ny >= nx && ny >= nz ? 1 : nx >= nz ? 0 : 2;
        addVertex(buffer, mat, worldPos, axis, x1, y1, z1);
        addVertex(buffer, mat, worldPos, axis, x2, y2, z2);
        addVertex(buffer, mat, worldPos, axis, x3, y3, z3);
        addVertex(buffer, mat, worldPos, axis, x4, y4, z4);
    }

    private static void addVertex(
            VertexConsumer buffer, Matrix4f mat, BlockPos worldPos, int axis, float x, float y, float z) {
        float wx = worldPos.getX() + x;
        float wy = worldPos.getY() + y;
        float wz = worldPos.getZ() + z;
        float u;
        float v;
        if (axis == 1) {
            u = wx;
            v = wz;
        } else if (axis == 0) {
            u = wz;
            v = wy;
        } else {
            u = wx;
            v = wy;
        }
        buffer.addVertex(mat, x, y, z).setUv(u, v);
    }
}
