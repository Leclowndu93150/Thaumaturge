package com.leclowndu93150.thaumaturge.content.focus;

import com.leclowndu93150.thaumaturge.network.effect.ClientboundFocusImpactPayload;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class FocusFX {
    private static final double FX_RADIUS = 64.0;

    private FocusFX() {}

    public static void impact(ServerLevel level, Vec3 hit, Identifier partKey) {
        send(level, new ClientboundFocusImpactPayload(hit.x, hit.y, hit.z, 0.0F, 0.0F, 0.0F, false, ClientboundFocusImpactPayload.NO_CASTER, List.of(partKey)));
    }

    public static void burst(ServerLevel level, Vec3 source, Vec3 halfDirection, List<Identifier> effectIds, @Nullable LivingEntity caster) {
        if (effectIds.isEmpty()) {
            return;
        }
        int casterId = caster != null ? caster.getId() : ClientboundFocusImpactPayload.NO_CASTER;
        send(level, new ClientboundFocusImpactPayload(source.x, source.y, source.z, (float) halfDirection.x, (float) halfDirection.y, (float) halfDirection.z, true, casterId, effectIds));
    }

    private static void send(ServerLevel level, ClientboundFocusImpactPayload payload) {
        PacketDistributor.sendToPlayersNear(level, null, payload.x(), payload.y(), payload.z(), FX_RADIUS, payload);
    }
}
