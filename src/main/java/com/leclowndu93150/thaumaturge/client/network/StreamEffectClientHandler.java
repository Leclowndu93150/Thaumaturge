package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker;
import com.leclowndu93150.thaumaturge.client.effect.instance.ArcInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BeamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoltInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoreStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.VoidStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.manager.BeamManager;
import com.leclowndu93150.thaumaturge.client.effect.manager.BoreVoidStreamManager;
import com.leclowndu93150.thaumaturge.client.effect.manager.EssentiaStreamManager;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class StreamEffectClientHandler {
    private StreamEffectClientHandler() {}

    public static void handle(ClientboundStreamEffectPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> dispatch(payload));
    }

    private static void dispatch(ClientboundStreamEffectPayload p) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 source = ClientSpaces.renderPosition(mc.level, new Vec3(p.sx(), p.sy(), p.sz()));
        Vec3 target = ClientSpaces.renderPosition(mc.level, new Vec3(p.tx(), p.ty(), p.tz()));
        switch (p.kind()) {
            case ARC ->
                BeamManager.addArc(new ArcInstance(
                        source.x, source.y, source.z, target.x, target.y, target.z, p.color(), p.extraFloat()));
            case BOLT -> {
                Vec3 from = source;
                if (p.entityId() == mc.player.getId()) {
                    Vec3 tip = WandTipTracker.firstPersonTip();
                    if (tip != null) {
                        from = tip;
                    }
                }
                BeamManager.addBolt(new BoltInstance(
                        from.x, from.y, from.z, target.x, target.y, target.z, p.color(), p.extraFloat()));
            }
            case BEAM ->
                BeamManager.addBeam(new BeamInstance(
                        source.x,
                        source.y,
                        source.z,
                        target.x,
                        target.y,
                        target.z,
                        p.color(),
                        p.extraInt(),
                        p.extraInt2(),
                        p.extraFloat(),
                        p.hasFlag(ClientboundStreamEffectPayload.FLAG_REVERSE),
                        p.entityId(),
                        p.hasFlag(ClientboundStreamEffectPayload.FLAG_WITH_SOURCE),
                        p.hasFlag(ClientboundStreamEffectPayload.FLAG_HIT)));
            case ESSENTIA ->
                EssentiaStreamManager.spawn(
                        source.x,
                        source.y,
                        source.z,
                        target.x,
                        target.y,
                        target.z,
                        p.color(),
                        p.extraInt(),
                        p.extraFloat(),
                        p.extraInt2(),
                        p.extraFloat2());
            case BORE ->
                BoreVoidStreamManager.addBore(new BoreStreamInstance(
                        source.x,
                        source.y,
                        source.z,
                        p.entityId(),
                        p.color(),
                        p.extraInt(),
                        p.extraFloat(),
                        p.extraInt2(),
                        p.extraFloat2()));
            case VOID ->
                BoreVoidStreamManager.addVoid(new VoidStreamInstance(
                        source.x, source.y, source.z, target.x, target.y, target.z, p.extraInt(), p.extraFloat()));
        }
    }
}
