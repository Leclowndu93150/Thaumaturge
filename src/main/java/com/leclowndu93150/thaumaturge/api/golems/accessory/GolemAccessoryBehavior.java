package com.leclowndu93150.thaumaturge.api.golems.accessory;

import net.minecraft.world.item.ItemStack;

/**
 * Optional server-authoritative lifecycle behavior owned by a {@link GolemAccessory}.
 *
 * <p>Callbacks run only on the server thread. Loading or unloading a golem does not represent an
 * attachment change and therefore does not invoke either attachment callback: persisted state is
 * restored on load and retained across an ordinary unload. Death, pickup, or explicit accessory
 * removal invokes {@link #onRemove} before the accessory's namespace is cleared.
 */
public interface GolemAccessoryBehavior {
    /** Called exactly once after attachment. The supplied stack is a one-item copy. */
    default void onAttach(GolemAccessoryContext context, ItemStack attachedStack) {}

    /**
     * Called exactly once before return/drop; changes to the stack are retained, but its count is
     * bounded to one by Thaumaturge.
     */
    default void onRemove(GolemAccessoryContext context, ItemStack returnedStack) {}

    /** Called once per loaded server tick and never on the client. */
    default void serverTick(GolemAccessoryContext context) {}
}
