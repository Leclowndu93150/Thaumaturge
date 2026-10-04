package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

/**
 * Inspects and starts infusion crafts on a real infusion matrix, for addons such as autocrafters
 * that stage the catalyst and components on the pedestals themselves.
 *
 * <p>Infusion is not a single-step craft: once started, the matrix drains essentia and pulls the
 * components over many ticks and can still fail from instability. This facade therefore offers a
 * read-only {@link #inspect} and a guarded {@link #start}, and leaves the craft itself to the
 * matrix.
 *
 * <p>All methods are server side and must run on the server thread. The implementation is bound
 * at mod init by Thaumaturge via {@link #bind(Bindings)}; addons must not call {@code bind}.
 *
 * @since 1.0.0
 */
public final class InfusionCraftingTransaction {
    private static Bindings impl;

    private InfusionCraftingTransaction() {}

    /**
     * Reports which infusion recipe a catalyst and components would craft at the matrix, and what
     * it would need, without changing anything.
     *
     * <p>Recipe types are tried in the order the matrix uses: plain infusion, then infusion
     * enchantment, then runic augmentation. A recipe the player has not unlocked is still reported,
     * with {@link Failure#RESEARCH_LOCKED} and {@link ResearchGateStatus#LOCKED}, when no unlocked
     * recipe matches. Each stack is read as a single item.
     *
     * @param context    the matrix and acting player
     * @param player     the acting player
     * @param catalyst   the item for the central pedestal
     * @param components the items for the surrounding pedestals, in any order
     * @return the matched recipe's details, or a failed inspection
     * @throws NullPointerException  when {@code catalyst} or {@code components} is null
     * @throws IllegalStateException when called before the implementation has bound the facade
     */
    public static Inspection inspect(
            InfusionMatrixContext context, ServerPlayer player, ItemStack catalyst, List<ItemStack> components) {
        Objects.requireNonNull(catalyst, "catalyst");
        Objects.requireNonNull(components, "components");
        return bindingOrThrow().inspect(context, player, catalyst, components);
    }

    /**
     * Starts the craft already staged on the matrix's pedestals, but only when it still resolves
     * to {@code expectedRecipeId}. The start behaves like the player activating the matrix with a
     * wand: the matrix must already be active and idle, the surroundings are surveyed again, and
     * the essentia cost is scaled by the altar's current setup.
     *
     * @param context          the matrix and acting player
     * @param player           the acting player, recorded as the crafter
     * @param expectedRecipeId the recipe an earlier {@link #inspect} reported
     * @return {@link Failure#NONE} when the matrix started crafting, or the reason it did not
     * @throws NullPointerException  when {@code expectedRecipeId} is null
     * @throws IllegalStateException when called before the implementation has bound the facade
     */
    public static Failure start(
            InfusionMatrixContext context, ServerPlayer player, ResourceKey<Recipe<?>> expectedRecipeId) {
        Objects.requireNonNull(expectedRecipeId, "expectedRecipeId");
        return bindingOrThrow().start(context, player, expectedRecipeId);
    }

    /**
     * Binds the implementation. Called once at mod init by Thaumaturge; addons must not call this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        if (impl != null) {
            throw new IllegalStateException("InfusionCraftingTransaction already bound");
        }
        impl = bindings;
    }

    private static Bindings bindingOrThrow() {
        if (impl == null) {
            throw new IllegalStateException("InfusionCraftingTransaction accessed before binding");
        }
        return impl;
    }

    /**
     * Why an infusion cannot be inspected or started.
     *
     * @since 1.0.0
     */
    public enum Failure {
        /** The recipe matched and the player may craft it, or the craft started. */
        NONE,
        /** The call was not made on the server thread. */
        NOT_SERVER_THREAD,
        /** The context's level or acting player does not match the player. */
        INVALID_CONTEXT,
        /** There is no infusion matrix at the position, or its altar structure is incomplete. */
        MATRIX_UNAVAILABLE,
        /** The matrix has not been activated with a wand yet. */
        MATRIX_INACTIVE,
        /** The matrix is already running a craft. */
        MATRIX_BUSY,
        /** No infusion recipe matches the catalyst and components. */
        NO_RECIPE,
        /** A recipe matches, but the player has not unlocked it. */
        RESEARCH_LOCKED,
        /** The staged items no longer resolve to the expected recipe. */
        INGREDIENTS_CHANGED,
        /** The recipe still matched but the matrix did not start the craft. */
        NOT_STARTED
    }

