package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public final class FluxRiftRenderer extends EntityRenderer<EntityFluxRift, FluxRiftRenderState> {
    private static final RenderType RIFT_GLOW_TYPE = RenderType.create("tc_rift_glow", RenderSetup.builder(TCRenderPipelines.RIFT_GLOW)
            .withTexture("Sampler0", AbstractEndPortalRenderer.END_PORTAL_LOCATION).withTexture("Sampler1", AbstractEndPortalRenderer.END_PORTAL_LOCATION).createRenderSetup());

    private static final RenderType RIFT_GLOW_NO_DEPTH_TYPE = RenderType.create("tc_rift_glow_no_depth", RenderSetup.builder(TCRenderPipelines.RIFT_GLOW_NO_DEPTH)
            .withTexture("Sampler0", AbstractEndPortalRenderer.END_PORTAL_LOCATION).withTexture("Sampler1", AbstractEndPortalRenderer.END_PORTAL_LOCATION).createRenderSetup());

    private static final RenderType RIFT_SOLID_TYPE = RenderType.create("tc_rift_solid", RenderSetup.builder(TCRenderPipelines.RIFT_SOLID)
            .withTexture("Sampler0", AbstractEndPortalRenderer.END_PORTAL_LOCATION).withTexture("Sampler1", AbstractEndPortalRenderer.END_PORTAL_LOCATION).createRenderSetup());

    private static final int TUBE_SIDES = 6;
    private static final int GLOW_PASSES = 3;
    private static final float GLOW_RADIUS_BASE = 1.25F;
    private static final float GLOW_RADIUS_STEP = 0.5F;
    private static final float WOBBLE_AMPLITUDE = 0.1F;
    private static final float MAX_STAB_FACTOR = 1.5F;
    private static final float STAB_DIVISOR = 50.0F;
    private static final float TIME_OFFSET_PER_POINT = 10.0F;
    private static final float WOBBLE_X_PERIOD = 50.0F;
    private static final float WOBBLE_Y_PERIOD = 60.0F;
    private static final float WOBBLE_Z_PERIOD = 70.0F;
    private static final float WIDTH_PULSE_PERIOD = 8.0F;

    public FluxRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public FluxRiftRenderState createRenderState() {
        return new FluxRiftRenderState();
    }

    @Override
    public void extractRenderState(EntityFluxRift entity, FluxRiftRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.points.clear();
        state.points.addAll(entity.points);
        state.widths.clear();
        state.widths.addAll(entity.pointsWidth);
        state.stability = entity.getRiftStability();
        state.animationTime = entity.tickCount + partialTicks;
        state.goggles = Minecraft.getInstance().player != null && GogglesAccess.wearsGoggles(Minecraft.getInstance().player);
    }

    @Override
    public void submit(FluxRiftRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        int count = state.points.size();
        if (count <= 2) {
            return;
        }
        float stab = Mth.clamp(1.0F - state.stability / STAB_DIVISOR, 0.0F, MAX_STAB_FACTOR);
        Vec3[] centers = new Vec3[count];
        float[] radii = new float[count];
        for (int a = 0; a < count; a++) {
            float time = state.animationTime;
            if (a > count / 2) {
                time -= a * TIME_OFFSET_PER_POINT;
            } else if (a < count / 2) {
                time += a * TIME_OFFSET_PER_POINT;
            }
            Vec3 p = state.points.get(a);
            centers[a] = new Vec3(p.x + Math.sin(time / WOBBLE_X_PERIOD) * WOBBLE_AMPLITUDE * stab, p.y + Math.sin(time / WOBBLE_Y_PERIOD) * WOBBLE_AMPLITUDE * stab,
                    p.z + Math.sin(time / WOBBLE_Z_PERIOD) * WOBBLE_AMPLITUDE * stab);
            double pulse = 1.0 - Math.sin(time / WIDTH_PULSE_PERIOD) * 0.1F * stab;
            radii[a] = (float) (state.widths.get(a) * pulse);
        }
        for (int pass = 0; pass <= GLOW_PASSES; pass++) {
            RenderType type;
            float radiusScale;
            if (pass < GLOW_PASSES) {
                type = pass == 0 && state.goggles ? RIFT_GLOW_NO_DEPTH_TYPE : RIFT_GLOW_TYPE;
                radiusScale = GLOW_RADIUS_BASE + GLOW_RADIUS_STEP * pass;
            } else {
                type = RIFT_SOLID_TYPE;
                radiusScale = 1.0F;
            }
            submitTube(collector, poseStack, type, centers, radii, radiusScale);
        }
    }

    private static void submitTube(SubmitNodeCollector collector, PoseStack poseStack, RenderType type, Vec3[] centers, float[] radii, float radiusScale) {
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            Vector3f[] previousRing = null;
            for (int a = 0; a < centers.length; a++) {
                Vec3 direction = segmentDirection(centers, a);
                Vector3f[] ring = buildRing(centers[a], direction, radii[a] * radiusScale);
                if (previousRing != null) {
                    for (int side = 0; side < TUBE_SIDES; side++) {
                        int next = (side + 1) % TUBE_SIDES;
                        addVertex(buffer, pose.pose(), previousRing[side]);
                        addVertex(buffer, pose.pose(), previousRing[next]);
                        addVertex(buffer, pose.pose(), ring[next]);
                        addVertex(buffer, pose.pose(), ring[side]);
                    }
                }
                previousRing = ring;
            }
        });
    }

    private static void addVertex(VertexConsumer buffer, Matrix4fc pose, Vector3f vertex) {
        buffer.addVertex(pose, vertex.x, vertex.y, vertex.z);
    }

    private static Vec3 segmentDirection(Vec3[] centers, int index) {
        Vec3 direction;
        if (index == 0) {
            direction = centers[1].subtract(centers[0]);
        } else if (index == centers.length - 1) {
            direction = centers[index].subtract(centers[index - 1]);
        } else {
            direction = centers[index + 1].subtract(centers[index - 1]);
        }
        return direction.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 1.0, 0.0) : direction.normalize();
    }

    private static Vector3f[] buildRing(Vec3 center, Vec3 direction, float radius) {
        Vec3 reference = Math.abs(direction.y) < 0.99 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 u = direction.cross(reference).normalize();
        Vec3 v = direction.cross(u).normalize();
        Vector3f[] ring = new Vector3f[TUBE_SIDES];
        for (int side = 0; side < TUBE_SIDES; side++) {
            double angle = (Math.PI * 2.0 * side) / TUBE_SIDES;
            double cos = Math.cos(angle) * radius;
            double sin = Math.sin(angle) * radius;
            ring[side] = new Vector3f((float) (center.x + u.x * cos + v.x * sin), (float) (center.y + u.y * cos + v.y * sin), (float) (center.z + u.z * cos + v.z * sin));
        }
        return ring;
    }
}
