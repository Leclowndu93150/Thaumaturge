package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionCraftingTransactions;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Read-only inspection and guarded startup for crafts performed by a real infusion Matrix. */
public final class InfusionCraftingTransaction {
    private InfusionCraftingTransaction() {}

    public static Inspection inspect(
            InfusionMatrixContext context, ServerPlayer player, ItemStack catalyst, List<ItemStack> components) {
        return InfusionCraftingTransactions.inspect(context, player, catalyst, components);
    }

    /** Starts the already-staged Matrix craft only if it still matches {@code expectedRecipeId}. */
    public static Failure start(InfusionMatrixContext context, ServerPlayer player, ResourceLocation expectedRecipeId) {
        return InfusionCraftingTransactions.start(context, player, expectedRecipeId);
    }

    public enum Failure {
        NONE,
        NOT_SERVER_THREAD,
        INVALID_CONTEXT,
        INVALID_INPUT,
        MATRIX_UNAVAILABLE,
        MATRIX_INACTIVE,
        MATRIX_BUSY,
        NO_RECIPE,
        RESEARCH_LOCKED,
        CATALYST_CHANGED,
        COMPONENTS_CHANGED,
        NATIVE_INFUSION_FAILED
    }

    /** Immutable snapshot. {@code exactOutput} is false when native completion can add randomness. */
    public record Inspection(
            boolean successful,
            Failure failure,
            @Nullable ResourceLocation recipeId,
            AspectList requiredAspects,
            int instability,
            ResearchStatus researchStatus,
            ItemStack output,
            ItemStack catalystAfter,
            List<ItemStack> componentRemainders,
            boolean exactOutput) {
        public Inspection {
            Objects.requireNonNull(failure, "failure");
            Objects.requireNonNull(requiredAspects, "requiredAspects");
            Objects.requireNonNull(researchStatus, "researchStatus");
            Objects.requireNonNull(output, "output");
            Objects.requireNonNull(catalystAfter, "catalystAfter");
            Objects.requireNonNull(componentRemainders, "componentRemainders");
            if (instability < 0) throw new IllegalArgumentException("instability must not be negative");
            output = output.copy();
            catalystAfter = catalystAfter.copy();
            componentRemainders =
                    componentRemainders.stream().map(ItemStack::copy).toList();
        }

        public static Inspection failure(Failure failure) {
            ResearchStatus research =
                    failure == Failure.RESEARCH_LOCKED ? ResearchStatus.LOCKED : ResearchStatus.INVALID;
            return new Inspection(
                    false,
                    failure,
                    null,
                    AspectList.EMPTY,
                    0,
                    research,
                    ItemStack.EMPTY,
                    ItemStack.EMPTY,
                    List.of(),
                    false);
        }

        @Override
        public ItemStack output() {
            return output.copy();
        }

        @Override
        public ItemStack catalystAfter() {
            return catalystAfter.copy();
        }

        @Override
        public List<ItemStack> componentRemainders() {
            return componentRemainders.stream().map(ItemStack::copy).toList();
        }
    }
}
