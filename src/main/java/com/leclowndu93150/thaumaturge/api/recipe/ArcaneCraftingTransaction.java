package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

/**
 * Runs arcane crafts for workbenches other than Thaumaturge's own, such as crafting terminals and
 * autocrafters. A craft pays wand vis, external vis sources, crystals and aura, and consumes the
 * grid. Every part is checked with a simulated pass before anything is changed, so a craft that
 * cannot complete changes nothing.
 *
 * <p>All methods are server side and must run on the server thread. The implementation is bound
 * at mod init by Thaumaturge via {@link #bind(Bindings)}; addons must not call {@code bind}.
 *
 * @since 1.0.0
 */
public final class ArcaneCraftingTransaction {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("ArcaneCraftingTransaction");

    private ArcaneCraftingTransaction() {}

    /**
     * Reports whether the input can be crafted right now and at what cost, without changing
     * anything. The payment is simulated but not made, and no store is consulted.
     *
     * @param context where and for whom the craft runs
     * @param player  the crafting player
     * @param input   the grid, crystals and wand to craft from
     * @return the outcome the craft would have; {@link Result#output()} is what it would produce
     * @throws IllegalStateException when called before the implementation has bound the facade
     */
    public static Result preview(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        return BINDING.get().preview(context, player, input);
    }

    /**
     * Matches the input against the arcane recipes and reports what the matched recipe needs,
     * without planning or paying anything. Nothing is changed and nothing is recorded, so it is
     * safe to call at any time on the server thread.
     *
     * <p>A recipe the player has not unlocked is still reported, with
     * {@link Failure#RESEARCH_LOCKED} and {@link ResearchGateStatus#LOCKED}, so a terminal can show
     * what is missing. Whether the craft is affordable is answered by {@link #preview}.
     *
     * @param context where and for whom the craft runs
     * @param player  the crafting player
     * @param input   the grid, crystals and wand to match
     * @return the matched recipe's details, or a failed inspection when nothing matches
     * @throws IllegalStateException when called before the implementation has bound the facade
     */
    public static Inspection inspect(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        return BINDING.get().inspect(context, player, input);
    }

