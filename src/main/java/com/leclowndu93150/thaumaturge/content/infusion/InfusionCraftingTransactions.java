package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.Failure;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.Inspection;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public final class InfusionCraftingTransactions implements InfusionCraftingTransaction.Bindings {

    @Override
    public Inspection inspect(
            InfusionMatrixContext context, ServerPlayer player, ItemStack catalyst, List<ItemStack> components) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return Inspection.failure(invalid);
        }
        BlockEntityInfusionMatrix matrix = matrix(context);
        if (matrix == null) {
            return Inspection.failure(Failure.MATRIX_UNAVAILABLE);
        }
        List<ItemStack> singles =
                components.stream().map(stack -> stack.copyWithCount(1)).toList();
        return inspect(matrix, context.level(), player, new InfusionInput(catalyst.copyWithCount(1), singles));
    }

    @Override
    public Failure start(InfusionMatrixContext context, ServerPlayer player, ResourceKey<Recipe<?>> expectedRecipeId) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return invalid;
        }
        BlockEntityInfusionMatrix matrix = matrix(context);
        if (matrix == null) {
            return Failure.MATRIX_UNAVAILABLE;
        }
        if (!matrix.isActive()) {
            return Failure.MATRIX_INACTIVE;
        }
        if (matrix.isCrafting()) {
            return Failure.MATRIX_BUSY;
        }
        Inspection inspection = inspect(matrix, context.level(), player, matrix.stagedInput(context.level()));
        if (!inspection.successful()) {
            return inspection.failure();
        }
        if (!expectedRecipeId.equals(inspection.recipeId())) {
            return Failure.INGREDIENTS_CHANGED;
        }
        return matrix.tryStartCraft(context.level(), player) ? Failure.NONE : Failure.NOT_STARTED;
    }

    private static Inspection inspect(
            BlockEntityInfusionMatrix matrix, ServerLevel level, ServerPlayer player, InfusionInput input) {
        InfusionRecipeMatcher.Match match = InfusionRecipeMatcher.find(level, player, input);
        if (match == null) {
            return Inspection.failure(Failure.NO_RECIPE);
        }
        InfusionJobRecipe recipe = match.recipe();
        ItemStack output = BlockEntityInfusionMatrix.preserveCatalystDamage(
                recipe.assemble(input, level.registryAccess()), input.catalyst());
        return new Inspection(
                match.locked() ? Failure.RESEARCH_LOCKED : Failure.NONE,
                ResourceKey.create(Registries.RECIPE, match.holder().id()),
                matrix.projectedCost(level, recipe.jobEssentia(input)),
                recipe.jobInstability(input),
                recipe.gateStatus(player),
                output,
                remainders(recipe.jobComponents(input)),
                recipe.exactResult());
    }

    private static Failure validate(InfusionMatrixContext context, ServerPlayer player) {
        if (!context.level().getServer().isSameThread()) {
            return Failure.NOT_SERVER_THREAD;
        }
        if (context.level() != player.level() || !context.actingPlayer().equals(player.getUUID())) {
            return Failure.INVALID_CONTEXT;
        }
        return Failure.NONE;
    }

    private static @Nullable BlockEntityInfusionMatrix matrix(InfusionMatrixContext context) {
        if (!MatrixEnvironment.validLocation(context.level(), context.position())) {
            return null;
        }
        return context.level().getBlockEntity(context.position()) instanceof BlockEntityInfusionMatrix matrix
                ? matrix
                : null;
    }

    private static List<ItemStack> remainders(@Nullable List<ItemStack> consumed) {
        List<ItemStack> remainders = new ArrayList<>();
        if (consumed == null) {
            return remainders;
        }
        for (ItemStack stack : consumed) {
            ItemStack remainder = stack.getCraftingRemainingItem();
            remainders.add(remainder == null ? ItemStack.EMPTY : remainder.copy());
        }
        return remainders;
    }
}
