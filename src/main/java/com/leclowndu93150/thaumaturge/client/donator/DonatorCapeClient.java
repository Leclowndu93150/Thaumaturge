package com.leclowndu93150.thaumaturge.client.donator;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import com.leclowndu93150.thaumaturge.content.donator.Donators;
import com.leclowndu93150.thaumaturge.network.ServerboundDonatorCapePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class DonatorCapeClient {
    private DonatorCapeClient() {}

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        sendPreference();
    }

    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ThaumaturgeClientConfig.SPEC) {
            Minecraft.getInstance().execute(DonatorCapeClient::sendPreference);
        }
    }

    public static boolean isLocalDonator() {
        User user = Minecraft.getInstance().getUser();
        return Donators.is(user.getProfileId(), user.getName());
    }

    private static void sendPreference() {
        if (isLocalDonator() && Minecraft.getInstance().getConnection() != null) {
            PacketDistributor.sendToServer(new ServerboundDonatorCapePayload(ThaumaturgeClientConfig.donatorCape()));
        }
    }
}
