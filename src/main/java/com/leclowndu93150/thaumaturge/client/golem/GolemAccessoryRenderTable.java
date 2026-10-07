package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryRenderContext;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryRenderer;
import com.leclowndu93150.thaumaturge.api.client.golems.RegisterGolemAccessoryRenderersEvent;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;

public final class GolemAccessoryRenderTable {
    public static final GolemAccessoryRenderTable EMPTY = new GolemAccessoryRenderTable(Map.of());
    private final Map<GolemAccessoryAnchor, Map<ResourceLocation, List<GolemAccessoryRenderer>>> renderers;

    private GolemAccessoryRenderTable(
            Map<GolemAccessoryAnchor, Map<ResourceLocation, List<GolemAccessoryRenderer>>> renderers) {
        this.renderers = renderers;
    }

    public static GolemAccessoryRenderTable collect(EntityRendererProvider.Context context) {
        RegisterGolemAccessoryRenderersEvent event = new RegisterGolemAccessoryRenderersEvent(context);
        ModLoader.postEvent(event);
        Map<GolemAccessoryAnchor, Map<ResourceLocation, List<GolemAccessoryRenderer>>> renderers =
                new EnumMap<>(GolemAccessoryAnchor.class);
        for (RegisterGolemAccessoryRenderersEvent.Registration registration : event.registrations()) {
            renderers
                    .computeIfAbsent(registration.anchor(), anchor -> new HashMap<>())
                    .computeIfAbsent(registration.accessory().id(), id -> new ArrayList<>())
                    .add(registration.renderer());
        }
        return new GolemAccessoryRenderTable(renderers);
    }

    public void render(
            GolemAccessoryAnchor anchor, GolemRenderState state, PoseStack poseStack, MultiBufferSource buffers) {
        Map<ResourceLocation, List<GolemAccessoryRenderer>> byAccessory = renderers.get(anchor);
        if (byAccessory == null || state.accessories.isEmpty()) {
            return;
        }
        GolemAccessoryRenderContext context = null;
        for (GolemAccessory accessory : state.accessories) {
            List<GolemAccessoryRenderer> accessoryRenderers = byAccessory.get(accessory.id());
            if (accessoryRenderers == null) {
                continue;
            }
            if (context == null) {
                context = new GolemAccessoryRenderContext(state.lightCoords, state.ageInTicks, state.accessoryStates);
            }
            for (GolemAccessoryRenderer renderer : accessoryRenderers) {
                renderer.render(poseStack, buffers, context);
            }
        }
    }
}
