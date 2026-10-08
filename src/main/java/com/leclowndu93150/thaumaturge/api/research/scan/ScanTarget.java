package com.leclowndu93150.thaumaturge.api.research.scan;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * What the thaumometer is pointed at.
 *
 * @since 1.0.0
 */
public sealed interface ScanTarget permits ScannedBlock, ScannedEntity, ScannedStack, ScannedSky {
    /**
     * @param pos a block in the scanning player's level
     * @return a block target
     */
    static ScanTarget block(BlockPos pos) {
        return new ScannedBlock(pos);
    }

    /**
     * @param entity the scanned entity; a dropped item counts as its stack for item-based scans
     * @return an entity target
     */
    static ScanTarget entity(Entity entity) {
        return new ScannedEntity(entity);
    }

    /**
     * @param stack a stack in an inventory or a container
     * @return a stack target
     */
    static ScanTarget stack(ItemStack stack) {
        return new ScannedStack(stack);
    }

    /**
     * @return the target for a scan that hit nothing, used by sky scans
     */
    static ScanTarget sky() {
        return ScannedSky.INSTANCE;
    }

    /**
     * @return the scanned entity when it is anything but a dropped item; null otherwise
     */
    default @Nullable Entity creature() {
        return null;
    }

    /**
     * @return the scanned stack, or the stack inside a scanned dropped item; empty otherwise. Blocks are not converted, see
     *         {@link ScanningManager#stackOf}.
     */
    default ItemStack carriedStack() {
        return ItemStack.EMPTY;
    }
}
