package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/** Material variants are made from the active resource pack's copper golem at runtime. */
@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class GolemSkins {
    public static final ResourceLocation COPPER = TTIds.rl("textures/entity/golem/copper_golem.png");
    public static final ResourceLocation EYES = TTIds.rl("textures/entity/golem/copper_golem_eyes.png");
    private static final Map<ResourceLocation, ResourceLocation> CACHE = new HashMap<>();
    private static final List<ResourceLocation> GENERATED = new ArrayList<>();

    private GolemSkins() {}

    public static ResourceLocation forMaterial(ResourceLocation material) {
        return CACHE.computeIfAbsent(material, GolemSkins::create);
    }

    private static ResourceLocation create(ResourceLocation material) {
        Minecraft client = Minecraft.getInstance();
        ResourceManager resources = client.getResourceManager();
        try (InputStream sourceStream = resources.open(COPPER);
                NativeImage source = NativeImage.read(sourceStream);
                InputStream materialStream = resources.open(material);
                NativeImage swatch = NativeImage.read(materialStream)) {
            List<Integer> palette = palette(swatch);
            if (palette.isEmpty()) {
                return COPPER;
            }
            int low = 255;
            int high = 0;
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    int pixel = ARGB32.color(
                            ABGR32.alpha(source.getPixelRGBA(x, y)),
                            ABGR32.red(source.getPixelRGBA(x, y)),
                            ABGR32.green(source.getPixelRGBA(x, y)),
                            ABGR32.blue(source.getPixelRGBA(x, y)));
                    if (ARGB32.alpha(pixel) != 0) {
                        int light = luminance(pixel);
                        low = Math.min(low, light);
                        high = Math.max(high, light);
                    }
                }
            }
            NativeImage skin = new NativeImage(source.getWidth(), source.getHeight(), false);
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    int pixel = ARGB32.color(
                            ABGR32.alpha(source.getPixelRGBA(x, y)),
                            ABGR32.red(source.getPixelRGBA(x, y)),
                            ABGR32.green(source.getPixelRGBA(x, y)),
                            ABGR32.blue(source.getPixelRGBA(x, y)));
                    float shade = Mth.clamp((luminance(pixel) - low) / (float) Math.max(1, high - low), 0.0F, 1.0F);
                    int mapped = palette.get(Math.round(shade * (palette.size() - 1)));
                    // Preserve the source's recesses even in pale materials such as iron.
                    float detail = 0.35F + shade * 0.65F;
                    skin.setPixelRGBA(
                            x,
                            y,
                            ABGR32.fromArgb32(ARGB32.color(
                                    ARGB32.alpha(pixel),
                                    Math.round(ARGB32.red(mapped) * detail),
                                    Math.round(ARGB32.green(mapped) * detail),
                                    Math.round(ARGB32.blue(mapped) * detail))));
                }
            }
            ResourceLocation id = TTIds.rl("dynamic/golem/" + material.getNamespace() + "/" + material.getPath());
            client.getTextureManager().register(id, new DynamicTexture(skin));
            GENERATED.add(id);
            return id;
        } catch (IOException e) {
            Thaumaturge.LOGGER.error("Could not build copper golem material skin for {}", material, e);
            return COPPER;
        }
    }

    private static List<Integer> palette(NativeImage image) {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = ARGB32.color(
                        ABGR32.alpha(image.getPixelRGBA(x, y)),
                        ABGR32.red(image.getPixelRGBA(x, y)),
                        ABGR32.green(image.getPixelRGBA(x, y)),
                        ABGR32.blue(image.getPixelRGBA(x, y)));
                if (ARGB32.alpha(color) == 255) {
                    colors.add(color);
                }
            }
        }
        return colors.stream()
                .sorted(Comparator.comparingInt(GolemSkins::luminance).thenComparingInt(Integer::intValue))
                .toList();
    }

    private static int luminance(int color) {
        return (ARGB32.red(color) * 54 + ARGB32.green(color) * 183 + ARGB32.blue(color) * 19) / 256;
    }

    @SubscribeEvent
    public static void onReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resources -> {
            GENERATED.forEach(Minecraft.getInstance().getTextureManager()::release);
            GENERATED.clear();
            CACHE.clear();
            GolemMeshes.clear();
        });
    }
}
