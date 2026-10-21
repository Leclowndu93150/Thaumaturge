package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

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
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("WandAccess");

    private WandAccess() {}

    static Bindings bindings() {
        return BINDING.get();
    }

    /** Returns empty for non-wands, missing parts, unregistered parts and invalid variants. */
    public static Optional<WandSnapshot> inspect(ItemStack wand) {
        return BINDING.get().inspect(wand);
    }

    /** Returns the current effective raw capacity, or empty for unsupported storage or aspects. */
    public static OptionalInt capacityCentivis(ItemStack wand, ResourceKey<IAspect> aspect) {
        return BINDING.get().capacityCentivis(wand, aspect);
    }

    /** Previews bounded insertion without writing the stack or its external backing store. */
    public static Optional<WandChargeQuote> quoteCharge(ServerPlayer player, WandSlot slot, ResourceKey<IAspect> aspect, int maximumCentivis) {
        return BINDING.get().quoteCharge(player, slot, aspect, maximumCentivis);
    }

    /** Joins a source's transaction; zero means rejection and never consumes a source resource. */
    public static int tryCharge(ServerPlayer player, WandSlot slot, WandChargeQuote quote, TransactionContext transaction) {
        return BINDING.get().tryCharge(player, slot, quote, transaction);
    }

    /**
     * Returns the full vis storage of the wand.
     *
     * @param wand the wand stack
     * @return the stored vis, or {@link WandVis#EMPTY} when the stack has none
     */
    public static WandVis getAllVis(ItemStack wand) {
        return BINDING.get().getAllVis(wand);
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
     * non-positive amount removes the aspect entry. The input stack is not modified, but a custom
     * storage override may share external backing between copies. This raw legacy accessor is not
     * a simulation or transaction; use {@link #quoteCharge} and {@link #tryCharge} for safe charging.
     *
     * @param wand     the wand stack
     * @param aspect   the aspect key
     * @param centivis the new centivis amount for the aspect
     * @return a copied stack carrying the updated vis
     */
    public static ItemStack withVis(ItemStack wand, ResourceKey<IAspect> aspect, int centivis) {
        ItemStack copy = wand.copy();
        BINDING.get().setAllVis(copy, getAllVis(wand).with(aspect, centivis));
        return copy;
    }

    /**
     * Binds the implementation. Called once at mod init by Thaumaturge; addons must not call this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /**
     * Implementation hook bound by Thaumaturge at mod init. Addons do not implement this.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        default Optional<WandSnapshot> inspect(ItemStack wand) {
            return Optional.empty();
        }

        default OptionalInt capacityCentivis(ItemStack wand, ResourceKey<IAspect> aspect) {
            return OptionalInt.empty();
        }

        default Optional<WandChargeQuote> quoteCharge(ServerPlayer player, WandSlot slot, ResourceKey<IAspect> aspect, int maximumCentivis) {
            return Optional.empty();
        }

        default int tryCharge(ServerPlayer player, WandSlot slot, WandChargeQuote quote, TransactionContext transaction) {
            return 0;
        }

        default Optional<WandPayment.Quote> quotePayment(ServerPlayer player, WandSlot slot, Map<ResourceKey<IAspect>, Integer> costs, WandPayment.CostKind kind) {
            return Optional.empty();
        }

        default boolean tryPay(ServerPlayer player, WandSlot slot, WandPayment.Quote quote, TransactionContext transaction) {
            return false;
        }

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
