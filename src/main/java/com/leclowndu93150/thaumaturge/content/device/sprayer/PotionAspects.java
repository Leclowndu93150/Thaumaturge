package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingAccessor;
import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingMixAccessor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

final class PotionAspects {
    private static final float REDUCTION_FACTOR = 0.66F;
    private static final int ALKIMIA_PER_REAGENT = 3;
    private static final int FALLBACK_AMOUNT = 5;
    private static final int ASPECT_CAP = 10;
    private static final int MAX_REAGENT_DEPTH = 16;

    private PotionAspects() {}

    static AspectList of(ServerLevel level, ItemStack stack) {
        Optional<Holder<Potion>> potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        AspectList result = AspectList.EMPTY;
        boolean anyReagent = false;
        if (potion.isPresent() && !potion.get().is(Potions.WATER)) {
            for (ItemStack reagent : reagentsOf(level, potion.get())) {
                AspectList reagentAspects = AspectIndexAccess.index().of(reagent);
                for (AspectInstance entry : reagentAspects.entries()) {
                    result = result.add(entry.aspect(), entry.amount());
                }
                result = result.add(resolve(level, TCAspects.ALKIMIA.identifier()), ALKIMIA_PER_REAGENT);
                anyReagent = true;
            }
            AspectList reduced = AspectList.EMPTY;
            for (AspectInstance entry : result.entries()) {
                int kept = entry.amount() - (int) (entry.amount() * REDUCTION_FACTOR);
                if (kept > 0) {
                    reduced = reduced.add(entry.aspect(), kept);
                }
            }
            result = reduced;
        }
        if (!anyReagent) {
            result = result.add(resolve(level, TCAspects.PRAECANTATIO.identifier()), FALLBACK_AMOUNT).add(resolve(level, TCAspects.ALKIMIA.identifier()), FALLBACK_AMOUNT);
        }
        return cull(result, ASPECT_CAP);
    }

    private static Holder<IAspect> resolve(ServerLevel level, Identifier id) {
        return EssentiaTransportHelper.resolve(level, ResourceKey.create(IAspect.REGISTRY_KEY, id));
    }

    private static List<ItemStack> reagentsOf(ServerLevel level, Holder<Potion> potion) {
        List<ItemStack> reagents = new ArrayList<>();
        Set<Identifier> visitedPotions = new HashSet<>();
        List<?> mixes = ((PotionBrewingAccessor) level.potionBrewing()).thaumaturge$getPotionMixes();
        collectReagents(mixes, potion, reagents, visitedPotions, 0);
        return reagents;
    }

    private static void collectReagents(List<?> mixes, Holder<Potion> target, List<ItemStack> reagents, Set<Identifier> visited, int depth) {
        if (depth > MAX_REAGENT_DEPTH)
            return;
        Identifier targetId = target.unwrapKey().map(k -> k.identifier()).orElse(null);
        if (targetId == null || !visited.add(targetId))
            return;
        for (Object raw : mixes) {
            PotionBrewingMixAccessor mix = (PotionBrewingMixAccessor) raw;
            if (!mix.thaumaturge$getTo().equals(target))
                continue;
            Ingredient ingredient = mix.thaumaturge$getIngredient();
            ingredient.items().findFirst().ifPresent(holder -> {
                ItemStack reagent = new ItemStack(holder);
                for (ItemStack existing : reagents) {
                    if (ItemStack.isSameItemSameComponents(existing, reagent)) {
                        return;
                    }
                }
                reagents.add(reagent);
            });
            @SuppressWarnings("unchecked")
            Holder<Potion> from = (Holder<Potion>) mix.thaumaturge$getFrom();
            collectReagents(mixes, from, reagents, visited, depth + 1);
        }
    }

    private static AspectList cull(AspectList list, int cap) {
        AspectList culled = list;
        while (culled.size() > cap) {
            AspectInstance lowest = null;
            for (AspectInstance entry : culled.entries()) {
                if (lowest == null || entry.amount() < lowest.amount()) {
                    lowest = entry;
                }
            }
            if (lowest == null)
                break;
            culled = culled.remove(lowest.aspect(), lowest.amount());
        }
        return culled;
    }
}
