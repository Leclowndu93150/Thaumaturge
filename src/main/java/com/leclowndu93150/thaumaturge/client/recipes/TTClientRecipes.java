package com.leclowndu93150.thaumaturge.client.recipes;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public class TTClientRecipes {

    private static RecipeMap recipeMap = RecipeMap.EMPTY;
    private static final Set<RecipeType<?>> knownRecipeTypes = Collections.newSetFromMap(new IdentityHashMap<>());

    @SubscribeEvent
    public static void receiveRecipes(RecipesReceivedEvent event) {
        recipeMap = event.getRecipeMap();
        knownRecipeTypes.clear();
        knownRecipeTypes.addAll(event.getRecipeTypes());
    }

    public static RecipeMap getRecipeMapForType(Level level, RecipeType<?> recipeType) {
        if (level instanceof ClientLevel) {
            if (!knownRecipeTypes.contains(recipeType)) {
                Thaumaturge.LOGGER.warn("Recipes of type {} haven't been received yet.", recipeType);
                return RecipeMap.EMPTY;
            }

            return recipeMap;
        }

        return RecipeMap.EMPTY;
    }
}
