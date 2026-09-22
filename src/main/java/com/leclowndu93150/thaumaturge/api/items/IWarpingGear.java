package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Equipment that gradually warps its wearer. While worn or held, each piece contributes its
 * warp value to the wearer's passive warp gain, evaluated periodically by the warp system.
 *
 * <p>Implement this on the {@code Item}. Items implementing this interface display the
 * "Warping" tooltip line automatically.
 *
 * @since 1.0.0
 */
public interface IWarpingGear {
    /**
     * The warp contributed by this piece while equipped.
     *
     * @param stack the equipped stack
     * @param wearer the entity wearing or holding the stack
     * @return the warp amount, usually 1 to 3
     */
    int getWarp(ItemStack stack, LivingEntity wearer);
}
