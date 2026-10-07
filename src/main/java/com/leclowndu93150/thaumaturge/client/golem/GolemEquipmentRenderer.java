package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryRenderContext;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.DyeColor;

public final class GolemEquipmentRenderer {
    private static final ResourceLocation WHITE_WOOL =
            ResourceLocation.withDefaultNamespace("textures/block/white_wool.png");
    private static final ResourceLocation COLOR_BAND = TTIds.rl("models/mesh/golem_color_band.ttmesh");

    private GolemEquipmentRenderer() {}

    public static void render(
            ResourceLocation mesh, PoseStack pose, MultiBufferSource buffers, GolemAccessoryRenderContext context) {
        render(mesh, pose, buffers, context.lightCoords(), -1);
    }

    public static void renderColorBand(GolemRenderState state, PoseStack pose, MultiBufferSource buffers, int color) {
        if (state.color > 0) {
            render(
                    COLOR_BAND,
                    pose,
                    buffers,
                    state.lightCoords,
                    ARGB32.color(
                            ARGB32.alpha(color), DyeColor.byId(state.color - 1).getTextureDiffuseColor()));
        }
    }

    private static void render(ResourceLocation mesh, PoseStack pose, MultiBufferSource buffers, int light, int color) {
        for (TTMeshPart part : GolemMeshes.get(mesh).parts()) {
            ResourceLocation texture = GolemMeshes.texture(part, WHITE_WOOL);
            RenderType type = ARGB32.alpha(color) < 255 || texture.getPath().contains("glass")
                    ? RenderType.entityTranslucent(texture)
                    : RenderType.entityCutout(texture);
            GolemMeshes.renderPart(part, pose.last(), buffers.getBuffer(type), light, color);
        }
    }
}
