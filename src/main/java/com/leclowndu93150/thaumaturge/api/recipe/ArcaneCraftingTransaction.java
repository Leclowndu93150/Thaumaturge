package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.workbench.ArcaneCraftingTransactions;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
     * Authoritatively matches an arcane recipe without checking or reserving payment. Inspection
     * never crafts, mutates resources, or records progression; {@link #commit} remains mandatory.
     */
    public static Inspection inspect(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        return ArcaneCraftingTransactions.inspect(context, player, input);
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

        /** Present for evaluated previews and successful commits; raw inspection has no payment context. */
        public @Nullable ArcanePaymentSummary paymentSummary() {
            return cost == null ? null : ArcanePaymentSummary.from(cost);
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

    /**
     * Immutable, reload-safe display snapshot returned by {@link #inspect}. It is descriptive only:
     * recipe reloads and resources are authoritatively revalidated by {@link #commit}.
     */
    public record Inspection(
            boolean successful,
            Failure failure,
            @Nullable ResourceLocation recipeId,
            ItemStack output,
            List<ItemStack> remainders,
            @Nullable Requirements requirements,
            ResearchStatus researchStatus) {
        public Inspection {
            Objects.requireNonNull(failure, "failure");
            Objects.requireNonNull(researchStatus, "researchStatus");
            output = output.copy();
            remainders = remainders.stream().map(ItemStack::copy).toList();
        }

        /** Retained for source and binary compatibility with the original inspection shape. */
        public Inspection(
                boolean successful,
                Failure failure,
                @Nullable ResourceLocation recipeId,
                ItemStack output,
                List<ItemStack> remainders,
                @Nullable Requirements requirements) {
            this(
                    successful,
                    failure,
                    recipeId,
                    output,
                    remainders,
                    requirements,
                    successful ? ResearchStatus.UNLOCKED : ResearchStatus.INVALID);
        }

        public static Inspection failure(Failure failure) {
            ResearchStatus status = failure == Failure.RESEARCH_LOCKED ? ResearchStatus.LOCKED : ResearchStatus.INVALID;
            return new Inspection(false, failure, null, ItemStack.EMPTY, List.of(), null, status);
        }

        @Override
        public ItemStack output() {
            return output.copy();
        }

        @Override
        public List<ItemStack> remainders() {
            return remainders.stream().map(ItemStack::copy).toList();
        }
    }

    /** Raw recipe requirements suitable for a Knowledge Core snapshot. */
    public record Requirements(int baseVis, AspectList crystals, List<Ingredient> ingredients) {
        public Requirements {
            Objects.requireNonNull(crystals, "crystals");
            ingredients = List.copyOf(ingredients);
        }
    }
}
