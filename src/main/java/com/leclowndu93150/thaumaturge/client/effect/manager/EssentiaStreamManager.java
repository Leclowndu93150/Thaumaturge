package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStreamPort;
import com.leclowndu93150.thaumaturge.client.effect.geometry.PolyCone;
import com.leclowndu93150.thaumaturge.client.effect.instance.EssentiaStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.EssentiaStreamRenderType;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.content.effect.StreamPathfinder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaStreamManager extends AbstractFXManager<EssentiaStreamInstance> {
    public static final EssentiaStreamManager INSTANCE = new EssentiaStreamManager();

    private static final Map<String, EssentiaStreamInstance> ACTIVE = new HashMap<>();

    private EssentiaStreamManager() {}

    private static final double PORT_ROUTING_MIN_DISTANCE = 1.75;

    public static void spawn(
            double sx,
            double sy,
            double sz,
            double tx,
            double ty,
            double tz,
            int color,
            int count,
            float scale,
            int extend,
            double my) {
        String key = key(sx, sy, sz, tx, ty, tz, color);
        EssentiaStreamInstance existing = ACTIVE.get(key);
        if (existing != null && !existing.isExpired()) {
            existing.extend(extend);
            return;
        }
        ACTIVE.put(key, routed(new Vec3(sx, sy, sz), new Vec3(tx, ty, tz), color, count, scale, extend, my));
    }

    private static EssentiaStreamInstance routed(
            Vec3 from, Vec3 to, int color, int count, float scale, int extend, double my) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || from.distanceTo(to) < PORT_ROUTING_MIN_DISTANCE) {
            return new EssentiaStreamInstance(from, List.of(to), null, 1, color, count, scale, extend, my);
        }
        Vec3 start = from;
        Vec3 launch = null;
        List<Vec3> route = new ArrayList<>();
        IEssentiaStreamPort.StreamPort exit = portAt(level, from, to, true);
        if (exit != null) {
            start = exit.anchor();
            launch = exit.clearance().subtract(exit.anchor());
            if (launch.lengthSqr() < 1.0E-6) {
                launch = null;
            }
            route.add(exit.clearance());
        }
        IEssentiaStreamPort.StreamPort entry = portAt(level, to, from, false);
        Vec3 pathFrom = exit != null ? exit.clearance() : from;
        Vec3 pathTo = entry != null ? entry.clearance() : to;
        List<Vec3> detour = StreamPathfinder.findRoute(level, pathFrom, pathTo);
        if (detour != null) {
            route.addAll(detour);
        }
        int fixedTail;
        if (entry != null) {
            route.add(entry.clearance());
            route.add(entry.anchor());
            fixedTail = 2;
        } else {
            route.add(to);
            fixedTail = 1;
        }
        return new EssentiaStreamInstance(start, route, launch, fixedTail, color, count, scale, extend, my);
    }

    private static IEssentiaStreamPort.@Nullable StreamPort portAt(
            ClientLevel level, Vec3 at, Vec3 far, boolean outgoing) {
        BlockPos pos = BlockPos.containing(at);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof IEssentiaStreamPort port) {
            return port.essentiaStreamPort(level, pos, state, far, outgoing);
        }
        return null;
    }

    private static String key(double sx, double sy, double sz, double tx, double ty, double tz, int color) {
        return ((int) Math.floor(sx)) + "," + ((int) Math.floor(sy)) + "," + ((int) Math.floor(sz))
                + ":" + ((int) Math.floor(tx)) + "," + ((int) Math.floor(ty)) + "," + ((int) Math.floor(tz))
                + ":" + color;
    }

    @Override
    protected Collection<EssentiaStreamInstance> activeInstances() {
        throw new UnsupportedOperationException(
                "EssentiaStreamManager overrides tickAll directly to handle map-keyed dedup");
    }

    @Override
    public void tickAll(ClientLevel level) {
        Iterator<Map.Entry<String, EssentiaStreamInstance>> it =
                ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, EssentiaStreamInstance> e = it.next();
            e.getValue().tick();
            if (e.getValue().isExpired()) it.remove();
        }
    }

    @Override
    public void renderAll(PoseStack poseStack, Camera camera, float partialTick) {
        if (ACTIVE.isEmpty()) return;
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(2048));
        VertexConsumer consumer =
                IrisCompat.entityEffectBuffers(bufferSource).getBuffer(EssentiaStreamRenderType.RENDER_TYPE);
        double cx = camera.getPosition().x;
        double cy = camera.getPosition().y;
        double cz = camera.getPosition().z;
        for (EssentiaStreamInstance inst : ACTIVE.values()) {
            StreamInstance.Snapshot snap = inst.snapshot(partialTick);
            if (snap == null) continue;
            poseStack.pushPose();
            poseStack.translate(snap.originX() - cx, snap.originY() - cy, snap.originZ() - cz);
            PolyCone.render(
                    poseStack,
                    consumer,
                    snap.points(),
                    snap.colours(),
                    snap.radii(),
                    0x00F000F0,
                    snap.texSlice(),
                    snap.start());
            poseStack.popPose();
        }
        bufferSource.endBatch();
    }
}
