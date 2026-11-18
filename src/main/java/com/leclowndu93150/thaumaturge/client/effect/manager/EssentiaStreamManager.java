package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStreamPort;
import com.leclowndu93150.thaumaturge.client.effect.geometry.TubeMesh;
import com.leclowndu93150.thaumaturge.client.effect.instance.EssentiaStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.StreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.EssentiaStreamRenderType;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.leclowndu93150.thaumaturge.content.effect.StreamPathfinder;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
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
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaStreamManager extends AbstractFXManager<EssentiaStreamInstance> {
    public static final EssentiaStreamManager INSTANCE = new EssentiaStreamManager();

    private record StreamKey(
            BlockPos from,
            BlockPos to,
            int color,
            @Nullable SpaceFrame frame,
            @Nullable Block sourcePort,
            @Nullable Block destinationPort) {
        boolean isLoaded(ClientLevel level) {
            return (frame == null || frame.equals(LevelSpaces.frameOf(level, from)))
                    && (sourcePort == null || level.getBlockState(from).is(sourcePort))
                    && (destinationPort == null || level.getBlockState(to).is(destinationPort));
        }
    }

    private static final Map<StreamKey, EssentiaStreamInstance> ACTIVE = new HashMap<>();

    private EssentiaStreamManager() {}

    private static final double PORT_ROUTING_MIN_DISTANCE = 1.75;

    public static void spawn(Vec3 from, Vec3 to, int color, int count, float scale, int extend, double my) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        SpaceFrame frame = LevelSpaces.frameOf(level, from);
        // Keep the route and trail in plot coordinates when both ends move together.
        boolean attached = !frame.isWorld() && frame.equals(LevelSpaces.frameOf(level, to));
        if (!attached) {
            from = ClientSpaces.renderPosition(level, from);
            to = ClientSpaces.renderPosition(level, to);
        }
        BlockPos source = BlockPos.containing(from);
        BlockPos destination = BlockPos.containing(to);
        StreamKey key = new StreamKey(
                source,
                destination,
                color,
                attached ? frame : null,
                streamPortBlock(level, source),
                streamPortBlock(level, destination));
        EssentiaStreamInstance existing = ACTIVE.get(key);
        if (existing != null && !existing.isExpired()) {
            existing.extend(extend);
            return;
        }
        ACTIVE.put(key, routed(level, from, to, color, count, scale, extend, my));
    }

    private static @Nullable Block streamPortBlock(ClientLevel level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block instanceof IEssentiaStreamPort ? block : null;
    }

    private static EssentiaStreamInstance routed(
            ClientLevel level, Vec3 from, Vec3 to, int color, int count, float scale, int extend, double my) {
        if (from.distanceTo(to) < PORT_ROUTING_MIN_DISTANCE) {
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

    @Override
    protected Collection<EssentiaStreamInstance> activeInstances() {
        throw new UnsupportedOperationException(
                "EssentiaStreamManager overrides tickAll directly to handle map-keyed dedup");
    }

    @Override
    public void tickAll(ClientLevel level) {
        Iterator<Map.Entry<StreamKey, EssentiaStreamInstance>> it =
                ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<StreamKey, EssentiaStreamInstance> e = it.next();
            if (!e.getKey().isLoaded(level)) {
                it.remove();
                continue;
            }
            e.getValue().tick();
            if (e.getValue().isExpired()) it.remove();
        }
    }

    @Override
    public void renderAll(PoseStack poseStack, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || ACTIVE.isEmpty()) return;
        boolean fabulous = Minecraft.useShaderTransparency();
        if (!fabulous) {
            mc.getMainRenderTarget().bindWrite(false);
        }
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        RenderType renderType = fabulous ? EssentiaStreamRenderType.FABULOUS : EssentiaStreamRenderType.RENDER_TYPE;
        MultiBufferSource effectBuffers = fabulous ? IrisCompat.entityEffectBuffers(bufferSource) : bufferSource;
        VertexConsumer consumer = effectBuffers.getBuffer(renderType);
        Vec3 cameraPos = camera.getPosition();
        for (var entry : ACTIVE.entrySet()) {
            StreamKey key = entry.getKey();
            if (!key.isLoaded(level)) continue;
            StreamInstance.Snapshot snap = entry.getValue().snapshot(partialTick);
            if (snap == null) continue;
            poseStack.pushPose();
            Vec3 origin = new Vec3(snap.originX(), snap.originY(), snap.originZ());
            if (key.frame() != null) {
                ClientSpaces.applyFrame(poseStack, level, origin, cameraPos);
            } else {
                poseStack.translate(origin.x - cameraPos.x, origin.y - cameraPos.y, origin.z - cameraPos.z);
            }
            TubeMesh.draw(
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
        bufferSource.endBatch(renderType);
        if (fabulous) {
            // Leave the Fabulous particle stage bound for the next subscriber.
            RenderStateShard.PARTICLES_TARGET.setupRenderState();
        }
    }

    @Override
    public void clear() {
        ACTIVE.clear();
    }
}
