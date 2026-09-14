package com.leclowndu93150.thaumaturge.content.research.link;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchEvent;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class ResearchLinkEvents {
    private static final int SYNC_INTERVAL_TICKS = 100;
    private static final Set<UUID> CHANGED_PLAYERS = new HashSet<>();

    private ResearchLinkEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!CHANGED_PLAYERS.isEmpty()) {
            Set<UUID> changed = Set.copyOf(CHANGED_PLAYERS);
            CHANGED_PLAYERS.clear();
            for (UUID playerId : changed) {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                if (player != null) syncPlayerLinks(player);
            }
        }
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

    @SubscribeEvent
    public static void onResearchUnlocked(ResearchEvent.Unlocked event) {
        queue(event);
    }

    @SubscribeEvent
    public static void onResearchAdvanced(ResearchEvent.StageAdvanced event) {
        queue(event);
    }

    @SubscribeEvent
    public static void onResearchCompleted(ResearchEvent.Completed event) {
        queue(event);
    }

    private static void queue(ResearchEvent event) {
        if (event.player() instanceof ServerPlayer player) CHANGED_PLAYERS.add(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncPlayerLinks(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResearchLinkData data = ResearchLinkData.get(player.getServer());
        for (ResearchLinkData.Link link : data.links()) {
            if (link.involves(player.getUUID()) && capture(link, player)) {
                data.setDirty();
            }
        }
    }

    private static void syncPlayerLinks(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        ResearchLinkData data = ResearchLinkData.get(server);
        for (ResearchLinkData.Link link : data.links()) {
            if (link.involves(player.getUUID())) {
                syncLink(server, data, link);
            }
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
            grew |= capture(link, player);
        }
        if (grew) {
            data.setDirty();
        }
        for (ServerPlayer player : online) {
            applyUnion(player, link);
        }
    }

    private static boolean capture(ResearchLinkData.Link link, ServerPlayer player) {
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        boolean changed = false;
        for (ResourceLocation research : knowledge.researchList()) {
            ResearchLinkData.Progress observed = new ResearchLinkData.Progress(
                    Math.max(0, knowledge.researchStage(research)), knowledge.isResearchComplete(research));
            ResearchLinkData.Progress previous = link.progress().get(research);
            ResearchLinkData.Progress merged = previous == null ? observed : previous.merge(observed);
            if (!merged.equals(previous)) {
                link.progress().put(research, merged);
                changed = true;
            }
        }
        return changed;
    }

    private static void applyUnion(ServerPlayer player, ResearchLinkData.Link link) {
        PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
        HolderLookup.RegistryLookup<IResearchEntry> entries =
                player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY);
        boolean changed = false;
        boolean progressed;
        do {
            progressed = false;
            for (Map.Entry<ResourceLocation, ResearchLinkData.Progress> shared :
                    link.progress().entrySet()) {
                ResourceLocation research = shared.getKey();
                Holder<IResearchEntry> holder = entries.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research))
                        .orElse(null);
                if (holder == null) {
                    progressed |= ResearchManager.unlock(player, research);
                    continue;
                }
                IResearchEntry entry = holder.value();
                if (!ResearchManager.parentsSatisfied(knowledge, entry)) continue;
                progressed |= applyProgress(player, knowledge, research, entry, shared.getValue());
            }
            changed |= progressed;
        } while (progressed);
        if (changed) {
            knowledge.sync(player);
        }
    }

    private static boolean applyProgress(
            ServerPlayer player,
            PlayerKnowledge knowledge,
            ResourceLocation research,
            IResearchEntry entry,
            ResearchLinkData.Progress shared) {
        boolean changed = ResearchManager.unlock(player, research);
        int targetStage = shared.complete()
                ? entry.stages().size()
                : Math.min(shared.stage(), entry.stages().size());
        while (!knowledge.isResearchComplete(research) && knowledge.researchStage(research) < targetStage) {
            if (!ResearchManager.advanceStage(player, research, false)) break;
            changed = true;
        }
        if (shared.complete()
                && !knowledge.isResearchComplete(research)
                && (entry.stages().isEmpty()
                        || knowledge.researchStage(research) >= entry.stages().size())) {
            changed |= ResearchManager.complete(player, research);
        }
        return changed;
    }
}
