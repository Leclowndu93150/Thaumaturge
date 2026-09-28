package com.leclowndu93150.thaumaturge.api.client.golems;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Draws one golem accessory. Registered for an accessory and a {@link GolemAccessoryAnchor} through
 * {@link RegisterGolemAccessoryRenderersEvent}. Client only.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface GolemAccessoryRenderer {
    /**
     * Draws the accessory's geometry. Called on the render thread once per frame for each drawn
     * golem that wears the accessory, with the pose placed at the anchor the renderer was
     * registered on. Not called for golems that are invisible to the player, nor for the outline
     * drawn when a seal-displaying item reveals golems through walls.
     *
     * @param poseStack the pose at the anchor; any pushed pose must be popped before returning
     * @param buffers   the buffer source to draw into
     * @param context   the light, age and synced accessory states of the golem
     */
    void render(PoseStack poseStack, MultiBufferSource buffers, GolemAccessoryRenderContext context);
}
