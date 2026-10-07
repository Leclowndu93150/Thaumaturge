package com.leclowndu93150.thaumaturge.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.donator.DonatorCapeClient;
import com.leclowndu93150.thaumaturge.client.donator.DonatorConfigFilter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = TTIds.MODID, dist = Dist.CLIENT)
public final class ThaumaturgeClient {
    public ThaumaturgeClient(IEventBus modBus, ModContainer container) {
        modBus.addListener(DonatorCapeClient::onConfigReloading);
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (mod, parent) -> new ConfigurationScreen(mod, parent, new DonatorConfigFilter()));
    }
}
