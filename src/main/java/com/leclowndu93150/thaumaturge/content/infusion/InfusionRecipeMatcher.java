package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

public final class InfusionRecipeMatcher {
    private InfusionRecipeMatcher() {}

    public static @Nullable Match find(ServerLevel level, Player player, InfusionInput input) {
        Match locked = null;
        for (RecipeType<? extends InfusionJobRecipe> type : List.of(
                TTRecipeTypes.INFUSION.get(),
                TTRecipeTypes.INFUSION_ENCHANTMENT.get(),
                TTRecipeTypes.RUNIC_AUGMENT.get())) {
            Match match = scan(level, player, input, type);
            if (match != null && !match.locked()) {
                return match;
            }
            if (locked == null) {
                locked = match;
            }
        }
        return locked;
    }

    private static <T extends InfusionJobRecipe> @Nullable Match scan(
            ServerLevel level, Player player, InfusionInput input, RecipeType<T> type) {
        Match locked = null;
        for (RecipeHolder<T> holder : level.getRecipeManager().getAllRecipesFor(type)) {
            if (!holder.value().matches(input, level)) {
                continue;
            }
            if (holder.value().doesPassGate(player)) {
                return new Match(holder, false);
            }
            if (locked == null) {
                locked = new Match(holder, true);
            }
        }
        return locked;
    }

    public record Match(RecipeHolder<? extends InfusionJobRecipe> holder, boolean locked) {

        public InfusionJobRecipe recipe() {
            return holder.value();
        }
    }
}
