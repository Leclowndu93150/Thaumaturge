package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCParticleSheets {
    private static final Map<String, ParticleSheet> SHEETS = new ConcurrentHashMap<>();

    private TCParticleSheets() {}

    public static ParticleSheet sheet(String name) {
        return SHEETS.computeIfAbsent(name, key -> new ParticleSheet(key, Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/particle/" + key + ".png")));
    }

    @SubscribeEvent
    static void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(TCIds.rl("particle_sheets"), (ResourceManagerReloadListener) manager -> {
            SHEETS.values().forEach(ParticleSheet::invalidate);
            TextureManager textures = Minecraft.getInstance().getTextureManager();
            for (Identifier id : manager.listResources("textures/particle", path -> path.getPath().endsWith(".png")).keySet()) {
                textures.getTexture(id);
            }
        });
    }
}
