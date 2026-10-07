package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryRenderContext;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.DyeColor;

public final class GolemEquipmentRenderer {
    private static final Identifier WHITE_WOOL = Identifier.withDefaultNamespace("textures/block/white_wool.png");
    private static final Identifier COLOR_BAND = TTIds.rl("models/mesh/golem_color_band.ttmesh");

    private GolemEquipmentRenderer() {}

    public static void submit(Identifier mesh, PoseStack pose, SubmitNodeCollector collector, GolemAccessoryRenderContext context) {
        submit(mesh, pose, collector, context.lightCoords(), context.color());
    }

    public static void submitColorBand(GolemRenderState state, PoseStack pose, SubmitNodeCollector collector, int color) {
        if (state.color > 0) {
            int tint = ARGB.color(ARGB.alpha(color), DyeColor.byId(state.color - 1).getTextureDiffuseColor());
            submit(COLOR_BAND, pose, collector, state.lightCoords, tint);
        }
    }

    private static void submit(Identifier mesh, PoseStack pose, SubmitNodeCollector collector, int light, int color) {
        for (TTMeshPart part : GolemMeshes.get(mesh).parts()) {
            Identifier texture = GolemMeshes.texture(part, WHITE_WOOL);
            boolean translucent = ARGB.alpha(color) < 255 || texture.getPath().contains("glass");
            collector.submitCustomGeometry(pose, translucent ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture),
                    (matrix, buffer) -> GolemMeshes.renderPart(part, matrix, buffer, light, color));
        }
    }
}
