package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.network.ClientboundAspectIndexPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class AspectIndexEvents {
    private static volatile boolean datamapsReady = false;
    private static volatile boolean buildRequested = false;
    private static volatile MinecraftServer runningServer = null;

    private AspectIndexEvents() {}

    @SubscribeEvent
    public static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        if (event.getRegistryKey() != Registries.ITEM) {
            return;
        }
        if (event.getCause() != DataMapsUpdatedEvent.UpdateCause.SERVER_RELOAD) {
            return;
        }
        datamapsReady = true;
        buildRequested = true;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        runningServer = event.getServer();
        buildRequested = true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        MinecraftServer server = runningServer;
        if (!buildRequested || !datamapsReady || server != event.getServer()) {
            return;
        }
        buildRequested = false;
        buildAndBroadcast(server);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        datamapsReady = false;
        buildRequested = false;
        runningServer = null;
        AspectIndexHolder.set(AspectIndex.EMPTY);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new ClientboundAspectIndexPayload(AspectIndexHolder.getConcrete()));
        }
    }

    private static void buildAndBroadcast(MinecraftServer server) {
        AspectIndexFingerprint fingerprint = AspectIndexFingerprint.compute(server);
        AspectIndex index =
                AspectIndexFile.load(server.registryAccess(), fingerprint).orElse(null);
        if (index == null) {
            int itemCount = BuiltInRegistries.ITEM.size();
            int recipeCount = server.getRecipeManager().getRecipes().size();
            index = AspectIndexBuilder.build(server.getRecipeManager(), server.registryAccess());
            AspectIndexFile.write(index, server.registryAccess(), fingerprint, itemCount, recipeCount);
        }
        AspectIndexHolder.set(index);
        if (!server.getPlayerList().getPlayers().isEmpty()) {
            PacketDistributor.sendToAllPlayers(new ClientboundAspectIndexPayload(index));
        }
    }
}
