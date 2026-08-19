package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.effect.instance.ArcInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BeamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoltInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.BoreStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.instance.VoidStreamInstance;
import com.leclowndu93150.thaumaturge.client.effect.manager.BeamManager;
import com.leclowndu93150.thaumaturge.client.effect.manager.BoreVoidStreamManager;
import com.leclowndu93150.thaumaturge.client.effect.manager.EssentiaStreamManager;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class StreamEffectClientHandler {
    private StreamEffectClientHandler() {}

    public static void handle(ClientboundStreamEffectPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> dispatch(payload));
    }

    private static void dispatch(ClientboundStreamEffectPayload p) {
        switch (p.kind()) {
            case ARC -> BeamManager.addArc(new ArcInstance(p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(), p.color(), p.extraFloat()));
            case BOLT -> BeamManager.addBolt(new BoltInstance(p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(), p.color(), p.extraFloat()));
            case BEAM -> BeamManager.addBeam(new BeamInstance(p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(), p.color(), p.extraInt(), p.extraInt2(), p.extraFloat(),
                    p.hasFlag(ClientboundStreamEffectPayload.FLAG_REVERSE), p.entityId(), p.hasFlag(ClientboundStreamEffectPayload.FLAG_WITH_SOURCE)));
            case ESSENTIA -> EssentiaStreamManager.spawn(p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(), p.color(), p.extraInt(), p.extraFloat(), p.extraInt2(), p.extraFloat2());
            case BORE -> BoreVoidStreamManager.addBore(new BoreStreamInstance(p.sx(), p.sy(), p.sz(), p.entityId(), p.color(), p.extraInt(), p.extraFloat(), p.extraInt2(), p.extraFloat2()));
            case VOID -> BoreVoidStreamManager.addVoid(new VoidStreamInstance(p.sx(), p.sy(), p.sz(), p.tx(), p.ty(), p.tz(), p.extraInt(), p.extraFloat()));
        }
    }
}
