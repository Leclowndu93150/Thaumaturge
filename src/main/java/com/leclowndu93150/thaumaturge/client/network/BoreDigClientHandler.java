package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.effect.instance.BoreDigEffect;
import com.leclowndu93150.thaumaturge.client.effect.manager.BoreDigEffectManager;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundBoreDigPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
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
            Entity bore = level.getEntity(payload.boreEntityId());
            BlockState state = level.getBlockState(payload.target());
            if (bore == null || state.isAir()) {
                return;
            }
            BoreDigEffectManager.INSTANCE.add(new BoreDigEffect(level, payload.target(), payload.boreEntityId(), state, payload.delay()));
        });
    }
}
