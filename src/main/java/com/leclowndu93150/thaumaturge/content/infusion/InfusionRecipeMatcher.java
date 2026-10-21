package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.recipe.DynamicInfusionRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionRecipeInput;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

public final class InfusionRecipeMatcher {
    private InfusionRecipeMatcher() {}

    public static @Nullable Match find(ServerLevel level, Player player, InfusionInput input) {
        return find(level, KnowledgeAccess.of(player), input);
    }

    public static @Nullable Match find(ServerLevel level, IPlayerKnowledge knowledge, InfusionInput input) {
        Match locked = null;
        for (RecipeType<? extends InfusionJobRecipe> type : List.of(TTRecipeTypes.INFUSION.get(), TTRecipeTypes.INFUSION_ENCHANTMENT.get(), TTRecipeTypes.RUNIC_AUGMENT.get())) {
            Match match = scan(level, knowledge, input, type);
            if (match != null && !match.locked()) {
                return match;
            }
            if (locked == null) {
                locked = match;
            }
        }
        if (input.components().size() <= 64) {
            InfusionRecipeInput snapshot = new InfusionRecipeInput(input.catalyst(), input.components());
            for (RecipeHolder<DynamicInfusionRecipe> holder : level.recipeAccess().recipeMap().byType(TTRecipeTypes.DYNAMIC_INFUSION.get())) {
                DynamicInfusionRecipe recipe = holder.value();
                Match match;
                try {
                    if (!recipe.matches(snapshot, level.registryAccess()) || recipe.matchComponents(snapshot.components()) == null) {
                        continue;
                    }
                    match = new Match(holder, !ResearchManager.doesPassGate(knowledge, recipe.researchGate().orElse(null)), new DynamicInfusionAdapter(recipe, snapshot, level.registryAccess()));
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    continue;
                }
                if (!match.locked()) {
                    return match;
                }
                if (locked == null) {
                    locked = match;
                }
            }
        }
        return locked;
    }

    private static <T extends InfusionJobRecipe> @Nullable Match scan(ServerLevel level, IPlayerKnowledge knowledge, InfusionInput input, RecipeType<T> type) {
        Match locked = null;
        for (RecipeHolder<T> holder : level.recipeAccess().recipeMap().byType(type)) {
            if (!holder.value().matches(input, level)) {
                continue;
            }
            if (ResearchManager.doesPassGate(knowledge, holder.value().researchGate().orElse(null))) {
                return new Match(holder, false, holder.value());
            }
            if (locked == null) {
                locked = new Match(holder, true, holder.value());
            }
        }
        return locked;
    }

    public record Match(RecipeHolder<?> holder, boolean locked, InfusionJobRecipe recipe) {
    }
}
