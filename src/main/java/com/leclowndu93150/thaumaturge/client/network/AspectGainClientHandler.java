package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.hud.KnowledgeGainOverlay;
import com.leclowndu93150.thaumaturge.compat.jei.AspectJeiSync;
import com.leclowndu93150.thaumaturge.network.ClientboundAspectGainPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundUpdateJEIAspectListPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class AspectGainClientHandler {
    private static final int BASE_DURATION_TICKS = 40;
    private static final int EXTRA_DURATION_SPREAD = 20;
    private static final int MAX_ICONS_PER_GAIN = 5;

    private AspectGainClientHandler() {}

    public static void handle(ClientboundAspectGainPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                return;
            }
            mc.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(ResourceKey.create(IAspect.REGISTRY_KEY, payload.aspect())).ifPresent(holder -> {
                int icons = Math.min(MAX_ICONS_PER_GAIN, payload.amount());
                for (int i = 0; i < icons; i++) {
                    KnowledgeGainOverlay.addAspectTracker(holder, BASE_DURATION_TICKS + mc.level.getRandom().nextInt(EXTRA_DURATION_SPREAD), mc.level.getRandom().nextLong());
                }
            });
            handleJEISync(ClientboundUpdateJEIAspectListPayload.INSTANCE, ctx);
        });
    }

    public static void handleJEISync(ClientboundUpdateJEIAspectListPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                return;
            }
            if (ModList.get().isLoaded("jei")) {
                AspectJeiSync.syncDiscovered();
            }
        });
    }
}
