package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.compat.jei.category.AspectFromStacksCategory;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.network.ServerboundRequestSyncAspectPoolPayload;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class AspectJeiSync {
    private static @Nullable IJeiRuntime runtime;
    private static final Set<ResourceLocation> discoveredAspects = new HashSet<>();
    private static final Map<ResourceLocation, List<ItemStack>> gatedStacks = new HashMap<>();

    private AspectJeiSync() {}

    static void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        rebuildAspectStackPages();
        discoveredAspects.clear();
        gatedStacks.clear();
        IIngredientManager ingredients = jeiRuntime.getIngredientManager();
        for (ItemStack stack : ingredients.getAllIngredients(VanillaTypes.ITEM_STACK)) {
            ResourceLocation aspect = gatedAspectOf(stack);
            if (aspect != null) {
                gatedStacks.computeIfAbsent(aspect, k -> new ArrayList<>()).add(stack);
            }
        }
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.registryAccess()
                    .lookupOrThrow(IAspect.REGISTRY_KEY)
                    .listElements()
                    .forEach(ref -> {
                        if (AspectPools.isDiscovered(player, ref)) {
                            discoveredAspects.add(AspectPools.idOf(ref));
                        }
                    });
        }
        PacketDistributor.sendToServer(new ServerboundRequestSyncAspectPoolPayload());
    }

    private static @Nullable ResourceLocation gatedAspectOf(ItemStack stack) {
        if (stack.is(TTItems.ESSENTIA_CRYSTAL.get())) {
            AspectInstance instance = stack.get(TTDataComponents.CRYSTAL_ASPECT.get());
            return instance == null ? null : AspectPools.idOf(instance.aspect());
        }
        if (stack.is(TTItems.PHIAL.get())) {
            AspectList aspects = stack.get(TTDataComponents.ASPECTS.get());
            if (aspects == null || aspects.isEmpty()) {
                return null;
            }
            return AspectPools.idOf(aspects.entries().get(0).aspect());
        }
        return null;
    }

    public static void rebuildAspectStackPages() {
        IJeiRuntime current = runtime;
        if (current == null) {
            return;
        }
        IRecipeManager recipes = current.getRecipeManager();
        List<AspectFromStacksCategory.Wrapper> stale = recipes.createRecipeLookup(AspectFromStacksCategory.RECIPE_TYPE)
                .includeHidden()
                .get()
                .toList();
        if (!stale.isEmpty()) {
            recipes.hideRecipes(AspectFromStacksCategory.RECIPE_TYPE, stale);
        }
        List<AspectFromStacksCategory.Wrapper> pages = ThaumaturgeJEIPlugin.aspectFromStacksPages(
                current.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK));
        if (!pages.isEmpty()) {
            recipes.addRecipes(AspectFromStacksCategory.RECIPE_TYPE, pages);
        }
    }

    public static void syncDiscovered() {
        IJeiRuntime current = runtime;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (current == null || player == null) {
            return;
        }
        IIngredientManager ingredients = current.getIngredientManager();
        List<AspectInstance> changedAspects = new ArrayList<>();
        List<ItemStack> changedStacks = new ArrayList<>();
        player.registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .listElements()
                .forEach(ref -> {
                    ResourceLocation id = ref.key().location();
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
                    changedAspects.add(new AspectInstance(ref, 1));
                    List<ItemStack> stacks = gatedStacks.get(id);
                    if (stacks != null) {
                        changedStacks.addAll(stacks);
                    }
                });
        if (!changedAspects.isEmpty()) {
            ingredients.removeIngredientsAtRuntime(AspectIngredientType.INSTANCE, changedAspects);
            ingredients.addIngredientsAtRuntime(AspectIngredientType.INSTANCE, changedAspects);
        }
        if (!changedStacks.isEmpty()) {
            ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, changedStacks);
            ingredients.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, changedStacks);
        }
    }
}
