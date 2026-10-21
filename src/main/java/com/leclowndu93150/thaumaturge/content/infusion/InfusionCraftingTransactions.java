package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.infusion.InfusionAutomationPolicy;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.Failure;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.Inspection;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.StagedInspection;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.State;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.Status;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionDelegation;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGateStatus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

public final class InfusionCraftingTransactions implements InfusionCraftingTransaction.Bindings {

    @Override
    public StagedInspection inspectStaged(InfusionDelegation.Context context) {
        IPlayerKnowledge knowledge = InfusionDelegations.knowledge(context);
        if (knowledge == null) {
            return stagedFailure(Failure.DELEGATION_DENIED);
        }
        BlockEntityInfusionMatrix matrix = matrix(context.matrix());
        if (matrix == null) {
            return stagedFailure(Failure.MATRIX_UNAVAILABLE);
        }
        InfusionInput input = matrix.stagedInput(context.matrix().level());
        InfusionRecipeMatcher.Match match = InfusionRecipeMatcher.find(context.matrix().level(), knowledge, input);
        if (match == null) {
            return stagedFailure(Failure.NO_RECIPE);
        }
        if (match.locked() || !InfusionDelegations.valid(context, match.holder().id())) {
            return stagedFailure(Failure.DELEGATION_DENIED);
        }
        InfusionJobRecipe recipe = match.recipe();
        ItemStack output = BlockEntityInfusionMatrix.withCatalystWear(recipe.assemble(input), input.catalyst());
        Inspection inspection = new Inspection(Failure.NONE, match.holder().id(), matrix.projectedCost(context.matrix().level(), recipe.jobEssentia(input)), recipe.jobInstability(input),
                recipe.researchGate().isEmpty() ? ResearchGateStatus.NOT_REQUIRED : ResearchGateStatus.UNLOCKED, output, remainders(recipe.jobComponents(input)), recipe.exactResult());
        return new StagedInspection(inspection, matrix.craftRevision(), input.catalyst(), input.components());
    }

    @Override
    public Status status(InfusionDelegation.Context context) {
        if (InfusionDelegations.knowledge(context) == null) {
            return new Status(Failure.DELEGATION_DENIED, State.UNAVAILABLE, 0);
        }
        BlockEntityInfusionMatrix matrix = matrix(context.matrix());
        if (matrix == null) {
            return new Status(Failure.MATRIX_UNAVAILABLE, State.UNAVAILABLE, 0);
        }
        return new Status(Failure.NONE, !matrix.isActive() ? State.INACTIVE : matrix.isCrafting() ? State.CRAFTING : State.IDLE, matrix.craftRevision());
    }

    @Override
    public Failure startDelegated(InfusionDelegation.Context context, StagedInspection expected) {
        ResourceKey<Recipe<?>> recipeId = expected.inspection().recipeId();
        if (!expected.inspection().successful() || recipeId == null || !InfusionDelegations.valid(context, recipeId)) {
            return Failure.DELEGATION_DENIED;
        }
        IPlayerKnowledge knowledge = InfusionDelegations.knowledge(context);
        if (knowledge == null) {
            return Failure.DELEGATION_DENIED;
        }
        BlockEntityInfusionMatrix matrix = matrix(context.matrix());
        if (matrix == null) {
            return Failure.MATRIX_UNAVAILABLE;
        }
        if (!matrix.isActive()) {
            return Failure.MATRIX_INACTIVE;
        }
        if (matrix.isCrafting()) {
            return Failure.MATRIX_BUSY;
        }
        if (expected.revision() != matrix.craftRevision() || !sameInput(expected, matrix.stagedInput(context.matrix().level()))) {
            return Failure.INGREDIENTS_CHANGED;
        }
        if (!sameInspection(expected.inspection(), inspectStaged(context).inspection())) {
            return Failure.INGREDIENTS_CHANGED;
        }
        int surcharge = InfusionDelegations.surcharge(context);
        if (surcharge < 0) {
            return Failure.DELEGATION_DENIED;
        }
        return matrix.tryStartCraft(context.matrix().level(), context.matrix().actingPlayer(), knowledge, recipeId, surcharge) ? Failure.NONE : Failure.NOT_STARTED;
    }

