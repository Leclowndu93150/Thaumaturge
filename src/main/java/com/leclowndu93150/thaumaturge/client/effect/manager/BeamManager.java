package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.client.effect.geometry.PolyCone;
import com.leclowndu93150.thaumaturge.client.effect.instance.ArcInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BeamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoltInstance;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.ArcRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BeamRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.BoltRenderType;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class BeamManager extends AbstractFXManager<IFXInstance> {
    public static final BeamManager INSTANCE = new BeamManager();

    private static final List<ArcInstance> ARCS = new ArrayList<>();
    private static final List<BoltInstance> BOLTS = new ArrayList<>();
    private static final List<BeamInstance> BEAMS = new ArrayList<>();
    private static final float ARC_SIZE = 0.125F;
    private static final float SOURCE_QUAD_SIZE = 0.33F;

    private BeamManager() {}

    public static void addArc(ArcInstance instance) {
        ARCS.add(instance);
    }

    public static void addBolt(BoltInstance instance) {
        BOLTS.add(instance);
    }

    public static void addBeam(BeamInstance instance) {
        BEAMS.add(instance);
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
            MultiBufferSource.BufferSource bufSource = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
            VertexConsumer consumer = IrisCompat.entityEffectBuffers(bufSource).getBuffer(ArcRenderType.RENDER_TYPE);
            for (ArcInstance arc : ARCS) renderArc(poseStack, consumer, arc, camPos, partialTick);
            bufSource.endBatch();
        }

        if (!BOLTS.isEmpty()) {
            MultiBufferSource.BufferSource bufSource = MultiBufferSource.immediate(new ByteBufferBuilder(4096));
            VertexConsumer consumer = IrisCompat.entityEffectBuffers(bufSource).getBuffer(BoltRenderType.RENDER_TYPE);
            for (BoltInstance bolt : BOLTS) renderBolt(poseStack, consumer, bolt, camPos, partialTick);
            bufSource.endBatch();
        }

        if (!BEAMS.isEmpty()) {
            MultiBufferSource.BufferSource bufSource = MultiBufferSource.immediate(new ByteBufferBuilder(8192));
            MultiBufferSource effectBuffers = IrisCompat.entityEffectBuffers(bufSource);
            for (BeamInstance beam : BEAMS) renderBeam(poseStack, effectBuffers, beam, camPos, partialTick);
            bufSource.endBatch();
        }
    }

    private static void renderBeam(
            PoseStack poseStack, MultiBufferSource bufSource, BeamInstance beam, Vec3 camPos, float partialTick) {
        VertexConsumer trunk = bufSource.getBuffer(BeamRenderType.trunkForType(beam.beamType()));
        Vec3 source = beam.sourcePos(partialTick);
        Vec3 target = beam.targetPos(partialTick);
        float size = beam.computeSize(partialTick);
        float op = beam.computeOpacity(partialTick);
        float texScroll = beam.texScroll(partialTick);
        float rot = beam.worldRotation(partialTick);
        float yaw = beam.yawAt(partialTick);
        float pitch = beam.pitchAt(partialTick);
        float length = (float) source.distanceTo(target);
        int color = ARGB32.colorFromFloat(op, beam.colorR(), beam.colorG(), beam.colorB());

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

        if (beam.withSource()) {
            VertexConsumer node = bufSource.getBuffer(BeamRenderType.NODE_TYPE);
            int partFrame = beam.age() % 32;
            float u0 = partFrame / 32.0F;
            float u1 = u0 + 0.03125F;
            float v0 = 0.09375F;
            float v1 = v0 + 0.03125F;
            float sourceOp = 0.8F;
            if (beam.maxAge() - beam.age() <= 4) {
                sourceOp = 0.8F - (4 - (beam.maxAge() - beam.age())) * 0.2F;
            }
            int sourceColor = ARGB32.colorFromFloat(sourceOp, beam.colorR(), beam.colorG(), beam.colorB());
            float halfSize = SOURCE_QUAD_SIZE;
            poseStack.pushPose();
            poseStack.translate(source.x - camPos.x, source.y - camPos.y, source.z - camPos.z);
            Quaternionf cameraRot = new Quaternionf();
            Minecraft.getInstance().gameRenderer.getMainCamera().rotation().normalize(cameraRot);
            poseStack.mulPose(cameraRot);
            Matrix4f sm = poseStack.last().pose();
            node.addVertex(sm, -halfSize, -halfSize, 0.0F).setColor(sourceColor).setUv(u1, v1);
            node.addVertex(sm, -halfSize, halfSize, 0.0F).setColor(sourceColor).setUv(u1, v0);
            node.addVertex(sm, halfSize, halfSize, 0.0F).setColor(sourceColor).setUv(u0, v0);
            node.addVertex(sm, halfSize, -halfSize, 0.0F).setColor(sourceColor).setUv(u0, v1);
            poseStack.popPose();
        }
    }

    private static void beamVertex(
            VertexConsumer consumer, Matrix4f mat, double x, double y, double z, float u, float v, int color) {
        consumer.addVertex(mat, (float) x, (float) y, (float) z).setColor(color).setUv(u, v);
    }

    private static void renderArc(
            PoseStack poseStack, VertexConsumer consumer, ArcInstance arc, Vec3 camPos, float partialTick) {
        poseStack.pushPose();
        poseStack.translate(arc.startX - camPos.x, arc.startY - camPos.y, arc.startZ - camPos.z);
        Matrix4f mat = poseStack.last().pose();
        float alpha = arc.alpha(partialTick);
        if (alpha <= 0.0F) {
            poseStack.popPose();
            return;
        }
        int color = ARGB32.colorFromFloat(alpha, arc.colorR, arc.colorG, arc.colorB);
        int n = arc.points.size();
        if (n < 2) {
            poseStack.popPose();
            return;
        }
        for (int c = 0; c < n - 1; c++) {
            Vec3 v0 = arc.points.get(c);
            Vec3 v1 = arc.points.get(c + 1);
            float u0 = c / arc.length;
            float u1 = (c + 1) / arc.length;
            emitArcQuadY(consumer, mat, v0, v1, u0, u1, color, ARC_SIZE);
            emitArcQuadXZ(consumer, mat, v0, v1, u0, u1, color, ARC_SIZE);
        }
        poseStack.popPose();
    }

    private static void emitArcQuadY(
            VertexConsumer consumer, Matrix4f mat, Vec3 v0, Vec3 v1, float u0, float u1, int color, float size) {
        vertex(consumer, mat, v0.x, v0.y - size, v0.z, u0, 1.0F, color);
        vertex(consumer, mat, v0.x, v0.y + size, v0.z, u0, 0.0F, color);
        vertex(consumer, mat, v1.x, v1.y + size, v1.z, u1, 0.0F, color);
        vertex(consumer, mat, v1.x, v1.y - size, v1.z, u1, 1.0F, color);
    }

    private static void emitArcQuadXZ(
            VertexConsumer consumer, Matrix4f mat, Vec3 v0, Vec3 v1, float u0, float u1, int color, float size) {
        vertex(consumer, mat, v0.x - size, v0.y, v0.z - size, u0, 1.0F, color);
        vertex(consumer, mat, v0.x + size, v0.y, v0.z + size, u0, 0.0F, color);
        vertex(consumer, mat, v1.x + size, v1.y, v1.z + size, u1, 0.0F, color);
        vertex(consumer, mat, v1.x - size, v1.y, v1.z - size, u1, 1.0F, color);
    }

    private static void vertex(
            VertexConsumer consumer, Matrix4f mat, double x, double y, double z, float u, float v, int color) {
        consumer.addVertex(mat, (float) x, (float) y, (float) z).setColor(color).setUv(u, v);
    }

    private static void renderBolt(
            PoseStack poseStack, VertexConsumer consumer, BoltInstance bolt, Vec3 camPos, float partialTick) {
        List<BoltInstance.PathStep> path = bolt.computePath(partialTick);
        if (path.size() < 3) return;
        poseStack.pushPose();
        poseStack.translate(bolt.startX - camPos.x, bolt.startY - camPos.y, bolt.startZ - camPos.z);
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
        PolyCone.render(poseStack, consumer, points, colours, radii, 0, 1.0F, 0.0F);
        for (int i = 0; i < n; i++) radii[i] /= 3.0;
        PolyCone.render(poseStack, consumer, points, colours, radii, 0, 1.0F, 0.0F);
        poseStack.popPose();
    }
}
