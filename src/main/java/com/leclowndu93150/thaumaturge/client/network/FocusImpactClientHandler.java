package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundFocusImpactPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class FocusImpactClientHandler {
    private static final int IMPACT_PARTICLE_BUDGET = 15;
    private static final int BURST_PARTICLE_BUDGET = 10;
    private static final double IMPACT_SPREAD = 0.15;
    private static final double BURST_JITTER_DIVISOR = 20.0;
    private static final float SOURCE_EYE_OFFSET = 0.1F;

    private FocusImpactClientHandler() {}

    public static void handle(ClientboundFocusImpactPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> dispatch(payload));
    }

    private static void dispatch(ClientboundFocusImpactPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || payload.parts().isEmpty()) {
            return;
        }
        Vec3 origin = new Vec3(payload.x(), payload.y(), payload.z());
        Vec3 casterVelocity = Vec3.ZERO;
        if (payload.burst() && payload.casterId() != ClientboundFocusImpactPayload.NO_CASTER) {
            Entity caster = level.getEntity(payload.casterId());
            if (caster != null) {
                origin = caster.position().add(0.0, caster.getEyeHeight() - SOURCE_EYE_OFFSET, 0.0);
                casterVelocity = caster.position().subtract(new Vec3(caster.xOld, caster.yOld, caster.zOld));
            }
        }
        int budget = payload.burst() ? BURST_PARTICLE_BUDGET : IMPACT_PARTICLE_BUDGET;
        int amount = Math.max(1, budget / payload.parts().size());
        RandomSource rand = level.getRandom();
        for (Identifier key : payload.parts()) {
            if (!(FocusEngine.element(key) instanceof FocusEffect effect)) {
                continue;
            }
            for (int a = 0; a < amount; a++) {
                if (payload.burst()) {
                    effect.impactParticles(level, origin, new Vec3(payload.mx() + rand.nextGaussian() / BURST_JITTER_DIVISOR, payload.my() + rand.nextGaussian() / BURST_JITTER_DIVISOR,
                            payload.mz() + rand.nextGaussian() / BURST_JITTER_DIVISOR), casterVelocity);
                } else {
                    effect.impactParticles(level, origin, new Vec3(rand.nextGaussian() * IMPACT_SPREAD, rand.nextGaussian() * IMPACT_SPREAD, rand.nextGaussian() * IMPACT_SPREAD));
                }
            }
        }
    }
}
