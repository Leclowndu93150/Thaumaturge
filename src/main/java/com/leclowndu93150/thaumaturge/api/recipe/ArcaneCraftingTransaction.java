package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.workbench.ArcaneCraftingTransactions;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Server-thread entry point for previewing and atomically executing arcane recipes. */
public final class ArcaneCraftingTransaction {
    private ArcaneCraftingTransaction() {}

    /**
     * Produces an authoritative preview without reserving, consuming, or changing any resource.
     * Callers must still handle a failure from {@link #commit} because resources may change.
     */
    public static Result preview(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        return ArcaneCraftingTransactions.preview(context, player, input);
    }

    /**
     * Executes one craft. The store is reserved before payment is revalidated; any failure closes
     * that reservation without consuming ingredients or payment.
     */
    public static Result commit(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneCraftingInput input,
            IArcaneCraftingStore store) {
        Objects.requireNonNull(store, "store");
        return ArcaneCraftingTransactions.commit(context, player, input, store);
    }

    /** Immutable outcome of a preview or commit. Returned stacks and lists are defensive copies. */
    public record Result(
            boolean successful,
            boolean committed,
            Failure failure,
            ItemStack output,
            List<ItemStack> remainders,
            @Nullable ArcaneCraftCost cost) {
        public Result {
            Objects.requireNonNull(failure, "failure");
            output = output.copy();
            remainders = remainders.stream().map(ItemStack::copy).toList();
        }

        public static Result failure(Failure failure) {
            return new Result(false, false, failure, ItemStack.EMPTY, List.of(), null);
        }

        @Override
        public ItemStack output() {
            return output.copy();
        }

        @Override
        public List<ItemStack> remainders() {
            return remainders.stream().map(ItemStack::copy).toList();
        }

        public AspectList crystals() {
            return cost == null ? AspectList.EMPTY : cost.crystalsNeeded();
        }
    }

    public enum Failure {
        NONE,
        NOT_SERVER_THREAD,
        INVALID_CONTEXT,
        NO_RECIPE,
        RESEARCH_LOCKED,
        INGREDIENTS_CHANGED,
        PAYMENT_UNAVAILABLE
    }
}
