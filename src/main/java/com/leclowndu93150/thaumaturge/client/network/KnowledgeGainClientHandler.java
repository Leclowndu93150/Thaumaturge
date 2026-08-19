package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.client.hud.KnowledgeGainOverlay;
import com.leclowndu93150.thaumaturge.network.ClientboundKnowledgeGainPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class KnowledgeGainClientHandler {
    private static final int BASE_DURATION_TICKS = 40;
    private static final int EXTRA_DURATION_SPREAD = 20;

    private KnowledgeGainClientHandler() {}

    public static void handle(ClientboundKnowledgeGainPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null || mc.level == null) {
                return;
            }
            for (int point = 0; point < payload.count(); point++) {
                KnowledgeGainOverlay.addTracker(payload.knowledgeType(), payload.category().orElse(null), BASE_DURATION_TICKS + mc.level.getRandom().nextInt(EXTRA_DURATION_SPREAD),
                        mc.level.getRandom().nextLong());
            }
        });
    }
}
