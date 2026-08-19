package com.leclowndu93150.thaumaturge.content.research.link;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ResearchLinkEvents {
    private static final int SYNC_INTERVAL_TICKS = 100;

    private ResearchLinkEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % SYNC_INTERVAL_TICKS != 0) {
            return;
        }
        ResearchLinkData data = ResearchLinkData.get(server);
        if (data.links().isEmpty()) {
            return;
        }
        for (ResearchLinkData.Link link : data.links()) {
            syncLink(server, data, link);
        }
    }

    public static void syncLink(MinecraftServer server, ResearchLinkData data, ResearchLinkData.Link link) {
        List<ServerPlayer> online = new ArrayList<>(2);
        ServerPlayer first = server.getPlayerList().getPlayer(link.first());
        ServerPlayer second = server.getPlayerList().getPlayer(link.second());
        if (first != null) {
            online.add(first);
        }
        if (second != null) {
            online.add(second);
        }
        if (online.isEmpty()) {
            return;
        }
        boolean grew = false;
        for (ServerPlayer player : online) {
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
            if (link.union().addAll(knowledge.researchList())) {
                grew = true;
            }
        }
        if (grew) {
            data.setDirty();
        }
        for (ServerPlayer player : online) {
            applyUnion(player, link);
        }
    }

    private static void applyUnion(ServerPlayer player, ResearchLinkData.Link link) {
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        HolderLookup.RegistryLookup<IResearchEntry> entries = player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY);
        boolean changed = false;
        for (Identifier research : link.union()) {
            if (knowledge.isResearchKnown(research)) {
                continue;
            }
            Holder<IResearchEntry> entry = entries.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)).orElse(null);
            if (entry != null) {
                if (ResearchManager.complete(player, research)) {
                    ResearchManager.setStage(player, research, entry.value().stages().size());
                    changed = true;
                }
            } else if (ResearchManager.unlock(player, research)) {
                changed = true;
            }
        }
        if (changed) {
            knowledge.sync(player);
        }
    }
}
