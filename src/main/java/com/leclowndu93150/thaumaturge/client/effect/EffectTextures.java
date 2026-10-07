package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class EffectTextures {
    private EffectTextures() {}

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) EffectTextures::preload);
    }

    private static void preload(ResourceManager manager) {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        for (String directory : new String[] {"textures/effect", "textures/misc"}) {
            for (ResourceLocation id : manager.listResources(
                            directory, path -> path.getPath().endsWith(".png"))
                    .keySet()) {
                if (id.getNamespace().equals(TTIds.MODID)) {
                    textures.getTexture(id);
                }
            }
        }
    }
}
