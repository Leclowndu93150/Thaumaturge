package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public final class RecipeDisplayCache {
    private static final Map<InfusionRunicAugmentRecipe, List<RecipeHolder<?>>> RUNIC_PAGES = new WeakHashMap<>();
    private static final Map<RecipeManager, NitorPages> NITOR_PAGES = new WeakHashMap<>();

    private record NitorPages(RecipeHolder<?> anchor, List<RecipeHolder<?>> pages) {}

    private RecipeDisplayCache() {}

    public static List<RecipeHolder<?>> get(ResourceLocation id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return List.of();
        }
        RecipeManager recipes = mc.level.getRecipeManager();
        return recipes.byKey(id)
                .<List<RecipeHolder<?>>>map(holder -> {
                    if (!(holder.value() instanceof InfusionRunicAugmentRecipe runic)) {
                        if (id.getNamespace().equals(TCIds.MODID)
                                && id.getPath().startsWith("nitors/")) {
                            return nitorPages(recipes, holder);
                        }
                        return List.of(holder);
                    }
                    return RUNIC_PAGES.computeIfAbsent(runic, ignored -> runicPages(id, runic));
                })
                .orElseGet(List::of);
    }

    private static List<RecipeHolder<?>> nitorPages(RecipeManager recipes, RecipeHolder<?> anchor) {
        NitorPages cached = NITOR_PAGES.get(recipes);
        if (cached != null && cached.anchor() == anchor) {
            return cached.pages();
        }

        List<RecipeHolder<?>> pages = new ArrayList<>(DyeColor.values().length);
        for (DyeColor color : DyeColor.values()) {
            ResourceLocation id = TCIds.rl("nitors/" + color.getName());
            recipes.byKey(id).ifPresent(pages::add);
        }
        List<RecipeHolder<?>> result = pages.isEmpty() ? List.of(anchor) : List.copyOf(pages);
        NITOR_PAGES.put(recipes, new NitorPages(anchor, result));
        return result;
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
        NITOR_PAGES.clear();
    }
}
