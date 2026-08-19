package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.entity.WispEntity;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.leclowndu93150.thaumaturge.network.ClientboundWispZapPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class WispZapClientHandler {
    private static final float BOLT_WIDTH = 0.6F;

    private WispZapClientHandler() {}

    public static void handle(ClientboundWispZapPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            Entity source = mc.level.getEntity(payload.sourceId());
            Entity target = mc.level.getEntity(payload.targetId());
            if (source == null || target == null) {
                return;
            }
            float r = 1.0F;
            float g = 1.0F;
            float b = 1.0F;
            if (source instanceof WispEntity wisp) {
                Holder<IAspect> aspect = wisp.aspect();
                if (aspect != null) {
                    int color = aspect.value().color();
                    r = ARGB.red(color) / 255.0F;
                    g = ARGB.green(color) / 255.0F;
                    b = ARGB.blue(color) / 255.0F;
                }
            }
            mc.level.addParticle(new BoltParticleOptions(target.getX(), target.getY(), target.getZ(), r, g, b, BOLT_WIDTH), source.getX(), source.getY(), source.getZ(), 0.0, 0.0, 0.0);
        });
    }
}
