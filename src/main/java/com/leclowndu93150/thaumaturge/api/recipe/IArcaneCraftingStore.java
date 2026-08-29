package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Transactional item storage used by {@link ArcaneCraftingTransaction}.
 *
 * <p>{@link #reserve(List)} must atomically verify and reserve the supplied grid snapshot. While a
 * reservation is live, its ingredients cannot be consumed by another operation. A successful
 * {@link Reservation#commit(ItemStack, List)} must consume exactly one item from each occupied
 * recipe slot and insert the output and remainders exactly once. Implementations must make commit
 * infallible after reservation, or provide their own rollback before returning from it.
 *
 * <p>All methods are called on the server thread. Stacks passed to or returned from this contract
 * must be treated as values and not retained for later mutation.
 *
 * @since 0.3.2
 */
public interface IArcaneCraftingStore {
    /** Returns a reservation, or {@code null} when the snapshot is no longer available. */
    @Nullable
    Reservation reserve(List<ItemStack> gridSnapshot);

    interface Reservation extends AutoCloseable {
        /** True while the originally reserved items remain available and unchanged. */
        boolean isValid();

        /** Completes the item side of the transaction exactly once. */
        void commit(ItemStack output, List<ItemStack> remainders, AspectList crystals);

        /** Releases an uncommitted reservation without changing stored items. */
        @Override
        void close();
    }
}
