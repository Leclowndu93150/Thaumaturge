package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction.Failure;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction.Result;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public final class ArcaneCraftingTransactions implements ArcaneCraftingTransaction.Bindings {

    @Override
    public Result preview(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        return run(context, player, input, null, true);
    }

    @Override
    public ArcaneCraftingTransaction.Inspection inspect(
            ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return ArcaneCraftingTransaction.Inspection.failure(invalid);
        }
        Match match = match(context.level(), player, input);
        if (match.holder() == null) {
            return ArcaneCraftingTransaction.Inspection.failure(match.failure());
        }
        IArcaneRecipe recipe = match.holder().value();
        ArcaneCraftingTransaction.Requirements requirements = new ArcaneCraftingTransaction.Requirements(
                recipe.getBaseVis(), recipe.getCrystals(), recipe.getIngredients());
        return new ArcaneCraftingTransaction.Inspection(
                match.failure(),
                ResourceKey.create(Registries.RECIPE, match.holder().id()),
                recipe.assemble(input, context.level().registryAccess()),
                remainders(recipe, input),
                requirements,
                recipe.gateStatus(player));
    }

    @Override
    public Result craft(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneCraftingInput input,
            IArcaneCraftingStore store,
            boolean simulate) {
        return run(context, player, input, store, simulate);
    }

    private static Result run(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneCraftingInput input,
            @Nullable IArcaneCraftingStore store,
            boolean simulate) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return Result.failure(invalid);
        }
        Match match = match(context.level(), player, input);
        if (match.failure() != Failure.NONE) {
            return Result.failure(match.failure());
        }
        IArcaneRecipe recipe = match.holder().value();
        ItemStack output = recipe.assemble(input, context.level().registryAccess());
        List<ItemStack> remainders = remainders(recipe, input);
        BlockEntityArcaneWorkbench tile = placedWorkbench(context);
        if (tile != null) {
            tile.refreshAura();
        }
        WorkbenchPayment.Plan plan = WorkbenchPayment.plan(recipe, input, player, context);
        WorkbenchPayment.PaymentReservation payment = WorkbenchPayment.reserve(plan, tile, player, input, context);
        ItemStack wand = payment == null ? null : WorkbenchPayment.paidWand(plan, input.wandStack());
        if (payment == null || wand == null) {
            return new Result(Failure.PAYMENT_UNAVAILABLE, output, remainders, plan.cost());
        }
        if (store == null) {
            return new Result(Failure.NONE, output, remainders, plan.cost());
        }
        IArcaneCraftingStore.Consumption consumption =
                new IArcaneCraftingStore.Consumption(grid(input), remainders, plan.crystalsToConsume(), wand);
        if (!store.consume(consumption, true)) {
            return new Result(Failure.INGREDIENTS_CHANGED, output, remainders, plan.cost());
        }
        if (simulate) {
            return new Result(Failure.NONE, output, remainders, plan.cost());
        }
        WorkbenchPayment.commit(payment, player, input, context);
        if (!store.consume(consumption, false)) {
            Thaumaturge.LOGGER.error(
                    "Arcane craft store at {} changed between its check and the craft; the payment was already made",
                    context.blockPosition().orElse(null));
            return new Result(Failure.INGREDIENTS_CHANGED, output, remainders, plan.cost());
        }
        ResearchProgressionEvents.recordCrafted(player, output);
        return new Result(Failure.NONE, output, remainders, plan.cost());
    }

    private static Failure validate(ArcaneWorkbenchContext context, ServerPlayer player) {
        if (!context.level().getServer().isSameThread()) {
            return Failure.NOT_SERVER_THREAD;
        }
        if (context.level() != player.serverLevel() || !context.actingPlayer().equals(player.getUUID())) {
            return Failure.INVALID_CONTEXT;
        }
        return Failure.NONE;
    }

    private static Match match(ServerLevel level, ServerPlayer player, IArcaneCraftingInput input) {
        RecipeHolder<? extends IArcaneRecipe> locked = null;
        for (RecipeHolder<? extends IArcaneRecipe> holder :
                level.getRecipeManager().getAllRecipesFor(TCRecipeTypes.ARCANE.get())) {
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

    private static @Nullable BlockEntityArcaneWorkbench placedWorkbench(ArcaneWorkbenchContext context) {
        if (context.kind() != ArcaneWorkbenchContext.Kind.PLACED) {
            return null;
        }
        return context.blockPosition()
                .map(context.level()::getBlockEntity)
                .filter(BlockEntityArcaneWorkbench.class::isInstance)
                .map(BlockEntityArcaneWorkbench.class::cast)
                .orElse(null);
    }

    private record Match(@Nullable RecipeHolder<? extends IArcaneRecipe> holder, Failure failure) {}
}
