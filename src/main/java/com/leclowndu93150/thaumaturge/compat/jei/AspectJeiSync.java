package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.compat.jei.category.AspectCompositionCategory;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.network.ServerboundRequestSyncAspectPoolPayload;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class AspectJeiSync {
    private static @Nullable IJeiRuntime runtime;
    private static final Set<Identifier> discoveredAspects = new HashSet<>();

    private AspectJeiSync() {}

    static void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        discoveredAspects.clear();
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(ref -> {
                if (AspectPools.isDiscovered(player, ref)) {
                    discoveredAspects.add(AspectPools.idOf(ref));
                }
            });
            updateCompositionVisibility(jeiRuntime);
        }
        ClientPacketDistributor.sendToServer(ServerboundRequestSyncAspectPoolPayload.INSTANCE);
    }

    private static boolean isAffected(Object ingredient, Set<Identifier> changedIds) {
        if (ingredient instanceof AspectInstance instance) {
            return changedIds.contains(AspectPools.idOf(instance.aspect()));
        }
        if (ingredient instanceof ItemStack stack) {
            return changedIds.contains(gatedAspectOf(stack));
        }
        return false;
    }

    private static void updateCompositionVisibility(IJeiRuntime runtime) {
        IRecipeManager recipes = runtime.getRecipeManager();
        List<AspectCompositionCategory.Composition> hidden = new ArrayList<>();
        List<AspectCompositionCategory.Composition> shown = new ArrayList<>();
        recipes.createRecipeLookup(AspectCompositionCategory.RECIPE_TYPE).includeHidden().get().forEach(row -> {
            if (AspectKnowledgeAccess.of(row.result()).isCompositionRevealed()) {
                shown.add(row);
            } else {
                hidden.add(row);
            }
        });
        if (!hidden.isEmpty()) {
            recipes.hideRecipes(AspectCompositionCategory.RECIPE_TYPE, hidden);
        }
        if (!shown.isEmpty()) {
            recipes.unhideRecipes(AspectCompositionCategory.RECIPE_TYPE, shown);
        }
    }

    private static @Nullable Identifier gatedAspectOf(ItemStack stack) {
        if (stack.is(TCItems.ESSENTIA_CRYSTAL.get())) {
            AspectInstance instance = stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
            return instance == null ? null : AspectPools.idOf(instance.aspect());
        }
        if (stack.is(TCItems.PHIAL.get())) {
            AspectList aspects = stack.get(TCDataComponents.ASPECTS.get());
            if (aspects == null || aspects.isEmpty()) {
                return null;
            }
            return AspectPools.idOf(aspects.entries().get(0).aspect());
        }
        return null;
    }

    public static void syncDiscovered() {
        IJeiRuntime current = runtime;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (current == null || player == null) {
            return;
        }
        IIngredientManager ingredients = current.getIngredientManager();
        Set<Identifier> changedIds = new HashSet<>();
        player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(ref -> {
            Identifier id = ref.key().identifier();
            boolean discovered = AspectPools.isDiscovered(player, ref);
            boolean known = discoveredAspects.contains(id);
            if (discovered == known) {
                return;
            }
            if (discovered) {
                discoveredAspects.add(id);
            } else {
                discoveredAspects.remove(id);
            }
            changedIds.add(id);
        });
        if (changedIds.isEmpty()) {
            return;
        }
        JeiSearchIndex.reindex(current, ingredients, ingredient -> isAffected(ingredient, changedIds));
        updateCompositionVisibility(current);
    }
}
