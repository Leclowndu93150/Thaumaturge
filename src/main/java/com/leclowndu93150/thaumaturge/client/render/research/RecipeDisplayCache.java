package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class RecipeDisplayCache {
    private static final Map<InfusionRunicAugmentRecipe, List<RecipeHolder<?>>> RUNIC_PAGES = new WeakHashMap<>();

    private RecipeDisplayCache() {}

    public static List<RecipeHolder<?>> get(ResourceLocation id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return List.of();
        }
        return mc.level
                .getRecipeManager()
                .byKey(id)
                .<List<RecipeHolder<?>>>map(holder -> {
                    if (!(holder.value() instanceof InfusionRunicAugmentRecipe runic)) {
                        return List.of(holder);
                    }
                    return RUNIC_PAGES.computeIfAbsent(runic, ignored -> runicPages(id, runic));
                })
                .orElseGet(List::of);
    }

    private static List<RecipeHolder<?>> runicPages(ResourceLocation id, InfusionRunicAugmentRecipe runic) {
        ItemStack[] catalysts = runic.catalyst().getItems();
        if (catalysts.length == 0) {
            return List.of(new RecipeHolder<>(id, runic));
        }
        List<RecipeHolder<?>> pages = new ArrayList<>(5);
        for (int charge = 0; charge < 5; charge++) {
            ItemStack catalyst = catalysts[0].copy();
            catalyst.set(TCDataComponents.RUNIC_CHARGE.get(), charge);
            InfusionRecipe display = new InfusionRecipe(
                    Ingredient.of(catalyst),
                    runic.scaledComponents(catalyst),
                    runic.scaledAspects(catalyst),
                    runic.scaledInstability(catalyst),
                    runic.augmentedResult(catalyst),
                    runic.researchGate());
            pages.add(new RecipeHolder<>(id, display));
        }
        return List.copyOf(pages);
    }

    public static void ensureRequested(ResourceLocation id) {}

    public static void clear() {
        RUNIC_PAGES.clear();
    }
}
