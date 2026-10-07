package com.leclowndu93150.thaumaturge.client.taint.overlay;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.taint.overlay.pattern.VeinTaintOverlayPattern;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TaintSkins {
    private static final List<TaintSkinSource> SOURCES =
            List.of(new HandPaintedTaintSkinSource(), new GeneratedTaintSkinSource(new VeinTaintOverlayPattern()));
    private static final Map<Model, Map<ResourceLocation, Optional<TaintSkin>>> CACHE = new IdentityHashMap<>();
    private static @Nullable TaintSkinResources resources;

    private TaintSkins() {}

    public static @Nullable TaintSkin get(Model model, ResourceLocation baseTexture) {
        Map<ResourceLocation, Optional<TaintSkin>> skins = CACHE.get(model);
        if (skins == null) {
            skins = new HashMap<>();
            CACHE.put(model, skins);
        }
        Optional<TaintSkin> skin = skins.get(baseTexture);
        if (skin == null) {
            skin = resolve(model, baseTexture);
            skins.put(baseTexture, skin);
        }
        return skin.orElse(null);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> clear());
    }

    private static Optional<TaintSkin> resolve(Model model, ResourceLocation baseTexture) {
        TaintSkinResources current = resources();
        for (TaintSkinSource source : SOURCES) {
            TaintSkin skin = source.resolve(model, baseTexture, current);
            if (skin != null) {
                return Optional.of(skin);
            }
        }
        return Optional.empty();
    }

    private static TaintSkinResources resources() {
        if (resources == null) {
            Minecraft minecraft = Minecraft.getInstance();
            resources = new TaintSkinResources(minecraft.getResourceManager(), minecraft.getTextureManager());
        }
        return resources;
    }

    private static void clear() {
        if (resources != null) {
            resources.release();
            resources = null;
        }
        CACHE.clear();
    }
}
