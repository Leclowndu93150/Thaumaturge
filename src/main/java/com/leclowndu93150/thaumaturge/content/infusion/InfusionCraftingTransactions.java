package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchStatus;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

/** Internal implementation behind the supported infusion facade. */
public final class InfusionCraftingTransactions {
    private InfusionCraftingTransactions() {}

    public static InfusionCraftingTransaction.Inspection inspect(
            InfusionMatrixContext context, ServerPlayer player, ItemStack catalyst, List<ItemStack> components) {
        InfusionCraftingTransaction.Failure invalid = validate(context, player);
        if (invalid == InfusionCraftingTransaction.Failure.NONE
                && (catalyst == null
                        || components == null
                        || components.stream().anyMatch(java.util.Objects::isNull))) {
            invalid = InfusionCraftingTransaction.Failure.INVALID_INPUT;
        }
        if (invalid != InfusionCraftingTransaction.Failure.NONE) {
            return InfusionCraftingTransaction.Inspection.failure(invalid);
        }
        return inspectValid(context, player, catalyst.copyWithCount(1), copyComponents(components));
    }

    public static InfusionCraftingTransaction.Failure start(
            InfusionMatrixContext context, ServerPlayer player, ResourceLocation expectedRecipeId) {
        InfusionCraftingTransaction.Failure invalid = validate(context, player);
        if (invalid != InfusionCraftingTransaction.Failure.NONE) return invalid;
        if (expectedRecipeId == null) return InfusionCraftingTransaction.Failure.INVALID_INPUT;
        BlockEntityInfusionMatrix matrix = matrix(context);
        if (matrix == null) return InfusionCraftingTransaction.Failure.MATRIX_UNAVAILABLE;
        if (!matrix.isActive()) return InfusionCraftingTransaction.Failure.MATRIX_INACTIVE;
        if (matrix.isCrafting()) return InfusionCraftingTransaction.Failure.MATRIX_BUSY;

        MatrixEnvironment environment = matrix.environmentSnapshot(context.level());
        ItemStack catalyst = pedestalItem(context, context.position().below(2));
        List<ItemStack> components = new ArrayList<>();
        for (BlockPos pedestal : environment.pedestals()) {
            ItemStack stack = pedestalItem(context, pedestal);
            if (!stack.isEmpty()) components.add(stack.copyWithCount(1));
        }
        InfusionCraftingTransaction.Inspection inspection = inspectValid(context, player, catalyst, components);
        if (!inspection.successful()) return inspection.failure();
        if (!expectedRecipeId.equals(inspection.recipeId())) {
            return InfusionCraftingTransaction.Failure.COMPONENTS_CHANGED;
        }
        matrix.refreshSurroundings();
        matrix.onRightClick(context.level(), player);
        return matrix.isCrafting()
                ? InfusionCraftingTransaction.Failure.NONE
                : InfusionCraftingTransaction.Failure.NATIVE_INFUSION_FAILED;
    }

    private static InfusionCraftingTransaction.Inspection inspectValid(
            InfusionMatrixContext context, ServerPlayer player, ItemStack catalyst, List<ItemStack> components) {
        BlockEntityInfusionMatrix matrix = matrix(context);
        if (matrix == null || !MatrixEnvironment.validLocation(context.level(), context.position())) {
            return InfusionCraftingTransaction.Inspection.failure(
                    InfusionCraftingTransaction.Failure.MATRIX_UNAVAILABLE);
        }
        InfusionInput input = new InfusionInput(catalyst, components);
        InfusionCraftingTransaction.Inspection locked = null;

        RecipeHolder<InfusionRecipe> normal = context.level()
                .getRecipeManager()
                .getRecipeFor(TCRecipeTypes.INFUSION.get(), input, context.level())
                .orElse(null);
        if (normal != null) {
            InfusionRecipe recipe = normal.value();
            InfusionCraftingTransaction.Inspection inspection = result(
                    matrix.environmentSnapshot(context.level()),
                    player,
                    normal.id(),
                    recipe,
                    recipe.matchComponents(components),
                    recipe.aspects(),
                    recipe.instability(),
                    projectedResult(recipe.assemble(input, context.level().registryAccess()), catalyst),
                    true);
            if (inspection.successful()) return inspection;
            locked = inspection;
        }

        RecipeHolder<InfusionEnchantmentRecipe> enchantment = context.level()
                .getRecipeManager()
                .getRecipeFor(TCRecipeTypes.INFUSION_ENCHANTMENT.get(), input, context.level())
                .orElse(null);
        if (enchantment != null) {
            InfusionEnchantmentRecipe recipe = enchantment.value();
            InfusionCraftingTransaction.Inspection inspection = result(
                    matrix.environmentSnapshot(context.level()),
                    player,
                    enchantment.id(),
                    recipe,
                    recipe.matchComponents(components),
                    recipe.scaledAspects(catalyst),
                    recipe.instability(),
                    projectedResult(recipe.assemble(input, context.level().registryAccess()), catalyst),
                    false);
            if (inspection.successful()) return inspection;
            if (locked == null) locked = inspection;
        }

        RecipeHolder<InfusionRunicAugmentRecipe> runic = context.level()
                .getRecipeManager()
                .getRecipeFor(TCRecipeTypes.RUNIC_AUGMENT.get(), input, context.level())
                .orElse(null);
        if (runic == null) {
            return locked != null
                    ? locked
                    : InfusionCraftingTransaction.Inspection.failure(InfusionCraftingTransaction.Failure.NO_RECIPE);
        }
        InfusionRunicAugmentRecipe recipe = runic.value();
        InfusionCraftingTransaction.Inspection inspection = result(
                matrix.environmentSnapshot(context.level()),
                player,
                runic.id(),
                recipe,
                recipe.matchScaled(catalyst, components),
                recipe.scaledAspects(catalyst),
                recipe.scaledInstability(catalyst),
                projectedResult(recipe.augmentedResult(catalyst), catalyst),
                true);
        return inspection.successful() || locked == null ? inspection : locked;
    }

