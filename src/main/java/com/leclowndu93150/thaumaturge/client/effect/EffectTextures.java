package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class EffectTextures {
    private EffectTextures() {}

    @SubscribeEvent
    static void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(TCIds.rl("effect_textures"), (ResourceManagerReloadListener) EffectTextures::preload);
    }

    private static void preload(ResourceManager manager) {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        for (String directory : new String[]{"textures/effect", "textures/misc"}) {
            for (Identifier id : manager.listResources(directory, path -> path.getPath().endsWith(".png")).keySet()) {
                if (id.getNamespace().equals(TCIds.MODID)) {
                    textures.getTexture(id);
                }
            }
        }
    }
}