    @Override
    public StagedInspection inspectStaged(InfusionMatrixContext context, ServerPlayer player) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return stagedFailure(invalid);
        }
        BlockEntityInfusionMatrix matrix = matrix(context);
        if (matrix == null) {
            return stagedFailure(Failure.MATRIX_UNAVAILABLE);
        }
        InfusionInput input = matrix.stagedInput(context.level());
        Inspection inspection = inspect(matrix, context.level(), player, input);
        if (!inspection.successful()) {
            return stagedFailure(inspection.failure());
        }
        return new StagedInspection(inspection, matrix.craftRevision(), input.catalyst(), input.components());
    }

    @Override
    public Status status(InfusionMatrixContext context, ServerPlayer player) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return new Status(invalid, State.UNAVAILABLE, 0);
        }
        BlockEntityInfusionMatrix matrix = matrix(context);
        if (matrix == null) {
            return new Status(Failure.MATRIX_UNAVAILABLE, State.UNAVAILABLE, 0);
        }
        State state = !matrix.isActive() ? State.INACTIVE : matrix.isCrafting() ? State.CRAFTING : State.IDLE;
        return new Status(Failure.NONE, state, matrix.craftRevision());
    }

    @Override
    public Failure start(InfusionMatrixContext context, ServerPlayer player, StagedInspection expected, @Nullable Identifier policy) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return invalid;
        }
        BlockEntityInfusionMatrix matrix = matrix(context);
        if (matrix == null) {
            return Failure.MATRIX_UNAVAILABLE;
        }
        if (!expected.inspection().successful() || expected.inspection().recipeId() == null) {
            return Failure.INGREDIENTS_CHANGED;
        }
        InfusionInput current = matrix.stagedInput(context.level());
        if (expected.revision() != matrix.craftRevision() || !sameInput(expected, current)) {
            return Failure.INGREDIENTS_CHANGED;
        }
        int surcharge = policy == null ? 0 : InfusionAutomationPolicy.resolve(policy, context, player);
        if (surcharge < 0) {
            return Failure.INVALID_AUTOMATION_POLICY;
        }
        if (expected.revision() != matrix.craftRevision() || !sameInput(expected, matrix.stagedInput(context.level()))) {
            return Failure.INGREDIENTS_CHANGED;
        }
        if (!sameInspection(expected.inspection(), inspect(matrix, context.level(), player, matrix.stagedInput(context.level())))) {
            return Failure.INGREDIENTS_CHANGED;
        }
        return start(context, player, expected.inspection().recipeId(), surcharge);
    }

    private static StagedInspection stagedFailure(Failure failure) {
        return new StagedInspection(Inspection.failure(failure), 0, ItemStack.EMPTY, List.of());
    }

    private static boolean sameInspection(Inspection expected, Inspection current) {
        if (!current.successful() || !java.util.Objects.equals(expected.recipeId(), current.recipeId()) || !expected.essentia().equals(current.essentia())
                || expected.instability() != current.instability() || expected.exactOutput() != current.exactOutput() || !ItemStack.matches(expected.output(), current.output())) {
            return false;
        }
        List<ItemStack> expectedRemainders = expected.componentRemainders();
        List<ItemStack> currentRemainders = current.componentRemainders();
        if (expectedRemainders.size() != currentRemainders.size()) {
            return false;
        }
        for (int index = 0; index < expectedRemainders.size(); index++) {
            if (!ItemStack.matches(expectedRemainders.get(index), currentRemainders.get(index))) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameInput(StagedInspection expected, InfusionInput current) {
        if (!ItemStack.matches(expected.catalyst(), current.catalyst())) {
            return false;
        }
        List<ItemStack> components = expected.components();
        if (components.size() != current.components().size()) {
            return false;
        }
        for (int index = 0; index < components.size(); index++) {
            if (!ItemStack.matches(components.get(index), current.components().get(index))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Inspection inspect(InfusionMatrixContext context, ServerPlayer player, ItemStack catalyst, List<ItemStack> components) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return Inspection.failure(invalid);
        }
        BlockEntityInfusionMatrix matrix = matrix(context);
        if (matrix == null) {
            return Inspection.failure(Failure.MATRIX_UNAVAILABLE);
        }
        List<ItemStack> singles = components.stream().map(stack -> stack.copyWithCount(1)).toList();
        return inspect(matrix, context.level(), player, new InfusionInput(catalyst.copyWithCount(1), singles));
    }

    @Override
    public Failure start(InfusionMatrixContext context, ServerPlayer player, ResourceKey<Recipe<?>> expectedRecipeId) {
        return start(context, player, expectedRecipeId, 0);
    }

    private Failure start(InfusionMatrixContext context, ServerPlayer player, ResourceKey<Recipe<?>> expectedRecipeId, int surcharge) {
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
        if (inspection.instability() > Integer.MAX_VALUE - surcharge) {
            return Failure.INVALID_AUTOMATION_POLICY;
        }
        return matrix.tryStartCraft(context.level(), player, surcharge) ? Failure.NONE : Failure.NOT_STARTED;
    }

    private static Inspection inspect(BlockEntityInfusionMatrix matrix, ServerLevel level, ServerPlayer player, InfusionInput input) {
        InfusionRecipeMatcher.Match match = InfusionRecipeMatcher.find(level, player, input);
        if (match == null) {
            return Inspection.failure(Failure.NO_RECIPE);
        }
        InfusionJobRecipe recipe = match.recipe();
        ItemStack output = BlockEntityInfusionMatrix.withCatalystWear(recipe.assemble(input), input.catalyst());
        return new Inspection(match.locked() ? Failure.RESEARCH_LOCKED : Failure.NONE, match.holder().id(), matrix.projectedCost(level, recipe.jobEssentia(input)), recipe.jobInstability(input),
                recipe.gateStatus(player), output, remainders(recipe.jobComponents(input)), recipe.exactResult());
    }

    private static Failure validate(InfusionMatrixContext context, ServerPlayer player) {
        if (!context.level().getServer().isSameThread()) {
            return Failure.NOT_SERVER_THREAD;
        }
        if (context.level() != player.level() || !context.actingPlayer().equals(player.getUUID())) {
            return Failure.INVALID_CONTEXT;
        }
        if (!context.level().mayInteract(player, context.position())) {
            return Failure.ACCESS_DENIED;
        }
        return Failure.NONE;
    }

    private static @Nullable BlockEntityInfusionMatrix matrix(InfusionMatrixContext context) {
        if (!context.level().hasChunkAt(context.position()) || !MatrixEnvironment.validLocation(context.level(), context.position())) {
            return null;
        }
        return context.level().getBlockEntity(context.position()) instanceof BlockEntityInfusionMatrix matrix ? matrix : null;
    }

    private static List<ItemStack> remainders(@Nullable List<ItemStack> consumed) {
        List<ItemStack> remainders = new ArrayList<>();
        if (consumed == null) {
            return remainders;
        }
        for (ItemStack stack : consumed) {
            ItemStackTemplate remainder = stack.getItem().getCraftingRemainder(stack);
            remainders.add(remainder == null ? ItemStack.EMPTY : remainder.create());
        }
        return remainders;
    }
}
