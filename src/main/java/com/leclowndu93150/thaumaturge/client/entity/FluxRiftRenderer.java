package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public final class FluxRiftRenderer extends EntityRenderer<EntityFluxRift, FluxRiftRenderState> {
    private static final float SHADOW_RADIUS = 0.0F;
    private static final int MIN_POINTS = 3;
    private static final int SIDES = 6;
    private static final float STABLE_LIMIT = 50.0F;
    private static final float MAX_INSTABILITY = 1.5F;
    private static final float PHASE_PER_POINT = 10.0F;
    private static final float WOBBLE_AMPLITUDE = 0.1F;
    private static final float WOBBLE_X_DIVISOR = 50.0F;
    private static final float WOBBLE_Y_DIVISOR = 60.0F;
    private static final float WOBBLE_Z_DIVISOR = 70.0F;
    private static final float PULSE_DIVISOR = 8.0F;
    private static final float CORE_SCALE = 1.0F;
    private static final float INNER_SHELL_SCALE = 1.25F;
    private static final float MIDDLE_SHELL_SCALE = 1.75F;
    private static final float OUTER_SHELL_SCALE = 2.25F;
    private static final float MIN_DIRECTION_LENGTH = 0.001F;
    private static final float VERTICAL_THRESHOLD = 0.9F;

    public FluxRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public FluxRiftRenderState createRenderState() {
        return new FluxRiftRenderState();
    }

    @Override
    public void extractRenderState(EntityFluxRift entity, FluxRiftRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.points.clear();
        state.points.addAll(entity.outline);
        state.widths.clear();
        state.widths.addAll(entity.outlineWidths);
        state.stability = entity.stabilityValue();
        state.animationTime = state.ageInTicks;
        state.goggles = GogglesAccess.wearsRevealingGear(Minecraft.getInstance().player);
    }

    @Override
    public void submit(FluxRiftRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (ringCount(state) < MIN_POINTS) {
            return;
        }
        Tube tube = buildTube(state);
        RenderType innerShell = state.goggles ? TTFXRenderTypes.RIFT_GLOW_NO_DEPTH : TTFXRenderTypes.RIFT_GLOW;
        submitTube(collector, poseStack, TTFXRenderTypes.RIFT_GLOW, tube, OUTER_SHELL_SCALE);
        submitTube(collector, poseStack, TTFXRenderTypes.RIFT_GLOW, tube, MIDDLE_SHELL_SCALE);
        submitTube(collector, poseStack, innerShell, tube, INNER_SHELL_SCALE);
        submitTube(collector, poseStack, TTFXRenderTypes.RIFT_SOLID, tube, CORE_SCALE);
    }

    private static void submitTube(SubmitNodeCollector collector, PoseStack poseStack, RenderType type, Tube tube, float scale) {
        LateWorldRenderQueue.submitOwnShader(poseStack, collector, type, (pose, buffer) -> tube.write(buffer, pose.pose(), scale));
    }

    private static int ringCount(FluxRiftRenderState state) {
        return Math.min(state.points.size(), state.widths.size());
    }

    private static Tube buildTube(FluxRiftRenderState state) {
        int count = ringCount(state);
        float instability = Mth.clamp(1.0F - state.stability / STABLE_LIMIT, 0.0F, MAX_INSTABILITY);
        float middle = (count - 1) / 2.0F;
        float[] centers = new float[count * 3];
        float[] radii = new float[count];
        for (int i = 0; i < count; i++) {
            Vec3 point = state.points.get(i);
            float time = state.animationTime + (i - middle) * PHASE_PER_POINT;
            float amplitude = WOBBLE_AMPLITUDE * instability;
            centers[i * 3] = (float) point.x + Mth.sin(time / WOBBLE_X_DIVISOR) * amplitude;
            centers[i * 3 + 1] = (float) point.y + Mth.sin(time / WOBBLE_Y_DIVISOR) * amplitude;
            centers[i * 3 + 2] = (float) point.z + Mth.sin(time / WOBBLE_Z_DIVISOR) * amplitude;
            radii[i] = state.widths.get(i) * (1.0F - Mth.sin(time / PULSE_DIVISOR) * amplitude);
        }
        float[] offsets = new float[count * SIDES * 3];
        Vector3f direction = new Vector3f();
        Vector3f reference = new Vector3f();
        Vector3f across = new Vector3f();
        Vector3f along = new Vector3f();
        for (int i = 0; i < count; i++) {
            int previous = Math.max(i - 1, 0);
            int next = Math.min(i + 1, count - 1);
            direction.set(centers[next * 3] - centers[previous * 3], centers[next * 3 + 1] - centers[previous * 3 + 1], centers[next * 3 + 2] - centers[previous * 3 + 2]);
            if (direction.length() < MIN_DIRECTION_LENGTH) {
                direction.set(0.0F, 1.0F, 0.0F);
            }
            direction.normalize();
            reference.set(Math.abs(direction.y) > VERTICAL_THRESHOLD ? 1.0F : 0.0F, Math.abs(direction.y) > VERTICAL_THRESHOLD ? 0.0F : 1.0F, 0.0F);
            reference.cross(direction, across).normalize();
            direction.cross(across, along);
            for (int side = 0; side < SIDES; side++) {
                float angle = Mth.TWO_PI * side / SIDES;
                float cos = Mth.cos(angle);
                float sin = Mth.sin(angle);
                int base = (i * SIDES + side) * 3;
                offsets[base] = across.x * cos + along.x * sin;
                offsets[base + 1] = across.y * cos + along.y * sin;
                offsets[base + 2] = across.z * cos + along.z * sin;
            }
        }
        return new Tube(count, centers, radii, offsets);
    }

    private record Tube(int count, float[] centers, float[] radii, float[] offsets) {
        void write(VertexConsumer buffer, Matrix4fc matrix, float scale) {
            for (int i = 0; i < count - 1; i++) {
                for (int side = 0; side < SIDES; side++) {
                    int nextSide = (side + 1) % SIDES;
                    vertex(buffer, matrix, i, side, scale);
                    vertex(buffer, matrix, i, nextSide, scale);
                    vertex(buffer, matrix, i + 1, nextSide, scale);
                    vertex(buffer, matrix, i + 1, side, scale);
                }
            }
        }

        private void vertex(VertexConsumer buffer, Matrix4fc matrix, int ring, int side, float scale) {
            int offset = (ring * SIDES + side) * 3;
            float radius = radii[ring] * scale;
            buffer.addVertex(matrix, centers[ring * 3] + offsets[offset] * radius, centers[ring * 3 + 1] + offsets[offset + 1] * radius, centers[ring * 3 + 2] + offsets[offset + 2] * radius);
        }
    }
}
