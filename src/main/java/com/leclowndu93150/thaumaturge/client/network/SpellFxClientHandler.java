package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker;
import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker.WandSource;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpellFxPayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundWandSpellFxPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;

public final class SpellFxClientHandler {
    private static final int PARTICLE_BUDGET = 96;
    private static final int MIN_PER_EVENT = 2;
    private static final int MAX_PER_EVENT = 10;
    private static final double IMPACT_SPREAD = 0.2;
    private static final double IMPACT_DRIFT = 0.04;
    private static final double BURST_JITTER = 0.06;

    private SpellFxClientHandler() {}

    public static void handle(ClientboundSpellFxPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> play(payload, null, 0));
    }

    public static void handle(ClientboundWandSpellFxPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            WandSource wand = player == null ? null : WandTipTracker.heldWand(player, payload.hand());
            play(payload.effects(), wand == null ? null : wand.tip(), payload.wandEvents());
        });
    }

    private static void play(ClientboundSpellFxPayload payload, @Nullable Vec3 wandTip, int wandEvents) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || payload.events().isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        int each = Math.clamp(PARTICLE_BUDGET / payload.events().size(), MIN_PER_EVENT, MAX_PER_EVENT);
        for (int index = 0; index < payload.events().size(); index++) {
            ClientboundSpellFxPayload.Event event = payload.events().get(index);
            Vec3 at = wandTip != null && index < Integer.SIZE && (wandEvents & (1 << index)) != 0
                    ? wandTip
                    : payload.anchor().add(event.x(), event.y(), event.z());
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
