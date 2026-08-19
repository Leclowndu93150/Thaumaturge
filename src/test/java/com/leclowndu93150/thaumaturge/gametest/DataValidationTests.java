package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

public final class DataValidationTests {
    private static final int MIN_RESEARCH_ENTRIES = 140;
    private static final int MIN_ASPECTS = 36;
    private static final int MIN_INDEXED_ITEMS = 500;
    private static final int MAX_COMPONENT_DEPTH = 8;

    private DataValidationTests() {}

    public static void register(TCTestRegistrar r) {
        r.add("data/recipes_present", 20, helper -> {
            RecipeManager recipes = helper.getLevel().getServer().getRecipeManager();
            requireRecipes(helper, recipes, TCRecipeTypes.ARCANE.get(), "arcane");
            requireRecipes(helper, recipes, TCRecipeTypes.CRUCIBLE.get(), "crucible");
            requireRecipes(helper, recipes, TCRecipeTypes.INFUSION.get(), "infusion");
            requireRecipes(helper, recipes, TCRecipeTypes.INFUSION_ENCHANTMENT.get(), "infusion enchantment");
            requireRecipes(helper, recipes, TCRecipeTypes.DUST_TRIGGER.get(), "dust trigger");
            helper.succeed();
        });

        r.add("data/research_entries_load", 20, helper -> {
            var registry = helper.getLevel().registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY);
            long count = registry.listElements().count();
            if (count < MIN_RESEARCH_ENTRIES) {
                helper.fail("Expected at least " + MIN_RESEARCH_ENTRIES + " research entries, found " + count);
                return;
            }
            helper.succeed();
        });

        r.add("data/aspect_compositions_resolve", 20, helper -> {
            var registry = helper.getLevel().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
            long count = registry.listElements().count();
            if (count < MIN_ASPECTS) {
                helper.fail("Expected at least " + MIN_ASPECTS + " aspects, found " + count);
                return;
            }
            registry.listElements().forEach(aspect -> {
                if (!aspect.value().isPrimal() && !terminatesAtPrimals(aspect, 0, new HashSet<>())) {
                    helper.fail("Aspect " + aspect.key().identifier() + " does not decompose to primals within depth " + MAX_COMPONENT_DEPTH);
                }
            });
            helper.succeed();
        });

        r.add("data/aspect_index_built", 20, helper -> {
            int size = AspectIndexHolder.getConcrete().size();
            if (size < MIN_INDEXED_ITEMS) {
                helper.fail("Aspect index holds " + size + " items, expected at least " + MIN_INDEXED_ITEMS);
                return;
            }
            helper.succeed();
        });
    }

    private static void requireRecipes(net.minecraft.gametest.framework.GameTestHelper helper, RecipeManager recipes, RecipeType<?> type, String label) {
        boolean any = recipes.getRecipes().stream().anyMatch(holder -> holder.value().getType() == type);
        if (!any) {
            helper.fail("No " + label + " recipes loaded");
        }
    }

    private static boolean terminatesAtPrimals(Holder<IAspect> aspect, int depth, Set<Object> seen) {
        if (aspect.value().isPrimal()) {
            return true;
        }
        if (depth >= MAX_COMPONENT_DEPTH || aspect.value().components().isEmpty() || !seen.add(aspect.value())) {
            return false;
        }
        boolean resolved = true;
        for (Holder<IAspect> component : aspect.value().components()) {
            if (!terminatesAtPrimals(component, depth + 1, seen)) {
                resolved = false;
                break;
            }
        }
        seen.remove(aspect.value());
        return resolved;
    }
}
