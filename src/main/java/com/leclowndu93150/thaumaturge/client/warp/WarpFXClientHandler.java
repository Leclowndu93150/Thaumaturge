package com.leclowndu93150.thaumaturge.client.warp;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.network.ClientboundWarpFXPayload;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class WarpFXClientHandler {
    private WarpFXClientHandler() {}

    public static void handle(ClientboundWarpFXPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            switch (payload.kind()) {
                case ClientboundWarpFXPayload.KIND_HEARTBEAT -> {
                    if (!ThaumaturgeCommonConfig.NO_STRESS.get()) {
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(TCSounds.HEARTBEAT.get(), 1.0F, 1.0F));
                    }
                }
                case ClientboundWarpFXPayload.KIND_MIST -> WarpFogState.startMist();
                case ClientboundWarpFXPayload.KIND_MIST_SHORT -> WarpFogState.startShortMist();
                default -> {
                }
            }
        });
    }
}
