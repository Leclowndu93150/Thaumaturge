package com.leclowndu93150.thaumaturge.api.items;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Text that floats above a block while a player wearing revealing gear looks straight at it.
 *
 * <p>Implement it on a {@link BlockEntity} or on a {@link Block}; when both implement it the block entity wins. The lines render as a
 * column of yaw-billboarded text that ignores depth, centred on the anchor.
 *
 * @since 1.0.0
 */
public interface IGogglesReadout {
    /**
     * Called on the client render thread every frame while the block is targeted. Build it from client-synced state only and keep it
     * cheap.
     *
     * @return the lines, top to bottom; an empty list draws nothing
     */
    List<Component> readout();

    /**
     * @return the anchor offset from the block's minimum corner, in blocks; {@link Vec3#ZERO} by default
     */
    default Vec3 readoutAnchor() {
        return Vec3.ZERO;
    }
}
