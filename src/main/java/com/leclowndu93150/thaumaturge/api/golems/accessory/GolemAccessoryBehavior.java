package com.leclowndu93150.thaumaturge.api.golems.accessory;

import net.minecraft.world.item.ItemStack;

/** Optional server-only lifecycle behavior owned by a {@link GolemAccessory}. */
public interface GolemAccessoryBehavior {
    /** Called exactly once after attachment. The supplied stack is a one-item copy. */
    default void onAttach(GolemAccessoryContext context, ItemStack attachedStack) {}

    /** Called exactly once before return/drop; changes to the one-item stack are retained. */
    default void onRemove(GolemAccessoryContext context, ItemStack returnedStack) {}

    /** Called once per loaded server tick and never on the client. */
    default void serverTick(GolemAccessoryContext context) {}
}