    private static InfusionCraftingTransaction.Inspection result(
            MatrixEnvironment environment,
            ServerPlayer player,
            ResourceLocation recipeId,
            com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe recipe,
            @Nullable List<ItemStack> matched,
            AspectList aspects,
            int instability,
            ItemStack output,
            boolean exactOutput) {
        if (matched == null) {
            return InfusionCraftingTransaction.Inspection.failure(InfusionCraftingTransaction.Failure.NO_RECIPE);
        }
        ResearchStatus research = recipe.researchStatus(player);
        float multiplier = BlockEntityInfusionMatrix.effectiveCostMultiplier(environment);
        AspectList required = BlockEntityInfusionMatrix.scaleByEnvironment(aspects, multiplier);
        return new InfusionCraftingTransaction.Inspection(
                research.permitsCrafting(),
                research.permitsCrafting()
                        ? InfusionCraftingTransaction.Failure.NONE
                        : InfusionCraftingTransaction.Failure.RESEARCH_LOCKED,
                recipeId,
                required,
                instability,
                research,
                output,
                output,
                remainders(matched),
                exactOutput);
    }

    private static InfusionCraftingTransaction.Failure validate(InfusionMatrixContext context, ServerPlayer player) {
        if (context == null || player == null) return InfusionCraftingTransaction.Failure.INVALID_CONTEXT;
        if (!context.level().getServer().isSameThread()) {
            return InfusionCraftingTransaction.Failure.NOT_SERVER_THREAD;
        }
        if (context.level() != player.serverLevel() || !context.actingPlayer().equals(player.getUUID())) {
            return InfusionCraftingTransaction.Failure.INVALID_CONTEXT;
        }
        return InfusionCraftingTransaction.Failure.NONE;
    }

    private static @Nullable BlockEntityInfusionMatrix matrix(InfusionMatrixContext context) {
        return context.level().getBlockEntity(context.position()) instanceof BlockEntityInfusionMatrix matrix
                ? matrix
                : null;
    }

    private static ItemStack pedestalItem(InfusionMatrixContext context, BlockPos position) {
        return context.level().getBlockEntity(position) instanceof BlockEntityPedestal pedestal
                ? pedestal.getItem().copy()
                : ItemStack.EMPTY;
    }

    private static List<ItemStack> copyComponents(List<ItemStack> components) {
        return components.stream().map(stack -> stack.copyWithCount(1)).toList();
    }

    private static ItemStack projectedResult(ItemStack result, ItemStack catalyst) {
        return BlockEntityInfusionMatrix.preserveCatalystDamage(result, catalyst);
    }

    private static List<ItemStack> remainders(List<ItemStack> matched) {
        return matched.stream()
                .map(stack -> stack.getItem().hasCraftingRemainingItem(stack)
                        ? stack.getItem().getCraftingRemainingItem(stack)
                        : ItemStack.EMPTY)
                .map(stack -> stack == null ? ItemStack.EMPTY : stack.copy())
                .toList();
    }
}
