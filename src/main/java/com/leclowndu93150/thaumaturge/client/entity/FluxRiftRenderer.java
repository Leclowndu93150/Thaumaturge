package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.render.TTShaders;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class FluxRiftRenderer extends EntityRenderer<EntityFluxRift> {
    private static final ResourceLocation END_PORTAL = TheEndPortalRenderer.END_PORTAL_LOCATION;

    private static final RenderType RIFT_GLOW_TYPE = riftType(
            "tt_rift_glow",
            RenderStateShard.ADDITIVE_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE);
    private static final RenderType RIFT_GLOW_NO_DEPTH_TYPE = riftType(
            "tt_rift_glow_no_depth",
            RenderStateShard.ADDITIVE_TRANSPARENCY,
            RenderStateShard.NO_DEPTH_TEST,
            RenderStateShard.COLOR_WRITE);
    private static final RenderType RIFT_SOLID_TYPE = riftType(
            "tt_rift_solid",
            RenderStateShard.TRANSLUCENT_TRANSPARENCY,
            RenderStateShard.LEQUAL_DEPTH_TEST,
            RenderStateShard.COLOR_DEPTH_WRITE);

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

    private static RenderType riftType(
            String name,
            RenderStateShard.TransparencyStateShard transparency,
            RenderStateShard.DepthTestStateShard depthTest,
            RenderStateShard.WriteMaskStateShard writeMask) {
        return RenderType.create(
                name,
                DefaultVertexFormat.POSITION,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(TTShaders::ender))
                        .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                                .add(END_PORTAL, false, false)
                                .add(END_PORTAL, false, false)
                                .build())
                        .setTransparencyState(transparency)
                        .setDepthTestState(depthTest)
                        .setWriteMaskState(writeMask)
                        .setCullState(RenderStateShard.NO_CULL)
                        .createCompositeState(false));
    }

    @Override
    public void render(
            EntityFluxRift entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        int count = entity.points.size();
        if (count <= 2) {
            return;
        }
        float animationTime = entity.tickCount + partialTicks;
        boolean goggles =
                Minecraft.getInstance().player != null && GogglesAccess.wearsGoggles(Minecraft.getInstance().player);
        float stab = Mth.clamp(1.0F - entity.getRiftStability() / STAB_DIVISOR, 0.0F, MAX_STAB_FACTOR);
        Vec3[] centers = new Vec3[count];
        float[] radii = new float[count];
        for (int a = 0; a < count; a++) {
            float time = animationTime;
            if (a > count / 2) {
                time -= a * TIME_OFFSET_PER_POINT;
            } else if (a < count / 2) {
                time += a * TIME_OFFSET_PER_POINT;
            }
            Vec3 p = entity.points.get(a);
            centers[a] = new Vec3(
                    p.x + Math.sin(time / WOBBLE_X_PERIOD) * WOBBLE_AMPLITUDE * stab,
                    p.y + Math.sin(time / WOBBLE_Y_PERIOD) * WOBBLE_AMPLITUDE * stab,
                    p.z + Math.sin(time / WOBBLE_Z_PERIOD) * WOBBLE_AMPLITUDE * stab);
            double pulse = 1.0 - Math.sin(time / WIDTH_PULSE_PERIOD) * 0.1F * stab;
            radii[a] = (float) (entity.pointsWidth.get(a) * pulse);
        }
        for (int pass = 0; pass <= GLOW_PASSES; pass++) {
            RenderType type;
            float radiusScale;
            if (pass < GLOW_PASSES) {
                type = pass == 0 && goggles ? RIFT_GLOW_NO_DEPTH_TYPE : RIFT_GLOW_TYPE;
                radiusScale = GLOW_RADIUS_BASE + GLOW_RADIUS_STEP * pass;
            } else {
                type = RIFT_SOLID_TYPE;
                radiusScale = 1.0F;
            }
            renderTube(buffers.getBuffer(type), poseStack.last().pose(), centers, radii, radiusScale);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFluxRift entity) {
        return END_PORTAL;
    }

    private static void renderTube(
            VertexConsumer buffer, Matrix4f pose, Vec3[] centers, float[] radii, float radiusScale) {
        Vector3f[] previousRing = null;
        for (int a = 0; a < centers.length; a++) {
            Vec3 direction = segmentDirection(centers, a);
            Vector3f[] ring = buildRing(centers[a], direction, radii[a] * radiusScale);
            if (previousRing != null) {
                for (int side = 0; side < TUBE_SIDES; side++) {
                    int next = (side + 1) % TUBE_SIDES;
                    addVertex(buffer, pose, previousRing[side]);
                    addVertex(buffer, pose, previousRing[next]);
                    addVertex(buffer, pose, ring[next]);
                    addVertex(buffer, pose, ring[side]);
                }
            }
            previousRing = ring;
        }
    }

    private static void addVertex(VertexConsumer buffer, Matrix4f pose, Vector3f vertex) {
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
            ring[side] = new Vector3f(
                    (float) (center.x + u.x * cos + v.x * sin), (float) (center.y + u.y * cos + v.y * sin), (float)
                            (center.z + u.z * cos + v.z * sin));
        }
        return ring;
    }
}
