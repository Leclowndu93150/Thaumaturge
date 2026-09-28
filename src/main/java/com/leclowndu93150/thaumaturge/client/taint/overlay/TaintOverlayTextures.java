package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TaintOverlayTextures {
    private static final TaintOverlayPattern PATTERN = new VeinTaintOverlayPattern();
    private static final Map<Model, ResourceLocation> CACHE = new IdentityHashMap<>();
    private static int nextId;

    private TaintOverlayTextures() {}

    public static ResourceLocation get(Model model) {
        return CACHE.computeIfAbsent(model, TaintOverlayTextures::create);
    }

    private static ResourceLocation create(Model model) {
        ModelUvLayout layout = ModelUvLayout.of(model);
        NativeImage image = new NativeImage(layout.width(), layout.height(), true);
        PATTERN.paint(
                image, layout, model.getClass().getName().hashCode() * 31L + layout.width() * 7L + layout.height());
        ResourceLocation id = TCIds.rl("dynamic/taint_overlay/" + nextId++);
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
        return id;
    }

    @SubscribeEvent
    static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> clear());
    }

    private static void clear() {
        for (ResourceLocation id : CACHE.values()) {
            Minecraft.getInstance().getTextureManager().release(id);
        }
        CACHE.clear();
    }
}
