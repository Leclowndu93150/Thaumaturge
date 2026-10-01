package com.leclowndu93150.thaumaturge.client.screen.research;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.content.research.ResearchStage;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ResearchStageHistoryTest {
    private static final ResourceLocation RESEARCH = ResourceLocation.withDefaultNamespace("test_research");

    @Test
    void recognizesRepeatedFinalStageWithoutCompletionRequirements() {
        IResearchStage requirementStage = stage("same_text", List.of(RESEARCH));
        IResearchStage emptyFinalStage = stage("same_text", List.of());

        assertTrue(EntryDetailScreen.hasRedundantFinalStage(List.of(requirementStage, emptyFinalStage)));
    }

    @Test
    void recognizesFinalStageThatOnlyAddsRecipeBookmarks() {
        ResourceLocation recipe = ResourceLocation.fromNamespaceAndPath("thaumaturge", "infusion/wand_rod_silverwood");
        IResearchStage requirementStage = stage("same_text", List.of(), List.of(RESEARCH));
        IResearchStage recipeStage = stage("same_text", List.of(recipe), List.of());

        assertTrue(EntryDetailScreen.hasRedundantFinalStage(List.of(requirementStage, recipeStage)));
    }

    @Test
    void keepsHistoryWhenFinalStageHasDifferentContentOrRequirements() {
        IResearchStage requirementStage = stage("first_text", List.of(RESEARCH));

        assertFalse(EntryDetailScreen.hasRedundantFinalStage(
                List.of(requirementStage, stage("different_text", List.of()))));
        assertFalse(EntryDetailScreen.hasRedundantFinalStage(
                List.of(requirementStage, stage("first_text", List.of(RESEARCH)))));
        ResourceLocation earlierRecipe = ResourceLocation.fromNamespaceAndPath("thaumaturge", "test/earlier");
        ResourceLocation replacementRecipe = ResourceLocation.fromNamespaceAndPath("thaumaturge", "test/replacement");
        assertFalse(EntryDetailScreen.hasRedundantFinalStage(List.of(
                stage("first_text", List.of(earlierRecipe), List.of(RESEARCH)),
                stage("first_text", List.of(replacementRecipe), List.of()))));
    }

    private static ResearchStage stage(String text, List<ResourceLocation> requiredResearch) {
        return stage(text, List.of(), requiredResearch);
    }

    private static ResearchStage stage(
            String text, List<ResourceLocation> recipes, List<ResourceLocation> requiredResearch) {
        return new ResearchStage(
                text, recipes, Optional.empty(), List.of(), List.of(), List.of(), List.of(), requiredResearch, 0);
    }
}
