package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.client.effect.geometry.TubeMesh;
import com.leclowndu93150.thaumaturge.client.effect.instance.ArcInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BeamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoltInstance;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.ArcRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BeamRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BoltRenderType;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public final class BeamManager extends AbstractFXManager<IFXInstance> {
    public static final BeamManager INSTANCE = new BeamManager();

    private static final List<ArcInstance> ARCS = new ArrayList<>();
    private static final List<BoltInstance> BOLTS = new ArrayList<>();
    private static final List<BeamInstance> BEAMS = new ArrayList<>();
    private static final float ARC_SIZE = 0.125F;
    private static final float ARC_CROSS_HALF_WIDTH = ARC_SIZE * Mth.SQRT_OF_TWO;
    private static final float SOURCE_QUAD_SIZE = 0.33F;
    private static final float HIT_QUAD_SIZE = 0.3F;
    private static final double MIN_BEAM_LENGTH = 1.0E-3;
    private static final int CHANNEL_SEGMENTS = 12;

    private BeamManager() {}

    public static void addArc(ArcInstance instance) {
        if (!instance.isExpired()) {
            ARCS.add(instance);
        }
    }

    public static void addBolt(BoltInstance instance) {
        if (!instance.isExpired()) {
            BOLTS.add(instance);
        }
    }

    public static void addBeam(BeamInstance instance) {
        if (instance.isExpired()) {
            return;
        }
        for (Iterator<BeamInstance> it = BEAMS.iterator(); it.hasNext(); ) {
            BeamInstance existing = it.next();
            if (existing.sameBeamAs(instance)) {
                if (existing.isStopped()) {
                    it.remove();
                    break;
                }
                existing.refresh(instance);
                return;
            }
        }
        BEAMS.add(instance);
    }

    public static void stopChannels(int casterId, @Nullable InteractionHand hand) {
        for (BeamInstance beam : BEAMS) {
            if (beam.isChannelFrom(casterId, hand)) beam.stopChannel();
        }
    }

    @Override
    protected Collection<IFXInstance> activeInstances() {
        throw new UnsupportedOperationException("BeamManager overrides tickAll directly");
    }

    @Override
    public void tickAll(ClientLevel level) {
        tickList(ARCS);
        tickList(BOLTS);
        tickList(BEAMS);
    }

    private static <I extends IFXInstance> void tickList(List<I> list) {
        Iterator<I> it = list.iterator();
        while (it.hasNext()) {
            I inst = it.next();
            inst.tick();
            if (inst.isExpired()) it.remove();
        }
    }

    @Override
    public void renderAll(PoseStack poseStack, Camera camera, float partialTick) {
        if (ARCS.isEmpty() && BOLTS.isEmpty() && BEAMS.isEmpty()) return;
        Vec3 camPos = camera.getPosition();

        if (!ARCS.isEmpty()) {
            MultiBufferSource.BufferSource bufSource =
                    Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer consumer = IrisCompat.entityEffectBuffers(bufSource).getBuffer(ArcRenderType.RENDER_TYPE);
            for (ArcInstance arc : ARCS) renderArc(poseStack, consumer, arc, camPos, partialTick);
            bufSource.endBatch();
        }

        if (!BOLTS.isEmpty()) {
            MultiBufferSource.BufferSource bufSource =
                    Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer consumer = IrisCompat.entityEffectBuffers(bufSource).getBuffer(BoltRenderType.RENDER_TYPE);
            for (BoltInstance bolt : BOLTS) renderBolt(poseStack, consumer, bolt, camPos, partialTick);
            bufSource.endBatch();
        }

        if (!BEAMS.isEmpty()) {
            MultiBufferSource.BufferSource bufSource =
                    Minecraft.getInstance().renderBuffers().bufferSource();
            MultiBufferSource effectBuffers = IrisCompat.entityEffectBuffers(bufSource);
            for (BeamInstance beam : BEAMS) renderBeam(poseStack, effectBuffers, beam, camPos, partialTick);
            bufSource.endBatch();
        }
    }

    private static void renderBeam(
            PoseStack poseStack, MultiBufferSource bufSource, BeamInstance beam, Vec3 camPos, float partialTick) {
        Vec3 wandTip = beam.wandTip();
        Vec3 source = wandTip == null ? beam.sourcePos(partialTick) : wandTip;
        Vec3 target = beam.targetPos(partialTick);
        float size = beam.computeSize(partialTick);
        float op = beam.computeOpacity(partialTick);
        float length = (float) source.distanceTo(target);
        if (shouldRenderBeam(length, size, op)) {
            VertexConsumer trunk = bufSource.getBuffer(BeamRenderType.trunkForType(beam.beamType()));
            float texScroll = beam.texScroll(partialTick);
            float rot = beam.worldRotation(partialTick);
            float yaw = beam.yawAt(partialTick);
            float pitch = beam.pitchAt(partialTick);
            if (wandTip != null) {
                Vec3 span = source.subtract(target);
                yaw = (float) Math.toDegrees(Math.atan2(span.x, span.z));
                pitch = (float) Math.toDegrees(Math.atan2(span.y, span.horizontalDistance()));
            }
            int color = ARGB32.colorFromFloat(op, beam.colorR(), beam.colorG(), beam.colorB());

            if (beam.isChannel()) {
                renderChannel(poseStack, trunk, beam, source, target, camPos, size, texScroll, rot, color, partialTick);
            } else {
                poseStack.pushPose();
                poseStack.translate(source.x - camPos.x, source.y - camPos.y, source.z - camPos.z);
                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(90.0)));
                poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(-(180.0F + yaw))));
                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(pitch)));
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(rot)));
                Matrix4f mat = poseStack.last().pose();

                double base = 0.15 * size;
                double baseEnd = base * beam.endMod();

                for (int t = 0; t < 3; t++) {
                    poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(60.0)));
                    Matrix4f stripMat = poseStack.last().pose();
                    float u0 = 0.0F;
                    float u1 = 1.0F;
                    float v0 = -1.0F + texScroll + t / 3.0F;
                    float v1 = length * size + v0;
                    beamVertex(trunk, stripMat, -baseEnd, length * size, 0.0, u1, v1, color);
                    beamVertex(trunk, stripMat, -base, 0.0, 0.0, u1, v0, color);
                    beamVertex(trunk, stripMat, base, 0.0, 0.0, u0, v0, color);
                    beamVertex(trunk, stripMat, baseEnd, length * size, 0.0, u0, v1, color);
                }
                poseStack.popPose();
            }
        }

        if (beam.withSource()) {
            VertexConsumer node = bufSource.getBuffer(BeamRenderType.NODE_TYPE);
            float sourceOp = 2.0F * beam.computeOpacity(partialTick);
            int sourceColor = ARGB32.colorFromFloat(sourceOp, beam.colorR(), beam.colorG(), beam.colorB());
            drawNodeQuad(poseStack, node, camPos, source, beam.age(), SOURCE_QUAD_SIZE, sourceColor);
        }
        if (beam.hit()) {
            VertexConsumer node = bufSource.getBuffer(BeamRenderType.NODE_TYPE);
            int flareColor = ARGB32.colorFromFloat(1.0F, beam.colorR(), beam.colorG(), beam.colorB());
            float halfSize = HIT_QUAD_SIZE * beam.hitFactor(partialTick);
            drawNodeQuad(poseStack, node, camPos, target, beam.age(), halfSize, flareColor);
        }
    }

    private static void renderChannel(
            PoseStack poseStack,
            VertexConsumer trunk,
            BeamInstance beam,
            Vec3 source,
            Vec3 target,
            Vec3 camPos,
            float size,
            float scroll,
            float rotation,
            int color,
            float partialTick) {
        Vec3 span = target.subtract(source);
        double length = span.length();
        Vec3 axis = span.normalize();
        Vec3 side = beam.ribbonSide(axis);
        Vec3 around = axis.cross(side).normalize();
        Vec3 forward = beam.channelForward(partialTick);
        poseStack.pushPose();
        poseStack.translate(source.x - camPos.x, source.y - camPos.y, source.z - camPos.z);
        Matrix4f matrix = poseStack.last().pose();
        for (int strip = 0; strip < 3; strip++) {
            double angle = Math.toRadians(rotation + 60.0 * (strip + 1));
            Vec3 ribbon = side.scale(Math.cos(angle)).add(around.scale(Math.sin(angle)));
            float offset = -1.0F + scroll + strip / 3.0F;
            for (int segment = 0; segment < CHANNEL_SEGMENTS; segment++) {
                float t0 = size * segment / CHANNEL_SEGMENTS;
                float t1 = size * (segment + 1) / CHANNEL_SEGMENTS;
                Vec3 near = channelPoint(Vec3.ZERO, span, forward, t0);
                Vec3 far = channelPoint(Vec3.ZERO, span, forward, t1);
                Vec3 nearWidth = ribbon.scale(0.15 * size * Mth.lerp(t0, 1.0F, beam.endMod()));
                Vec3 farWidth = ribbon.scale(0.15 * size * Mth.lerp(t1, 1.0F, beam.endMod()));
                float v0 = offset + (float) length * t0;
                float v1 = offset + (float) length * t1;
                channelVertex(trunk, matrix, far.subtract(farWidth), 1.0F, v1, color);
                channelVertex(trunk, matrix, near.subtract(nearWidth), 1.0F, v0, color);
                channelVertex(trunk, matrix, near.add(nearWidth), 0.0F, v0, color);
                channelVertex(trunk, matrix, far.add(farWidth), 0.0F, v1, color);
            }
        }
        poseStack.popPose();
    }

    static Vec3 channelPoint(Vec3 source, Vec3 target, Vec3 forward, float t) {
        Vec3 span = target.subtract(source);
        return source.add(span.scale(t * t)).add(forward.normalize().scale(span.length() * t * (1.0F - t)));
    }

    private static void channelVertex(
            VertexConsumer consumer, Matrix4f matrix, Vec3 point, float u, float v, int color) {
        beamVertex(consumer, matrix, point.x, point.y, point.z, u, v, color);
    }

    private static void drawNodeQuad(
            PoseStack poseStack, VertexConsumer node, Vec3 camPos, Vec3 at, int age, float halfSize, int color) {
        float u0 = (age % 32) / 32.0F;
        float u1 = u0 + 0.03125F;
        float v0 = 0.09375F;
        float v1 = v0 + 0.03125F;
        poseStack.pushPose();
        poseStack.translate(at.x - camPos.x, at.y - camPos.y, at.z - camPos.z);
        Quaternionf cameraRot = new Quaternionf();
        Minecraft.getInstance().gameRenderer.getMainCamera().rotation().normalize(cameraRot);
        poseStack.mulPose(cameraRot);
        Matrix4f matrix = poseStack.last().pose();
        node.addVertex(matrix, -halfSize, -halfSize, 0.0F).setColor(color).setUv(u1, v1);
        node.addVertex(matrix, -halfSize, halfSize, 0.0F).setColor(color).setUv(u1, v0);
        node.addVertex(matrix, halfSize, halfSize, 0.0F).setColor(color).setUv(u0, v0);
        node.addVertex(matrix, halfSize, -halfSize, 0.0F).setColor(color).setUv(u0, v1);
        poseStack.popPose();
    }

    private static void beamVertex(
            VertexConsumer consumer, Matrix4f mat, double x, double y, double z, float u, float v, int color) {
        consumer.addVertex(mat, (float) x, (float) y, (float) z).setColor(color).setUv(u, v);
    }

    private static void renderArc(
            PoseStack poseStack, VertexConsumer consumer, ArcInstance arc, Vec3 camPos, float partialTick) {
        float alpha = arc.alpha(partialTick);
        if (!shouldRenderArc(arc.length, alpha)) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(arc.startX - camPos.x, arc.startY - camPos.y, arc.startZ - camPos.z);
        Matrix4f mat = poseStack.last().pose();
        int color = ARGB32.colorFromFloat(alpha, arc.colorR, arc.colorG, arc.colorB);
        int n = arc.points.size();
        if (n < 2) {
            poseStack.popPose();
            return;
        }
        Vec3 side = arcHorizontalSide(arc.points.get(n - 1));
        for (int c = 0; c < n - 1; c++) {
            Vec3 v0 = arc.points.get(c);
            Vec3 v1 = arc.points.get(c + 1);
            float u0 = arcTextureU(c, arc.length);
            float u1 = arcTextureU(c + 1, arc.length);
            emitArcQuadY(consumer, mat, v0, v1, u0, u1, color, ARC_SIZE);
            emitArcQuadXZ(consumer, mat, v0, v1, u0, u1, color, side, ARC_CROSS_HALF_WIDTH);
        }
        poseStack.popPose();
    }

    static Vec3 arcHorizontalSide(Vec3 endpoint) {
        double sideX = -endpoint.z;
        double sideZ = endpoint.x;
        double flat = Math.sqrt(sideX * sideX + sideZ * sideZ);
        if (flat < MIN_BEAM_LENGTH) {
            return new Vec3(1.0, 0.0, 0.0);
        }
        return new Vec3(sideX / flat, 0.0, sideZ / flat);
    }

    static float arcTextureU(int pointIndex, float chordLength) {
        return chordLength > 0.0F ? pointIndex / chordLength : 0.0F;
    }

    static boolean shouldRenderArc(double length, float alpha) {
        return length >= MIN_BEAM_LENGTH && alpha > 0.0F;
    }

    static boolean shouldRenderBeam(double length, float size, float opacity) {
        return length >= MIN_BEAM_LENGTH && size > 0.0F && opacity > 0.0F;
    }

    private static void emitArcQuadY(
            VertexConsumer consumer, Matrix4f mat, Vec3 v0, Vec3 v1, float u0, float u1, int color, float size) {
        vertex(consumer, mat, v0.x, v0.y - size, v0.z, u0, 1.0F, color);
        vertex(consumer, mat, v0.x, v0.y + size, v0.z, u0, 0.0F, color);
        vertex(consumer, mat, v1.x, v1.y + size, v1.z, u1, 0.0F, color);
        vertex(consumer, mat, v1.x, v1.y - size, v1.z, u1, 1.0F, color);
    }

    private static void emitArcQuadXZ(
            VertexConsumer consumer,
            Matrix4f mat,
            Vec3 v0,
            Vec3 v1,
            float u0,
            float u1,
            int color,
            Vec3 side,
            float halfWidth) {
        double sx = side.x * halfWidth;
        double sz = side.z * halfWidth;
        vertex(consumer, mat, v0.x - sx, v0.y, v0.z - sz, u0, 1.0F, color);
        vertex(consumer, mat, v0.x + sx, v0.y, v0.z + sz, u0, 0.0F, color);
        vertex(consumer, mat, v1.x + sx, v1.y, v1.z + sz, u1, 0.0F, color);
        vertex(consumer, mat, v1.x - sx, v1.y, v1.z - sz, u1, 1.0F, color);
    }

    private static void vertex(
            VertexConsumer consumer, Matrix4f mat, double x, double y, double z, float u, float v, int color) {
        consumer.addVertex(mat, (float) x, (float) y, (float) z).setColor(color).setUv(u, v);
    }

    private static void renderBolt(
            PoseStack poseStack, VertexConsumer consumer, BoltInstance bolt, Vec3 camPos, float partialTick) {
        Vec3 wandTip = bolt.wandTip();
        Vec3 source = wandTip == null ? bolt.source() : wandTip;
        List<BoltInstance.PathStep> path = bolt.computePath(partialTick, wandTip);
        if (path.size() < 3) return;
        poseStack.pushPose();
        poseStack.translate(source.x - camPos.x, source.y - camPos.y, source.z - camPos.z);
        float alpha = bolt.alpha(partialTick);
        if (alpha <= 0.0F) {
            poseStack.popPose();
            return;
        }
        int n = path.size();
        double[][] points = new double[n][3];
        float[][] colours = new float[n][4];
        double[] radii = new double[n];
        for (int i = 0; i < n; i++) {
            BoltInstance.PathStep s = path.get(i);
            points[i][0] = s.x();
            points[i][1] = s.y();
            points[i][2] = s.z();
            colours[i][0] = bolt.colorR;
            colours[i][1] = bolt.colorG;
            colours[i][2] = bolt.colorB;
            colours[i][3] = alpha;
            radii[i] = s.width() / 10.0;
        }
        TubeMesh.draw(poseStack, consumer, points, colours, radii, 0, 1.0F, 0.0F);
        for (int i = 0; i < n; i++) radii[i] /= 3.0;
        TubeMesh.draw(poseStack, consumer, points, colours, radii, 0, 1.0F, 0.0F);
        poseStack.popPose();
    }

    @Override
    public void clear() {
        ARCS.clear();
        BOLTS.clear();
        BEAMS.clear();
    }
}
