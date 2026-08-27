package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.model.entity.GrapplerModel;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.entity.projectile.EntityGrapple;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class GrappleRenderer extends EntityRenderer<EntityGrapple> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/entity/grappler.png");
    private static final ResourceLocation ROPE = TCIds.rl("textures/misc/rope.png");
    private static final RenderType ROPE_TYPE = TCRenderTypes.fxTranslucent(ROPE);
    private static final RenderType GLOW_TYPE = TCRenderTypes.fxAdditiveBlurred(ParticleTextures.PARTICLES);

    private static final double ROPE_RADIUS = 0.025;
    private static final int ROPE_SIDES = 4;
    private static final int EMISSIVE_LIGHT = 0x00F000DC;
    private static final float GLOW_ALPHA = 0.21F;
    private static final float GLOW_HALF = 0.5F;

    private final GrapplerModel model;

    public GrappleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new GrapplerModel(context.bakeLayer(TCModelLayers.GRAPPLER));
    }

    @Override
    public void render(
            EntityGrapple entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        int ticks = entity.tickCount;
        List<Vec3> points = new ArrayList<>();
        calcPoints(entity.getOwner(), entity, partialTicks, points);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        model.root.render(
                poseStack, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        float bob = Mth.sin(ticks / 5.0F) * 0.2F + 0.2F;
        float glowScale = 1.0F + bob;
        float u0 = (1 + ticks % 6) / 32.0F;
        float u1 = u0 + 0.03125F;
        float v0 = 0.21875F;
        float v1 = v0 + 0.03125F;
        int glowTint = ARGB32.colorFromFloat(GLOW_ALPHA, 1.0F, 1.0F, 1.0F);
        float half = GLOW_HALF * glowScale;
        Vec3 glowOrigin = entity.getPosition(partialTicks);
        LateWorldRenderQueue.enqueue(glowOrigin, (latePose, lateBuffers) -> {
            latePose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            writeGlow(lateBuffers.getBuffer(GLOW_TYPE), latePose.last().pose(), half, u0, v0, u1, v1, glowTint);
        });
        if (points.size() > 2) {
            submitRope(buffers.getBuffer(ROPE_TYPE), poseStack.last().pose(), points, packedLight);
        }
    }

    private static void writeGlow(
            VertexConsumer glowBuffer,
            Matrix4f glowMat,
            float half,
            float u0,
            float v0,
            float u1,
            float v1,
            int glowTint) {
        glowBuffer
                .addVertex(glowMat, -half, -half, 0.0F)
                .setUv(u0, v1)
                .setColor(glowTint)
                .setLight(EMISSIVE_LIGHT);
        glowBuffer
                .addVertex(glowMat, half, -half, 0.0F)
                .setUv(u1, v1)
                .setColor(glowTint)
                .setLight(EMISSIVE_LIGHT);
        glowBuffer
                .addVertex(glowMat, half, half, 0.0F)
                .setUv(u1, v0)
                .setColor(glowTint)
                .setLight(EMISSIVE_LIGHT);
        glowBuffer
                .addVertex(glowMat, -half, half, 0.0F)
                .setUv(u0, v0)
                .setColor(glowTint)
                .setLight(EMISSIVE_LIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityGrapple entity) {
        return TEXTURE;
    }

    private static void submitRope(VertexConsumer buffer, Matrix4f mat, List<Vec3> points, int light) {
        Vector3f[] previousRing = null;
        double previousV = 0.0;
        for (int index = 0; index < points.size(); index++) {
            Vec3 center = points.get(index);
            Vec3 next = points.get(Math.min(index + 1, points.size() - 1));
            Vec3 prev = points.get(Math.max(index - 1, 0));
            Vec3 direction = next.subtract(prev);
            if (direction.lengthSqr() < 1.0E-8) {
                direction = new Vec3(0.0, 1.0, 0.0);
            }
            Vector3f[] ring = buildRing(center, direction.normalize());
            double v = index * 0.5;
            if (previousRing != null) {
                for (int side = 0; side < ROPE_SIDES; side++) {
                    int nextSide = (side + 1) % ROPE_SIDES;
                    addRopeVertex(buffer, mat, previousRing[side], side / (float) ROPE_SIDES, (float) previousV, light);
                    addRopeVertex(
                            buffer,
                            mat,
                            previousRing[nextSide],
                            (side + 1) / (float) ROPE_SIDES,
                            (float) previousV,
                            light);
                    addRopeVertex(buffer, mat, ring[nextSide], (side + 1) / (float) ROPE_SIDES, (float) v, light);
                    addRopeVertex(buffer, mat, ring[side], side / (float) ROPE_SIDES, (float) v, light);
                }
            }
            previousRing = ring;
            previousV = v;
        }
    }

    private static void addRopeVertex(VertexConsumer buffer, Matrix4f mat, Vector3f pos, float u, float v, int light) {
        buffer.addVertex(mat, pos.x(), pos.y(), pos.z())
                .setUv(u, v)
                .setColor(-1)
                .setLight(light);
    }

    private static Vector3f[] buildRing(Vec3 center, Vec3 direction) {
        Vec3 reference = Math.abs(direction.y) > 0.99 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = direction.cross(reference).normalize();
        Vec3 up = direction.cross(side).normalize();
        Vector3f[] ring = new Vector3f[ROPE_SIDES];
        for (int index = 0; index < ROPE_SIDES; index++) {
            double angle = index * (Math.PI * 2.0 / ROPE_SIDES);
            Vec3 offset = side.scale(Math.cos(angle) * ROPE_RADIUS).add(up.scale(Math.sin(angle) * ROPE_RADIUS));
            ring[index] = new Vector3f(
                    (float) (center.x + offset.x), (float) (center.y + offset.y), (float) (center.z + offset.z));
        }
        return ring;
    }

    private static void calcPoints(Entity thrower, EntityGrapple grapple, float partialTicks, List<Vec3> points) {
        if (thrower == null) {
            return;
        }
        double tx = Mth.lerp(partialTicks, thrower.xOld, thrower.getX());
        double ty = Mth.lerp(partialTicks, thrower.yOld, thrower.getY());
        double tz = Mth.lerp(partialTicks, thrower.zOld, thrower.getZ());
        Minecraft minecraft = Minecraft.getInstance();
        if (thrower == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            double yaw = Mth.rotLerp(partialTicks, thrower.yRotO, thrower.getYRot());
            int handSign = grapple.getHand() == InteractionHand.MAIN_HAND ? 1 : -1;
            double px = -Mth.cos((float) ((yaw - 0.5) / 180.0 * Math.PI)) * 0.1F * handSign;
            double pz = -Mth.sin((float) ((yaw - 0.5) / 180.0 * Math.PI)) * 0.1F * handSign;
            Vec3 look = thrower.getLookAngle();
            tx += px + look.x / 5.0;
            ty += thrower.getEyeHeight() / 2.6 + look.y / 5.0;
            tz += pz + look.z / 5.0;
        }
        double gx = Mth.lerp(partialTicks, grapple.xOld, grapple.getX());
        double gy = Mth.lerp(partialTicks, grapple.yOld, grapple.getY());
        double gz = Mth.lerp(partialTicks, grapple.zOld, grapple.getZ());
        Vec3 end = new Vec3(tx - gx, ty - gy + thrower.getBbHeight() / 2.0F, tz - gz);
        float length = (float) (end.length() * 5.0);
        int steps = (int) length;
        points.add(Vec3.ZERO);
        for (int step = 1; step < steps - 1; step++) {
            float dist = step * (length / steps);
            float damp = 1.0F - step / (steps * 1.25F);
            double dx = (tx - gx) / steps * step + Mth.sin(dist / 10.0F) * grapple.ampl * damp;
            double dy = (ty - gy + thrower.getBbHeight() / 2.0F) / steps * step
                    + Mth.sin(dist / 8.0F) * grapple.ampl * damp;
            double dz = (tz - gz) / steps * step + Mth.sin(dist / 6.0F) * grapple.ampl * damp;
            points.add(new Vec3(dx, dy, dz));
        }
        points.add(end);
    }
}
