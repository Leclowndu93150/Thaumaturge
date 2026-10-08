package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpellFxPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SpellFxClientHandler {
    private static final int PARTICLE_BUDGET = 96;
    private static final int MIN_PER_EVENT = 2;
    private static final int MAX_PER_EVENT = 10;
    private static final double IMPACT_SPREAD = 0.2;
    private static final double IMPACT_DRIFT = 0.04;
    private static final double BURST_JITTER = 0.06;

    private SpellFxClientHandler() {}

    public static void handle(ClientboundSpellFxPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> play(payload));
    }

    private static void play(ClientboundSpellFxPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || payload.events().isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        int each = Math.clamp(PARTICLE_BUDGET / payload.events().size(), MIN_PER_EVENT, MAX_PER_EVENT);
        for (ClientboundSpellFxPayload.Event event : payload.events()) {
            Vec3 at = payload.anchor().add(event.x(), event.y(), event.z());
            Vec3 heading = new Vec3(event.dx(), event.dy(), event.dz());
            for (int particle = 0; particle < each; particle++) {
                if (event.directed()) {
                    Vec3 motion = heading.add(
                            random.nextGaussian() * BURST_JITTER,
                            random.nextGaussian() * BURST_JITTER,
                            random.nextGaussian() * BURST_JITTER);
                    event.style().value().spawn(level, at, motion, event.color(), random);
                } else {
                    Vec3 spot = at.add(
                            random.nextGaussian() * IMPACT_SPREAD,
                            random.nextGaussian() * IMPACT_SPREAD,
                            random.nextGaussian() * IMPACT_SPREAD);
                    Vec3 drift = new Vec3(
                            random.nextGaussian() * IMPACT_DRIFT,
                            random.nextGaussian() * IMPACT_DRIFT,
                            random.nextGaussian() * IMPACT_DRIFT);
                    event.style().value().spawn(level, spot, drift, event.color(), random);
                }
            }
        }
    }
}
