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
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class DonatorCapeClient {
    private DonatorCapeClient() {}

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        sendPreference();
    }

    @SubscribeEvent
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ThaumaturgeClientConfig.SPEC) {
            Minecraft.getInstance().execute(DonatorCapeClient::sendPreference);
        }
    }

    @SubscribeEvent
    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new DonatorCapeModifier());
    }

    public static boolean isLocalDonator() {
        User user = Minecraft.getInstance().getUser();
        return Donators.is(user.getProfileId(), user.getName());
    }

    private static void sendPreference() {
        if (isLocalDonator() && Minecraft.getInstance().getConnection() != null) {
            ClientPacketDistributor.sendToServer(new ServerboundDonatorCapePayload(ThaumaturgeClientConfig.donatorCape()));
        }
    }
}
