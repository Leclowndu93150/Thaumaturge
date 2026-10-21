package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectSupplement;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumaturge.content.wands.WandAspectVariants;
import com.leclowndu93150.thaumaturge.registry.TTAspectContributors;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

public final class AspectIndexBuilder {
    public static final int MAX_AMOUNT_PER_ASPECT = 500;
    private static final int MAX_RECURSION_DEPTH = 100;

    private AspectIndexBuilder() {}

    public static AspectIndex build(RecipeManager recipes, HolderLookup.Provider registries) {
        for (IAspectRecipeContributor contributor : TTAspectContributors.all()) {
            contributor.beginBuild(recipes, registries);
        }
        RecursiveIndex index = new RecursiveIndex(collectBase(registries), collectSupplements(), recipes, registries, TTAspectContributors.all());
        for (Item item : BuiltInRegistries.ITEM) {
            index.resolve(item);
        }
        Map<Item, AspectList> resolved = index.resolved();
        return AspectIndex.of(resolved, Map.of(TTItems.WAND.get(), WandAspectVariants.build(resolved)));
    }

    private static Map<Item, AspectList> collectBase(HolderLookup.Provider registries) {
        Map<Item, AspectList> map = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            AspectList declared = item.builtInRegistryHolder().getData(AspectDataMaps.BASE_ASPECTS);
            if (declared != null) {
                map.put(item, declared.isEmpty() ? AspectList.EMPTY : cap(declared.merge(EquipmentAspects.bonusFor(item, registries)), registries));
            }
        }
        return map;
    }

    private static Map<Item, AspectSupplement> collectSupplements() {
        Map<Item, AspectSupplement> supplements = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            AspectSupplement supplement = item.builtInRegistryHolder().getData(AspectDataMaps.ASPECT_SUPPLEMENTS);
            if (supplement != null) {
                supplements.put(item, supplement);
            }
        }
        return Map.copyOf(supplements);
    }

    private static AspectList cap(AspectList list, HolderLookup.Provider registries) {
        AspectList result = AspectList.EMPTY;
        for (var entry : list.entries()) {
            Holder<IAspect> bound = entry.aspect().unwrapKey().map(key -> Aspects.resolve(registries, key)).orElse(null);
            if (bound == null) {
                Thaumaturge.LOGGER.warn("Dropping aspect {} while building the aspect index: not resolvable in the active registries", entry.aspect());
                continue;
            }
            int capped = Math.min(MAX_AMOUNT_PER_ASPECT, entry.amount());
            if (capped != entry.amount()) {
                Thaumaturge.LOGGER.debug("Capping aspect {} from {} to {} while building the aspect index", bound.unwrapKey().orElseThrow().identifier(), entry.amount(), capped);
            }
            result = result.add(bound, capped);
        }
        return result;
    }

    static final class RecursiveIndex implements IAspectIndex {
        private final Map<Item, AspectList> resolved;
        private final Set<Item> computed = new HashSet<>();
        private final Set<Item> visiting = new HashSet<>();
        private final RecipeManager recipes;
        private final HolderLookup.Provider registries;
        private final Map<Item, AspectSupplement> supplements;
        private final List<IAspectRecipeContributor> contributors;

        RecursiveIndex(Map<Item, AspectList> base, Map<Item, AspectSupplement> supplements, RecipeManager recipes, HolderLookup.Provider registries, List<IAspectRecipeContributor> contributors) {
            this.resolved = new HashMap<>(base);
            this.computed.addAll(base.keySet());
            this.recipes = recipes;
            this.registries = registries;
            this.supplements = Map.copyOf(supplements);
            this.contributors = List.copyOf(contributors);
        }

        Map<Item, AspectList> resolved() {
            return Map.copyOf(resolved);
        }

        AspectList resolve(Item item) {
            if (computed.contains(item)) {
                return resolved.getOrDefault(item, AspectList.EMPTY);
            }
            if (visiting.contains(item) || visiting.size() >= MAX_RECURSION_DEPTH) {
                Thaumaturge.LOGGER.debug("Skipping recursive aspect derivation for {}: cycle or depth limit {}", BuiltInRegistries.ITEM.getKey(item), MAX_RECURSION_DEPTH);
                return AspectList.EMPTY;
            }
            visiting.add(item);
            try {
                AspectList derived = AspectList.EMPTY;
                for (IAspectRecipeContributor contributor : contributors) {
                    Optional<AspectList> result = contributor.derive(item, recipes, registries, this);
                    if (result.isPresent() && !result.get().isEmpty()) {
                        derived = result.get();
                        break;
                    }
                }
                AspectList merged = supplement(item, cap(derived.merge(EquipmentAspects.bonusFor(item, registries)), registries));
                computed.add(item);
                if (merged.isEmpty()) {
                    return AspectList.EMPTY;
                }
                AspectList capped = cap(merged, registries);
                resolved.put(item, capped);
                return capped;
            } finally {
                visiting.remove(item);
            }
        }

        private AspectList supplement(Item item, AspectList derived) {
            AspectSupplement supplement = supplements.get(item);
            if (supplement == null) {
                return derived;
            }
            AspectList result = derived;
            for (var entry : supplement.amounts().entrySet()) {
                Holder<IAspect> aspect = Aspects.resolve(registries, ResourceKey.create(IAspect.REGISTRY_KEY, entry.getKey()));
                if (aspect == null) {
                    Thaumaturge.LOGGER.warn("Dropping unknown supplemental aspect {} for {}", entry.getKey(), BuiltInRegistries.ITEM.getKey(item));
                } else if (entry.getValue() > 0) {
                    result = result.add(aspect, entry.getValue());
                }
            }
            return result;
        }

        @Override
        public AspectList of(Item item) {
            return resolve(item);
        }

        @Override
        public AspectList of(ItemStack stack) {
            return resolve(stack.getItem());
        }
    }
}