    /**
     * Crafts the input, or checks that it could be crafted.
     *
     * <p>The recipe is matched against {@code input}, the cost is planned, and {@code store} is asked
     * whether it can consume the grid, crystals and wand vis. With {@code simulate} true nothing is
     * changed and the result reports whether the craft would succeed. With {@code simulate} false
     * the payment and the store's consumption are both simulated first; only when both succeed is
     * the cost paid and the store told to consume, so a failed craft changes nothing. The caller is
     * responsible for placing {@link Result#output()}; check that it fits with a simulated craft
     * before crafting for real.
     *
     * @param context  where and for whom the craft runs
     * @param player   the crafting player
     * @param input    the grid, crystals and wand to craft from, read from {@code store}
     * @param store    the storage the input was read from
     * @param simulate true to check the craft without changing anything
     * @return the outcome; {@link Result#output()} holds the crafted item on success
     * @throws IllegalStateException when called before the implementation has bound the facade
     */
    public static Result craft(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneCraftingInput input,
            IArcaneCraftingStore store,
            boolean simulate) {
        Objects.requireNonNull(store, "store");
        return BINDING.get().craft(context, player, input, store, simulate);
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
     * The outcome of a preview or a craft.
     *
     * @param failure    why the craft cannot happen, or {@link Failure#NONE}
     * @param output     the crafted item; empty when no recipe matched
     * @param remainders the item left at each grid position, indexed like
     *                   {@link IArcaneCraftingStore.Consumption#grid()}
     * @param cost       the resolved cost, or null when the craft failed before the cost was
     *                   planned
     * @since 1.0.0
     */
    public record Result(
            Failure failure,
            ItemStack output,
            List<ItemStack> remainders,
            @Nullable ArcaneCraftCost cost) {
        /**
         * Copies every stack so the record owns them.
         *
         * @throws NullPointerException when {@code failure} is null
         */
        public Result {
            Objects.requireNonNull(failure, "failure");
            output = output.copy();
            remainders = remainders.stream().map(ItemStack::copy).toList();
        }

        /**
         * Creates a result for a craft that failed before a recipe or cost was known.
         *
         * @param failure the reason
         * @return the result
         */
        public static Result failure(Failure failure) {
            return new Result(failure, ItemStack.EMPTY, List.of(), null);
        }

        /**
         * Whether the craft can happen, or happened.
         *
         * @return true when {@link #failure()} is {@link Failure#NONE}
         */
        public boolean successful() {
            return failure == Failure.NONE;
        }

        /**
         * The crafted item.
         *
         * @return a copy of the output
         */
        @Override
        public ItemStack output() {
            return output.copy();
        }

        /**
         * The remainders left in the grid.
         *
         * @return copies of the remainders
         */
        @Override
        public List<ItemStack> remainders() {
            return remainders.stream().map(ItemStack::copy).toList();
        }

        /**
         * The crystals the craft consumes.
         *
         * @return the crystal aspects, or {@link AspectList#EMPTY} when no cost was planned
         */
        public AspectList crystals() {
            return cost == null ? AspectList.EMPTY : cost.crystalsNeeded();
        }
    }

    /**
     * What {@link #inspect} found for an input.
     *
     * @param failure        {@link Failure#NONE} when a recipe matches and the player may craft
     *                       it, {@link Failure#RESEARCH_LOCKED} when the only matching recipes are
     *                       locked, or the reason no recipe was evaluated
     * @param recipeId       the matched recipe, or null when none matched
     * @param output         what the recipe produces from the input; empty when none matched
     * @param remainders     the item left at each grid position, indexed like
     *                       {@link IArcaneCraftingStore.Consumption#grid()}
     * @param requirements   the matched recipe's cost and ingredients, or null when none matched
     * @param researchStatus the player's standing against the recipe's research gate, or null when
     *                       none matched
     * @since 1.0.0
     */
    public record Inspection(
            Failure failure,
            @Nullable ResourceKey<Recipe<?>> recipeId,
            ItemStack output,
            List<ItemStack> remainders,
            @Nullable Requirements requirements,
            @Nullable ResearchGateStatus researchStatus) {
        /**
         * Copies every stack so the record owns them.
         *
         * @throws NullPointerException when {@code failure} is null
         */
        public Inspection {
            Objects.requireNonNull(failure, "failure");
            output = output.copy();
            remainders = remainders.stream().map(ItemStack::copy).toList();
        }

        /**
         * Creates an inspection for an input no recipe was evaluated for.
         *
         * @param failure the reason
         * @return the inspection
         */
        public static Inspection failure(Failure failure) {
            return new Inspection(failure, null, ItemStack.EMPTY, List.of(), null, null);
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
         * What the recipe produces from the input.
         *
         * @return a copy of the output
         */
        @Override
        public ItemStack output() {
            return output.copy();
        }

        /**
         * The remainders the recipe leaves in the grid.
         *
         * @return copies of the remainders
         */
        @Override
        public List<ItemStack> remainders() {
            return remainders.stream().map(ItemStack::copy).toList();
        }
    }

    /**
     * The fixed requirements of an arcane recipe, before any wand, source or gear discount.
     *
     * @param baseVis     the recipe's base aura vis cost
     * @param crystals    the primal crystal cost, which a wand or vis sources can pay instead
     * @param ingredients the recipe's ingredients, as given by {@link Recipe#getIngredients()}
     * @since 1.0.0
     */
    public record Requirements(int baseVis, AspectList crystals, List<Ingredient> ingredients) {
        /**
         * Copies the ingredient list.
         *
         * @throws NullPointerException when {@code crystals} is null
         */
        public Requirements {
            Objects.requireNonNull(crystals, "crystals");
            ingredients = List.copyOf(ingredients);
        }
    }

    /**
     * Why a craft cannot happen.
     *
     * @since 1.0.0
     */
    public enum Failure {
        /** The craft can happen, or happened. */
        NONE,
        /** The call was not made on the server thread. */
        NOT_SERVER_THREAD,
        /** The context's level or acting player does not match the player. */
        INVALID_CONTEXT,
        /** No arcane recipe matches the grid. */
        NO_RECIPE,
        /** A recipe matches, but the player has not unlocked it. */
        RESEARCH_LOCKED,
        /** The store no longer matched the input when the craft tried to consume it. */
        INGREDIENTS_CHANGED,
        /** The wand, sources, crystals or aura cannot pay the cost. */
        PAYMENT_UNAVAILABLE
    }

    /**
     * Implementation hook supplied by Thaumaturge. Each method corresponds to a public static on
     * {@link ArcaneCraftingTransaction}. Addons must not implement this interface.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * Implements {@link ArcaneCraftingTransaction#preview}.
         *
         * @param context where and for whom the craft runs
         * @param player  the crafting player
         * @param input   the grid, crystals and wand to craft from
         * @return the outcome
         */
        Result preview(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input);

        /**
         * Implements {@link ArcaneCraftingTransaction#inspect}.
         *
         * @param context where and for whom the craft runs
         * @param player  the crafting player
         * @param input   the grid, crystals and wand to match
         * @return the inspection
         */
        Inspection inspect(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input);

        /**
         * Implements {@link ArcaneCraftingTransaction#craft}.
         *
         * @param context  where and for whom the craft runs
         * @param player   the crafting player
         * @param input    the grid, crystals and wand to craft from
         * @param store    the storage the input was read from
         * @param simulate true to check the craft without changing anything
         * @return the outcome
         */
        Result craft(
                ArcaneWorkbenchContext context,
                ServerPlayer player,
                IArcaneCraftingInput input,
                IArcaneCraftingStore store,
                boolean simulate);
    }
}
