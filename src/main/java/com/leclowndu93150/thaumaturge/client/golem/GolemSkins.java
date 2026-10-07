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
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

/** Material variants are made from the active resource pack's copper golem at runtime. */
@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class GolemSkins {
    public static final Identifier COPPER = Identifier.withDefaultNamespace("textures/entity/copper_golem/copper_golem.png");
    public static final Identifier EYES = Identifier.withDefaultNamespace("textures/entity/copper_golem/copper_golem_eyes.png");
    private static final Map<Identifier, Identifier> CACHE = new HashMap<>();
    private static final List<Identifier> GENERATED = new ArrayList<>();

    private GolemSkins() {}

    public static Identifier forMaterial(Identifier material) {
        return CACHE.computeIfAbsent(material, GolemSkins::create);
    }

    private static Identifier create(Identifier material) {
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
                    int pixel = source.getPixel(x, y);
                    if (ARGB.alpha(pixel) != 0) {
                        int light = luminance(pixel);
                        low = Math.min(low, light);
                        high = Math.max(high, light);
                    }
                }
            }
            NativeImage skin = new NativeImage(source.getWidth(), source.getHeight(), false);
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    int pixel = source.getPixel(x, y);
                    float shade = Mth.clamp((luminance(pixel) - low) / (float) Math.max(1, high - low), 0.0F, 1.0F);
                    int mapped = palette.get(Math.round(shade * (palette.size() - 1)));
                    // Preserve the source's recesses even in pale materials such as iron.
                    float detail = 0.35F + shade * 0.65F;
                    skin.setPixel(x, y, ARGB.color(ARGB.alpha(pixel), Math.round(ARGB.red(mapped) * detail), Math.round(ARGB.green(mapped) * detail), Math.round(ARGB.blue(mapped) * detail)));
                }
            }
            Identifier id = TTIds.rl("dynamic/golem/" + material.getNamespace() + "/" + material.getPath());
            client.getTextureManager().register(id, new DynamicTexture(id::toString, skin));
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
                int color = image.getPixel(x, y);
                if (ARGB.alpha(color) == 255) {
                    colors.add(color);
                }
            }
        }
        return colors.stream().sorted(Comparator.comparingInt(GolemSkins::luminance).thenComparingInt(Integer::intValue)).toList();
    }

    private static int luminance(int color) {
        return (ARGB.red(color) * 54 + ARGB.green(color) * 183 + ARGB.blue(color) * 19) / 256;
    }

    @SubscribeEvent
    public static void onReload(AddClientReloadListenersEvent event) {
        event.addListener(TTIds.rl("golem_skins"), (ResourceManagerReloadListener) resources -> {
            GENERATED.forEach(Minecraft.getInstance().getTextureManager()::release);
            GENERATED.clear();
            CACHE.clear();
            GolemMeshes.clear();
        });
    }
}
