package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction.Failure;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction.Result;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class ArcaneCraftingTransactions implements ArcaneCraftingTransaction.Bindings {

    @Override
    public Result preview(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        try (Transaction simulation = Transaction.openRoot()) {
            return run(context, player, input, null, simulation);
        }
    }

    @Override
    public ArcaneCraftingTransaction.Inspection inspect(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return ArcaneCraftingTransaction.Inspection.failure(invalid);
        }
        Match match = match(context.level(), player, input);
        if (match.holder() == null) {
            return ArcaneCraftingTransaction.Inspection.failure(match.failure());
        }
        ArcaneCraftingRecipe recipe = match.holder().value();
        ArcaneCraftingTransaction.Requirements requirements = new ArcaneCraftingTransaction.Requirements(recipe.visCost(), recipe.crystalCost(), recipe.placementInfo().ingredients());
        return new ArcaneCraftingTransaction.Inspection(match.failure(), match.holder().id(), recipe.assemble(input), remainders(recipe, input), requirements, recipe.gateStatus(player));
    }

    @Override
    public Result craft(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input, IArcaneCraftingStore store, TransactionContext transaction) {
        return run(context, player, input, store, transaction);
    }

    private static Result run(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input, @Nullable IArcaneCraftingStore store, TransactionContext transaction) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return Result.failure(invalid);
        }
        Match match = match(context.level(), player, input);
        if (match.failure() != Failure.NONE) {
            return Result.failure(match.failure());
        }
        IArcaneRecipe recipe = match.holder().value();
        ItemStack output = recipe.assemble(input);
        List<ItemStack> grid = grid(input);
        List<ItemStack> remainders = remainders(recipe, input);
        try (Transaction craft = Transaction.open(transaction)) {
            WorkbenchPayment.Plan plan = WorkbenchPayment.plan(recipe, input, player, context, craft);
            ItemStack wand = plan.crystalsSatisfied() ? WorkbenchPayment.paidWand(plan, input.wandStack()) : null;
            if (wand == null || !WorkbenchPayment.paySources(plan, context, player, input, craft) || !WorkbenchPayment.payAura(plan, context, player, input, craft)) {
                return new Result(Failure.PAYMENT_UNAVAILABLE, output, remainders, plan.cost());
            }
            if (store != null) {
                if (!store.consume(new IArcaneCraftingStore.Consumption(grid, remainders, plan.crystalsToConsume(), wand), craft)) {
                    return new Result(Failure.INGREDIENTS_CHANGED, output, remainders, plan.cost());
                }
                ItemStack crafted = output.copy();
                new RootCommitJournal(() -> ResearchProgressionEvents.recordCrafted(player, crafted)).updateSnapshots(craft);
            }
            craft.commit();
            return new Result(Failure.NONE, output, remainders, plan.cost());
        }
    }

    private static Failure validate(ArcaneWorkbenchContext context, ServerPlayer player) {
        if (!context.level().getServer().isSameThread()) {
            return Failure.NOT_SERVER_THREAD;
        }
        if (context.level() != player.level() || !context.actingPlayer().equals(player.getUUID())) {
            return Failure.INVALID_CONTEXT;
        }
        return Failure.NONE;
    }

    private static Match match(ServerLevel level, ServerPlayer player, IArcaneCraftingInput input) {
        RecipeHolder<ArcaneCraftingRecipe> locked = null;
        for (RecipeHolder<ArcaneCraftingRecipe> holder : level.recipeAccess().recipeMap().byType(TCRecipeTypes.ARCANE.get())) {
            if (holder.value().matches(input, level)) {
                if (holder.value().doesPassGate(player)) {
                    return new Match(holder, Failure.NONE);
                }
                if (locked == null) {
                    locked = holder;
                }
            }
        }
        return locked == null ? new Match(null, Failure.NO_RECIPE) : new Match(locked, Failure.RESEARCH_LOCKED);
    }

    private static List<ItemStack> grid(IArcaneCraftingInput input) {
        List<ItemStack> grid = new ArrayList<>(input.width() * input.height());
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                grid.add(input.getItem(x, y).copy());
            }
        }
        return grid;
    }

    private static List<ItemStack> remainders(IArcaneRecipe recipe, IArcaneCraftingInput input) {
        List<ItemStack> all = recipe.getRemainingItems(input);
        int size = input.width() * input.height();
        List<ItemStack> remainders = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            remainders.add(i < all.size() ? all.get(i).copy() : ItemStack.EMPTY);
        }
        return remainders;
    }

    private record Match(@Nullable RecipeHolder<ArcaneCraftingRecipe> holder, Failure failure) {
    }
}
