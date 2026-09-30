package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

/**
 * Static read and write access to the vis stored on a wand stack.
 *
 * <p>Wand vis is stored in the {@code thaumaturge:wand_vis} data component as a {@link WandVis}
 * record, unless the wand's rod declares an {@link IWandVisStorage}, in which case these helpers read
 * and write through that storage instead. Amounts are in centivis (one hundred per vis unit).
 *
 * <p>Writes here store raw amounts; they do not apply the consumption discounts that the wand's
 * caps and the player's gear grant during casting or crafting. They are the low-level storage
 * accessor, suited to relays, chargers, and inspection tools.
 *
 * <p>The implementation is bound once at mod init by Thaumaturge via {@link #bind(Bindings)}; addons
 * must not call {@code bind}.
 *
 * @since 1.0.0
 */
public final class WandAccess {
    private static Bindings impl;

    private WandAccess() {}

    /**
     * Returns the full vis storage of the wand.
     *
     * @param wand the wand stack
     * @return the stored vis, or {@link WandVis#EMPTY} when the stack has none
     */
    public static WandVis getAllVis(ItemStack wand) {
        return bindingOrThrow().getAllVis(wand);
    }

    /**
     * Returns the centivis of a single aspect stored on the wand.
     *
     * @param wand   the wand stack
     * @param aspect the aspect key
     * @return the centivis amount, or zero when absent
     */
    public static int getVis(ItemStack wand, ResourceKey<IAspect> aspect) {
        return getAllVis(wand).amount(aspect);
    }

    /**
     * Returns a copy of the wand stack with the given aspect set to {@code centivis}. Setting a
     * non-positive amount removes the aspect entry. The input stack is not modified.
     *
     * @param wand     the wand stack
     * @param aspect   the aspect key
     * @param centivis the new centivis amount for the aspect
     * @return a copied stack carrying the updated vis
     */
    public static ItemStack withVis(ItemStack wand, ResourceKey<IAspect> aspect, int centivis) {
        ItemStack copy = wand.copy();
        bindingOrThrow().setAllVis(copy, getAllVis(wand).with(aspect, centivis));
        return copy;
    }

    /**
     * Binds the implementation. Called once at mod init by Thaumaturge; addons must not call this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        if (impl != null) {
            throw new IllegalStateException("WandAccess already bound");
        }
        impl = bindings;
    }

    private static Bindings bindingOrThrow() {
        if (impl == null) {
            throw new IllegalStateException("WandAccess accessed before binding");
        }
        return impl;
    }

    /**
     * Implementation hook bound by Thaumaturge at mod init. Addons do not implement this.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * Reads the wand's vis, honoring the rod's {@link IWandVisStorage}.
         *
         * @param wand the wand stack
         * @return the stored vis, never null
         */
        WandVis getAllVis(ItemStack wand);

        /**
         * Replaces the wand's vis in place, honoring the rod's {@link IWandVisStorage}.
         *
         * @param wand the wand stack
         * @param vis  the new vis storage
         */
        void setAllVis(ItemStack wand, WandVis vis);
    }
}