    /**
     * What {@link #inspect} found.
     *
     * @param failure             {@link Failure#NONE} when a recipe matches and the player may
     *                            craft it, {@link Failure#RESEARCH_LOCKED} when the only matching
     *                            recipes are locked, or the reason no recipe was evaluated
     * @param recipeId            the matched recipe, or null when none matched
     * @param essentia            the essentia the craft drains, already scaled by the matrix's
     *                            current altar setup; empty when none matched
     * @param instability         the recipe's instability for this catalyst; zero when none matched
     * @param researchStatus      the player's standing against the recipe's research gate, or null
     *                            when none matched
     * @param output              the item the central pedestal holds when the craft finishes; empty
     *                            when none matched
     * @param componentRemainders what each consumed component leaves on its pedestal, in the order
     *                            the recipe consumes them
     * @param exactOutput         false when the finished item can differ from {@code output}, as
     *                            with an infusion enchantment that may add warp
     * @since 1.0.0
     */
    public record Inspection(
            Failure failure,
            @Nullable ResourceKey<Recipe<?>> recipeId,
            AspectList essentia,
            int instability,
            @Nullable ResearchGateStatus researchStatus,
            ItemStack output,
            List<ItemStack> componentRemainders,
            boolean exactOutput) {
        /**
         * Copies every stack so the record owns them.
         *
         * @throws NullPointerException     when {@code failure} or {@code essentia} is null
         * @throws IllegalArgumentException when {@code instability} is negative
         */
        public Inspection {
            Objects.requireNonNull(failure, "failure");
            Objects.requireNonNull(essentia, "essentia");
            if (instability < 0) {
                throw new IllegalArgumentException("instability must not be negative");
            }
            output = output.copy();
            componentRemainders =
                    componentRemainders.stream().map(ItemStack::copy).toList();
        }

        /**
         * Creates an inspection for an input no recipe was evaluated for.
         *
         * @param failure the reason
         * @return the inspection
         */
        public static Inspection failure(Failure failure) {
            return new Inspection(failure, null, AspectList.EMPTY, 0, null, ItemStack.EMPTY, List.of(), false);
        }

        /**
         * Whether a recipe matched and the player may craft it.
         *
         * @return true when {@link #failure()} is {@link Failure#NONE}
         */
        public boolean successful() {
            return failure == Failure.NONE;
        }

        /**
         * The item the craft produces.
         *
         * @return a copy of the output
         */
        @Override
        public ItemStack output() {
            return output.copy();
        }

        /**
         * What the consumed components leave behind.
         *
         * @return copies of the remainders
         */
        @Override
        public List<ItemStack> componentRemainders() {
            return componentRemainders.stream().map(ItemStack::copy).toList();
        }
    }

    /**
     * Implementation hook supplied by Thaumaturge. Each method corresponds to a public static on
     * {@link InfusionCraftingTransaction}. Addons must not implement this interface.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * Implements {@link InfusionCraftingTransaction#inspect}.
         *
         * @param context    the matrix and acting player
         * @param player     the acting player
         * @param catalyst   the catalyst
         * @param components the components
         * @return the inspection
         */
        Inspection inspect(
                InfusionMatrixContext context, ServerPlayer player, ItemStack catalyst, List<ItemStack> components);

        /**
         * Implements {@link InfusionCraftingTransaction#start}.
         *
         * @param context          the matrix and acting player
         * @param player           the acting player
         * @param expectedRecipeId the expected recipe
         * @return the outcome
         */
        Failure start(InfusionMatrixContext context, ServerPlayer player, ResourceKey<Recipe<?>> expectedRecipeId);
    }
}
