package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumaturge.api.aspect.RegisterAspectContributorsEvent;
import com.leclowndu93150.thaumaturge.content.wands.WandAspectVariants;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.IEventBus;

public final class AspectIndexBuilder {
    public static final int MAX_AMOUNT_PER_ASPECT = 500;
    private static final int MAX_RECURSION_DEPTH = 100;

    private static final List<IAspectRecipeContributor> CONTRIBUTORS = new ArrayList<>();

    static {
        CONTRIBUTORS.add(new CrucibleAspectContributor());
        CONTRIBUTORS.add(new InfusionAspectContributor());
        CONTRIBUTORS.add(new CraftingAspectContributor());
    }

    private AspectIndexBuilder() {}

    public static void registerContributor(IAspectRecipeContributor contributor) {
        CONTRIBUTORS.add(contributor);
    }

    public static void fireContributorEvent(IEventBus modBus) {
        RegisterAspectContributorsEvent event = new RegisterAspectContributorsEvent();
        modBus.post(event);
        for (IAspectRecipeContributor contributor : event.contributors()) {
            CONTRIBUTORS.add(contributor);
        }
    }

    public static AspectIndex build(RecipeManager recipes, HolderLookup.Provider registries) {
        for (IAspectRecipeContributor contributor : CONTRIBUTORS) {
            contributor.beginBuild(recipes, registries);
        }
        RecursiveIndex index = new RecursiveIndex(collectBase(registries), recipes, registries);
        for (Item item : BuiltInRegistries.ITEM) {
            index.resolve(item);
        }
        Map<Item, AspectList> resolved = index.resolved();
        return AspectIndex.of(resolved, Map.of(TCItems.WAND.get(), WandAspectVariants.build(resolved)));
    }

    private static Map<Item, AspectList> collectBase(HolderLookup.Provider registries) {
        Map<Item, AspectList> map = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            AspectList declared = item.builtInRegistryHolder().getData(AspectDataMaps.BASE_ASPECTS);
            if (declared != null && !declared.isEmpty()) {
                AspectList capped = cap(declared.merge(EquipmentAspects.bonusFor(item, registries)), registries);
                if (!capped.isEmpty()) {
                    map.put(item, capped);
                }
            }
        }
        return map;
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
            result = result.add(bound, capped);
        }
        return result;
    }

    private static final class RecursiveIndex implements IAspectIndex {
        private final Map<Item, AspectList> resolved;
        private final Set<Item> computed = new HashSet<>();
        private final Set<Item> visiting = new HashSet<>();
        private final RecipeManager recipes;
        private final HolderLookup.Provider registries;

        RecursiveIndex(Map<Item, AspectList> base, RecipeManager recipes, HolderLookup.Provider registries) {
            this.resolved = new HashMap<>(base);
            this.computed.addAll(base.keySet());
            this.recipes = recipes;
            this.registries = registries;
        }

        Map<Item, AspectList> resolved() {
            return resolved;
        }

        AspectList resolve(Item item) {
            if (computed.contains(item)) {
                return resolved.getOrDefault(item, AspectList.EMPTY);
            }
            if (visiting.contains(item) || visiting.size() >= MAX_RECURSION_DEPTH) {
                return AspectList.EMPTY;
            }
            visiting.add(item);
            try {
                AspectList derived = AspectList.EMPTY;
                for (IAspectRecipeContributor contributor : CONTRIBUTORS) {
                    Optional<AspectList> result = contributor.derive(item, recipes, registries, this);
                    if (result.isPresent() && !result.get().isEmpty()) {
                        derived = result.get();
                        break;
                    }
                }
                AspectList merged = derived.merge(EquipmentAspects.bonusFor(item, registries));
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
