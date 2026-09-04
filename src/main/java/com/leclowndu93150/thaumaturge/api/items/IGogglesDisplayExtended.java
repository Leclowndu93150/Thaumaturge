package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/**
 * Provides floating text lines that render above a block when a player wearing goggles
 * looks directly at it.
 *
 * <p>Implement this on a {@link net.minecraft.world.level.block.entity.BlockEntity} or a {@link
 * net.minecraft.world.level.block.Block}; the client overlay checks the crosshair target each
 * frame, the block entity taking precedence over the block. Text renders as yaw-billboarded,
 * depth-ignoring lines centered above the block.
 *
 * @since 1.0.0
 */
public interface IGogglesDisplayExtended {
    /**
     * Returns the text lines to display, ordered top to bottom.
     *
     * <p>Called on the client render thread every frame while the block is targeted, so the
     * result must be computed from client-synced state only and should allocate sparingly.
     *
     * @return the lines to render; an empty array renders nothing
     */
    Component[] getIGogglesText();

    /**
     * Returns the aspect tags to render as world-space icons (with quantity badges) above the
     * block, e.g. the remaining essentia of an active infusion. Rendered by the goggles overlay
     * using {@code AspectTagWorldRenderer}, so it must be computed from client-synced state only.
     *
     * @return the aspects to display; an empty list renders nothing
     */
    default AspectList getIGogglesTags() {
        return AspectList.EMPTY;
    }

    /**
     * Returns the offset from the block position at which the text column is anchored.
     *
     * @return the anchor offset in block units; {@link Vec3#ZERO} centers on the block
     */
    default Vec3 getIGogglesTextOffset() {
        return Vec3.ZERO;
    }
}
