package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Implemented by blocks, block entities, or items that react when right-clicked with an
 * {@link ICaster} item, before normal caster behavior runs.
 *
 * @since 1.0.0
 */
public interface IInteractWithCaster {
    /**
     * Called when a caster item right-clicks this target.
     *
     * @param level       the level the interaction happens in
     * @param casterStack the caster stack used
     * @param player      the interacting player
     * @param pos         the clicked position
     * @param side        the clicked face
     * @param hand        the hand holding the caster
     * @return true to consume the interaction and suppress default caster behavior
     */
    boolean onCasterRightClick(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand);
}
