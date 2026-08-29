package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftCost;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

/** Internal implementation behind the supported transaction facade. */
public final class ArcaneCraftingTransactions {
    private ArcaneCraftingTransactions() {}

    public static ArcaneCraftingTransaction.Inspection inspect(
            ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        ArcaneCraftingTransaction.Failure invalid = validateCall(context, player);
        if (invalid != ArcaneCraftingTransaction.Failure.NONE) {
            return ArcaneCraftingTransaction.Inspection.failure(invalid);
        }
        Match match = match(context, player, input);
        if (match.failure() != ArcaneCraftingTransaction.Failure.NONE) {
            return ArcaneCraftingTransaction.Inspection.failure(match.failure());
        }
        IArcaneRecipe recipe = match.recipe();
        return new ArcaneCraftingTransaction.Inspection(
                true,
                ArcaneCraftingTransaction.Failure.NONE,
                match.holder().id(),
                recipe.assemble(input, context.level().registryAccess()),
                recipe.getRemainingItems(input),
                new ArcaneCraftingTransaction.Requirements(
                        recipe.getBaseVis(), recipe.getCrystals(), recipe.getIngredients()));
    }

    public static ArcaneCraftingTransaction.Result preview(
            ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        ArcaneCraftingTransaction.Failure invalid = validateCall(context, player);
        if (invalid != ArcaneCraftingTransaction.Failure.NONE) {
            return ArcaneCraftingTransaction.Result.failure(invalid);
        }
        Match match = match(context, player, input);
        if (match.failure() != ArcaneCraftingTransaction.Failure.NONE) {
            return ArcaneCraftingTransaction.Result.failure(match.failure());
        }
        IArcaneRecipe recipe = match.recipe();
        WorkbenchPayment.Plan plan = WorkbenchPayment.plan(recipe, input, player, context);
        BlockEntityArcaneWorkbench tile = placedWorkbench(context);
        if (tile != null) tile.refreshAura();
        if (WorkbenchPayment.reserve(plan, tile, player, input, context) == null) {
            return ArcaneCraftingTransaction.Result.failure(ArcaneCraftingTransaction.Failure.PAYMENT_UNAVAILABLE);
        }
        return result(context, recipe, input, plan, false);
    }

    public static ArcaneCraftingTransaction.Result commit(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneCraftingInput input,
            IArcaneCraftingStore store) {
        ArcaneCraftingTransaction.Failure invalid = validateCall(context, player);
        if (invalid != ArcaneCraftingTransaction.Failure.NONE) {
            return ArcaneCraftingTransaction.Result.failure(invalid);
        }
        Match initial = match(context, player, input);
        if (initial.failure() != ArcaneCraftingTransaction.Failure.NONE) {
            return ArcaneCraftingTransaction.Result.failure(initial.failure());
        }
        List<ItemStack> grid = gridSnapshot(input);
        try (IArcaneCraftingStore.Reservation reservation = store.reserve(grid)) {
            if (reservation == null || !reservation.isValid()) {
                return ArcaneCraftingTransaction.Result.failure(ArcaneCraftingTransaction.Failure.INGREDIENTS_CHANGED);
            }
            Match current = match(context, player, input);
            if (current.failure() != ArcaneCraftingTransaction.Failure.NONE
                    || !current.holder().id().equals(initial.holder().id())) {
                return ArcaneCraftingTransaction.Result.failure(ArcaneCraftingTransaction.Failure.INGREDIENTS_CHANGED);
            }
            IArcaneRecipe recipe = current.recipe();
            BlockEntityArcaneWorkbench tile = placedWorkbench(context);
            if (tile != null) tile.refreshAura();
            WorkbenchPayment.Plan plan = WorkbenchPayment.plan(recipe, input, player, context);
            WorkbenchPayment.PaymentReservation payment = WorkbenchPayment.reserve(plan, tile, player, input, context);
            if (!reservation.isValid() || payment == null) {
                return ArcaneCraftingTransaction.Result.failure(ArcaneCraftingTransaction.Failure.PAYMENT_UNAVAILABLE);
            }

            ArcaneCraftingTransaction.Result result = result(context, recipe, input, plan, true);
            WorkbenchPayment.commit(payment, player, input, context);
            reservation.commit(result.output(), result.remainders(), plan.crystalsToConsume());
            ResearchProgressionEvents.recordCrafted(player, result.output());
            return result;
        }
    }

    private static ArcaneCraftingTransaction.Result result(
            ArcaneWorkbenchContext context,
            IArcaneRecipe recipe,
            IArcaneCraftingInput input,
            WorkbenchPayment.Plan plan,
            boolean committed) {
        ItemStack output = recipe.assemble(input, context.level().registryAccess());
        List<ItemStack> remainders = recipe.getRemainingItems(input);
        ArcaneCraftCost cost = new ArcaneCraftCost(
                plan.fullWand(),
                plan.wandCentivis(),
                plan.crystalsToConsume(),
                plan.auraVis(),
                plan.crystalsSatisfied());
        return new ArcaneCraftingTransaction.Result(
                true, committed, ArcaneCraftingTransaction.Failure.NONE, output, remainders, cost);
    }

    private static ArcaneCraftingTransaction.Failure validateCall(ArcaneWorkbenchContext context, ServerPlayer player) {
        if (!context.level().getServer().isSameThread()) {
            return ArcaneCraftingTransaction.Failure.NOT_SERVER_THREAD;
        }
        if (context.level() != player.serverLevel() || !context.actingPlayer().equals(player.getUUID())) {
            return ArcaneCraftingTransaction.Failure.INVALID_CONTEXT;
        }
        return ArcaneCraftingTransaction.Failure.NONE;
    }

    private static Match match(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        RecipeHolder<? extends IArcaneRecipe> holder =
                context.level().getRecipeManager().getAllRecipesFor(TCRecipeTypes.ARCANE.get()).stream()
                        .filter(candidate -> candidate.value().matches(input, context.level()))
                        .findFirst()
                        .orElse(null);
        if (holder == null) return new Match(null, ArcaneCraftingTransaction.Failure.NO_RECIPE);
        if (!holder.value().doesPassGate(player)) {
            return new Match(null, ArcaneCraftingTransaction.Failure.RESEARCH_LOCKED);
        }
        return new Match(holder, ArcaneCraftingTransaction.Failure.NONE);
    }

    private static List<ItemStack> gridSnapshot(IArcaneCraftingInput input) {
        return input.items().subList(0, input.width() * input.height()).stream()
                .map(ItemStack::copy)
                .toList();
    }

    private static @Nullable BlockEntityArcaneWorkbench placedWorkbench(ArcaneWorkbenchContext context) {
        if (context.kind() != ArcaneWorkbenchContext.Kind.PLACED) return null;
        return context.blockPosition()
                .map(context.level()::getBlockEntity)
                .filter(BlockEntityArcaneWorkbench.class::isInstance)
                .map(BlockEntityArcaneWorkbench.class::cast)
                .orElse(null);
    }

    private record Match(
            @Nullable RecipeHolder<? extends IArcaneRecipe> holder, ArcaneCraftingTransaction.Failure failure) {
        private @Nullable IArcaneRecipe recipe() {
            return holder == null ? null : holder.value();
        }
    }
}
