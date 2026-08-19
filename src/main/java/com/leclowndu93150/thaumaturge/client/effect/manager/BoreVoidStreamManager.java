package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.client.effect.geometry.PolyCone;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoreStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.VoidStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.EssentiaStreamRenderType;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.VoidStreamRenderType;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;

public final class BoreVoidStreamManager extends AbstractFXManager<IFXInstance> {
    public static final BoreVoidStreamManager INSTANCE = new BoreVoidStreamManager();

    private static final List<BoreStreamInstance> BORES = new ArrayList<>();
    private static final List<VoidStreamInstance> VOIDS = new ArrayList<>();

    private BoreVoidStreamManager() {}

    public static void addBore(BoreStreamInstance instance) {
        BORES.add(instance);
    }

    public static void addVoid(VoidStreamInstance instance) {
        VOIDS.add(instance);
    }

    @Override
    protected Collection<IFXInstance> activeInstances() {
        throw new UnsupportedOperationException("BoreVoidStreamManager overrides tickAll directly");
    }

    @Override
    public void tickAll(ClientLevel level) {
        tickList(BORES);
        tickList(VOIDS);
    }

    private static <I extends IFXInstance> void tickList(List<I> list) {
        Iterator<I> it = list.iterator();
        while (it.hasNext()) {
            I inst = it.next();
            inst.tick();
            if (inst.isExpired())
                it.remove();
        }
    }

    @Override
    public void renderAll(PoseStack poseStack, Camera camera, float partialTick) {
        if (BORES.isEmpty() && VOIDS.isEmpty())
            return;
        double cx = camera.position().x;
        double cy = camera.position().y;
        double cz = camera.position().z;

        if (!BORES.isEmpty()) {
            MultiBufferSource.BufferSource buf = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
            VertexConsumer consumer = buf.getBuffer(EssentiaStreamRenderType.RENDER_TYPE);
            for (BoreStreamInstance inst : BORES) {
                StreamInstance.Snapshot snap = inst.snapshot(partialTick);
                if (snap == null)
                    continue;
                poseStack.pushPose();
                poseStack.translate(snap.originX() - cx, snap.originY() - cy, snap.originZ() - cz);
                PolyCone.render(poseStack, consumer, snap.points(), snap.colours(), snap.radii(), 0, snap.texSlice(), snap.start());
                poseStack.popPose();
            }
            buf.endBatch(EssentiaStreamRenderType.RENDER_TYPE);
        }

        if (!VOIDS.isEmpty()) {
            float yawRad = (float) Math.toRadians(camera.yRot());
            float pitchRad = (float) Math.toRadians(camera.xRot());
            float yawNorm = ((yawRad % (float) (2.0 * Math.PI)) + (float) (2.0 * Math.PI)) % (float) (2.0 * Math.PI) / (float) (2.0 * Math.PI);
            float pitchNorm = (pitchRad + (float) (Math.PI * 0.5)) / (float) Math.PI;

            MultiBufferSource.BufferSource bufA = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
            VertexConsumer addConsumer = bufA.getBuffer(VoidStreamRenderType.ADDITIVE);
            for (VoidStreamInstance inst : VOIDS) {
                StreamInstance.Snapshot snap = inst.snapshotWithRadiusMul(partialTick, 1.5F);
                if (snap == null)
                    continue;
                packYawPitch(snap.colours(), yawNorm, pitchNorm);
                poseStack.pushPose();
                poseStack.translate(snap.originX() - cx, snap.originY() - cy, snap.originZ() - cz);
                PolyCone.render(poseStack, addConsumer, snap.points(), snap.colours(), snap.radii(), 0, snap.texSlice(), snap.start());
                poseStack.popPose();
            }
            bufA.endBatch(VoidStreamRenderType.ADDITIVE);

            MultiBufferSource.BufferSource bufT = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
            VertexConsumer trConsumer = bufT.getBuffer(VoidStreamRenderType.TRANSLUCENT);
            for (VoidStreamInstance inst : VOIDS) {
                StreamInstance.Snapshot snap = inst.snapshotWithRadiusMul(partialTick, 0.5F);
                if (snap == null)
                    continue;
                packYawPitch(snap.colours(), yawNorm, pitchNorm);
                poseStack.pushPose();
                poseStack.translate(snap.originX() - cx, snap.originY() - cy, snap.originZ() - cz);
                PolyCone.render(poseStack, trConsumer, snap.points(), snap.colours(), snap.radii(), 0, snap.texSlice(), snap.start());
                poseStack.popPose();
            }
            bufT.endBatch(VoidStreamRenderType.TRANSLUCENT);
        }
    }

    private static void packYawPitch(float[][] colours, float yawNorm, float pitchNorm) {
        for (int i = 0; i < colours.length; i++) {
            float alpha = colours[i][3];
            colours[i][0] = yawNorm;
            colours[i][1] = pitchNorm;
            colours[i][2] = 0.0F;
            colours[i][3] = alpha;
        }
    }
}
