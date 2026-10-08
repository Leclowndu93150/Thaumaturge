package com.leclowndu93150.thaumaturge.client.toast;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import com.leclowndu93150.thaumaturge.network.ServerboundClearResearchFlagsPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ResearchToastEvents {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static int tickCounter;

    private ResearchToastEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || ++tickCounter < CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(mc.player);
        List<ResourceLocation> shown = new ArrayList<>();
        for (ResourceLocation research : knowledge.researchList()) {
            if (!knowledge.hasResearchFlag(research, ResearchFlag.POPUP)) {
                continue;
            }
            mc.player
                    .registryAccess()
                    .lookup(IResearchEntry.REGISTRY_KEY)
                    .flatMap(lookup -> lookup.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)))
                    .ifPresent(holder -> mc.getToasts()
                            .addToast(new ResearchToast(
                                    research,
                                    Component.translatable("gui.thaumaturge.research.complete"),
                                    Component.translatable(holder.value().nameKey()),
                                    EntryIconRenderer.resolveIcon(holder.value(), mc.player.tickCount))));
            knowledge.clearResearchFlag(research, ResearchFlag.POPUP);
            shown.add(research);
        }
        for (ResourceLocation research : shown) {
            PacketDistributor.sendToServer(
                    new ServerboundClearResearchFlagsPayload(research, List.of(ResearchFlag.POPUP)));
        }
    }
}
