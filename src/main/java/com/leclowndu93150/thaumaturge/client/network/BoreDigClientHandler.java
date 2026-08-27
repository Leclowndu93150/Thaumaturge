package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.effect.instance.BoreDigEffect;
import com.leclowndu93150.thaumaturge.client.effect.manager.BoreDigEffectManager;
import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundBoreDigPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class BoreDigClientHandler {
    private BoreDigClientHandler() {}

    public static void handle(ClientboundBoreDigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }
            BlockState state = level.getBlockState(payload.target());
            if (state.isAir()) {
                return;
            }
            if (payload.boreEntityId() != BoreDebrisParticleOptions.NO_ENTITY && level.getEntity(payload.boreEntityId()) == null) {
                return;
            }
            BoreDigEffectManager.INSTANCE.add(new BoreDigEffect(level, payload.target(), payload.boreEntityId(), payload.borePos(), state, payload.delay()));
        });
    }
}
