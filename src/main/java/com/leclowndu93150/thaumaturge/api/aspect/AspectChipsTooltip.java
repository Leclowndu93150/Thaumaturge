package com.leclowndu93150.thaumaturge.api.aspect;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

/**
 * Common-side tooltip carrier for a row of aspect chips.
 *
 * <p>Injected into the tooltip element list by Thaumaturge's tooltip handler when an item has known
 * aspects and the player meets the visibility conditions (Shift modifier and a container screen
 * open by default). The client-side renderer is registered separately and draws sprite-based chips.
 *
 * @param aspects the aspect list to display
 * @since 1.0.0
 */
public record AspectChipsTooltip(AspectList aspects) implements TooltipComponent {
}
