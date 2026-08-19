package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndex;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public final class WandAspectVariants {
    private static final int CAPS_PER_WAND = 2;

    private WandAspectVariants() {}

    public static List<AspectIndex.Variant> build(Map<Item, AspectList> resolved) {
        Map<WandCap, Item> capItems = new LinkedHashMap<>();
        Map<WandRod, Item> rodItems = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ItemWandCap cap) {
                capItems.put(cap.cap(), item);
            } else if (item instanceof ItemWandRod rod) {
                rodItems.put(rod.rod(), item);
            }
        }
        List<AspectIndex.Variant> variants = new ArrayList<>();
        for (Map.Entry<WandCap, Item> cap : capItems.entrySet()) {
            AspectList capAspects = resolved.getOrDefault(cap.getValue(), AspectList.EMPTY);
            for (Map.Entry<WandRod, Item> rod : rodItems.entrySet()) {
                AspectList sum = resolved.getOrDefault(rod.getValue(), AspectList.EMPTY);
                for (int i = 0; i < CAPS_PER_WAND; i++) {
                    sum = addAll(sum, capAspects);
                }
                if (sum.isEmpty()) {
                    continue;
                }
                variants.add(variant(cap.getKey(), rod.getKey(), false, sum));
                variants.add(variant(cap.getKey(), rod.getKey(), true, sum));
            }
        }
        return variants;
    }

    private static AspectIndex.Variant variant(WandCap cap, WandRod rod, boolean sceptre, AspectList aspects) {
        return new AspectIndex.Variant(new DataComponentMatchers(DataComponentExactPredicate.expect(TCDataComponents.WAND_PARTS.get(), new WandParts(cap, rod, sceptre)), Map.of()), aspects);
    }

    private static AspectList addAll(AspectList into, AspectList from) {
        AspectList result = into;
        for (var entry : from.entries()) {
            result = result.add(entry.aspect(), entry.amount());
        }
        return result;
    }
}
