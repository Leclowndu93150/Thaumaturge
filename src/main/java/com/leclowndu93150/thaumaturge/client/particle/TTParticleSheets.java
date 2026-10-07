package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.TTIds;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTParticleSheets {
    private static final Map<String, ParticleSheet> SHEETS = new ConcurrentHashMap<>();

    private TTParticleSheets() {}

    public static ParticleSheet sheet(String name) {
        return SHEETS.computeIfAbsent(
                name,
                key -> new ParticleSheet(
                        key, ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "textures/particle/" + key + ".png")));
    }

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            SHEETS.values().forEach(ParticleSheet::invalidate);
            TextureManager textures = Minecraft.getInstance().getTextureManager();
            for (ResourceLocation id : manager.listResources(
                            "textures/particle", path -> path.getPath().endsWith(".png"))
                    .keySet()) {
                textures.getTexture(id);
            }
        });
    }
}
