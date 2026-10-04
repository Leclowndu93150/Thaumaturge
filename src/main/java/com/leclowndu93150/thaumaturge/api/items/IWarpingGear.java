/*
 * Thaumaturge rewrite for modern Minecraft.
 */
package com.leclowndu93150.thaumaturge.api.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Equipment whose warp depends on its wearer. While worn on the body, held in the main hand or equipped as a curio, the piece adds
 * its value to the wearer's passive warp gain and the stack shows the "Warping" tooltip line.
 *
 * <p>Gear with a fixed warp value does not need this interface: give the item the {@code thaumaturge:warp} data component
 * instead, as a default component or on individual stacks. Both sources add up.
 *
 * @since 1.0.0
 */
public interface IWarpingGear {
    /**
     * Called on both sides whenever warp is evaluated, including every tooltip render.
     *
     * @param stack  the equipped stack
     * @param wearer the entity wearing or holding it
     * @return the warp this piece adds, usually 1 to 3; never negative
     */
    int warp(ItemStack stack, LivingEntity wearer);
}
