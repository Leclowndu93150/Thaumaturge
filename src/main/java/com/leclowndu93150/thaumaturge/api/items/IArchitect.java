package com.leclowndu93150.thaumaturge.api.items;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * An item that previews a set of block positions it is about to affect, rendered client-side
 * as a white grid overlay while the item is held.
 *
 * <p>Implemented by the caster's gauntlet, which delegates to an architect-capable focus medium
 * such as Plan.
 *
 * @since 1.0.0
 */
public interface IArchitect {
    /**
     * Performs the block ray trace this architect uses to anchor its preview.
     *
     * @param stack  the architect item stack
     * @param level  the level of the holder
     * @param caster the entity holding the item
     * @return the trace result, or null when the item cannot anchor a preview right now
     */
    @Nullable
    HitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity caster);

    /**
     * Whether the overlay renderer should replace the vanilla block highlight instead of
     * drawing during the level render pass.
     *
     * @param stack the architect item stack
     * @return true to draw in place of the vanilla block outline
     */
    boolean useBlockHighlight(ItemStack stack);

    /**
     * The block positions the item would currently affect, anchored at the traced block.
     *
     * @param stack  the architect item stack
     * @param level  the level of the holder
     * @param pos    the traced block position
     * @param side   the traced block face
     * @param player the player holding the item
     * @return the affected positions; empty when nothing would be affected
     */
    List<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player);

    /**
     * Whether the axis indicator for the given axis should render at the anchor block.
     *
     * @param stack  the architect item stack
     * @param level  the level of the holder
     * @param player the player holding the item
     * @param side   the traced block face
     * @param axis   the axis being queried
     * @return true to render the indicator for that axis
     */
    boolean showAxis(ItemStack stack, Level level, Player player, Direction side, EnumAxis axis);

    /**
     * Axes an architect preview can extend along.
     *
     * @since 1.0.0
     */
    enum EnumAxis {
        X, Y, Z
    }
}
