package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.BoxGeometry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class GolemAccessoryRenderer {
    private static final Identifier TEXTURE = TCIds.rl("textures/models/golem_decoration.png");
    private static final int TEX_SIZE = 128;
    private static final int WHITE = 0xFFFFFFFF;

    private static final float SX = 0.09375F / 4.0F;
    private static final float SY = 0.1875F / 9.0F;
    private static final float SZ = 0.09375F / 4.0F;
    private static final float HEAD_TOP = 0.1875F;
    private static final float HEAD_FRONT = -0.09375F;

    private static final float BX = 0.15625F / 8.0F;
    private static final float BY = 0.25F / 12.0F;
    private static final float BZ = 0.125F / 5.5F;
    private static final float CHEST_TOP = 0.25F;

    private GolemAccessoryRenderer() {}

    public static void submitHead(GolemRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        String accessories = state.accessories;
        if (accessories.isEmpty()) {
            return;
        }
        boolean fez = accessories.contains("thaumaturge:fez");
        boolean topHat = accessories.contains("thaumaturge:top_hat");
        boolean glasses = accessories.contains("thaumaturge:glasses");
        boolean visor = accessories.contains("thaumaturge:visor");
        if (!fez && !topHat && !glasses && !visor) {
            return;
        }
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, buffer) -> {
            if (fez) {
                BoxGeometry.box(pose, buffer, -4.5F * SX, HEAD_TOP - 3.0F * SY, -4.5F * SZ, 4.5F * SX, HEAD_TOP + 4.0F * SY, 4.5F * SZ, 0, 94, 9, 7, 9, TEX_SIZE, TEX_SIZE, WHITE, light, false);
            }
            if (topHat) {
                BoxGeometry.box(pose, buffer, -4.5F * SX, HEAD_TOP - 3.0F * SY, -4.5F * SZ, 4.5F * SX, HEAD_TOP + 6.0F * SY, 4.5F * SZ, 0, 110, 9, 9, 9, TEX_SIZE, TEX_SIZE, WHITE, light, false);
                BoxGeometry.box(pose, buffer, -6.5F * SX, HEAD_TOP - 3.0F * SY, -6.5F * SZ, 6.5F * SX, HEAD_TOP - 2.0F * SY, 6.5F * SZ, 36, 114, 13, 1, 13, TEX_SIZE, TEX_SIZE, WHITE, light, false);
            }
            if (glasses) {
                BoxGeometry.box(pose, buffer, -4.5F * SX, HEAD_TOP - 7.0F * SY, -4.5F * SZ, 4.5F * SX, HEAD_TOP - 3.0F * SY, 4.5F * SZ, 0, 80, 9, 4, 9, TEX_SIZE, TEX_SIZE, WHITE, light, false);
            }
            if (visor) {
                BoxGeometry.box(pose, buffer, -5.0F * SX, HEAD_TOP - 8.0F * SY, HEAD_FRONT - 0.5F * SZ, 5.0F * SX, HEAD_TOP - 3.0F * SY, HEAD_FRONT + 4.5F * SZ, 0, 70, 10, 5, 5, TEX_SIZE, TEX_SIZE,
                        WHITE, light, false);
            }
        });
    }

    public static void submitBody(GolemRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!state.accessories.contains("thaumaturge:bowtie")) {
            return;
        }
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TEXTURE), (pose, buffer) -> BoxGeometry.box(pose, buffer, -8.5F * BX, CHEST_TOP - 4.0F * BY, -6.0F * BZ, 8.5F * BX,
                CHEST_TOP, 6.0F * BZ, 0, 0, 17, 4, 12, TEX_SIZE, TEX_SIZE, WHITE, light, false));
    }
}
