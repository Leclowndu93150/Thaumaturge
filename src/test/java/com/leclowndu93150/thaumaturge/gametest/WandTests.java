package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import net.minecraft.world.item.ItemStack;

public final class WandTests {
    private WandTests() {}

    public static void register(TTTestRegistrar r) {
        r.add("wand/vis_store_roundtrip", 20, helper -> {
            ItemStack wand = new ItemStack(TTItems.WAND.get());
            int leftover = WandVisHelper.addVis(wand, TTAspects.IGNIS, 50, true);
            int storedCentivis = WandVisHelper.getVis(wand, TTAspects.IGNIS);
            int expected = (50 - leftover) * WandEconomy.CENTIVIS_PER_VIS;
            if (storedCentivis != expected) {
                helper.fail("Stored " + storedCentivis + " centivis, expected " + expected);
                return;
            }
            if (storedCentivis <= 0) {
                helper.fail("Wand stored no vis at all");
                return;
            }
            helper.succeed();
        });

        r.add("wand/capacity_enforced", 20, helper -> {
            ItemStack wand = new ItemStack(TTItems.WAND.get());
            WandParts parts = wand.getOrDefault(TTDataComponents.WAND_PARTS.get(), WandParts.starter());
            int capacity = parts.maxCentivis();
            int flood = capacity * 2;
            int leftover = WandVisHelper.addVis(wand, TTAspects.IGNIS, flood, true);
            if (leftover <= 0) {
                helper.fail("Flooding " + flood + " vis into capacity " + capacity + " left no remainder");
                return;
            }
            helper.succeed();
        });

        r.add("wand/aspect_index_varies_by_parts", 20, helper -> {
            ItemStack ironWood = new ItemStack(TTItems.WAND.get());
            ironWood.set(TTDataComponents.WAND_PARTS.get(), new WandParts(TTWandParts.CAP_IRON.get(), TTWandParts.ROD_WOOD.get(), false));
            ItemStack goldGreatwood = new ItemStack(TTItems.WAND.get());
            goldGreatwood.set(TTDataComponents.WAND_PARTS.get(), new WandParts(TTWandParts.CAP_GOLD.get(), TTWandParts.ROD_GREATWOOD.get(), false));
            var first = AspectIndexHolder.getConcrete().of(ironWood);
            var second = AspectIndexHolder.getConcrete().of(goldGreatwood);
            if (first.isEmpty() || second.isEmpty()) {
                helper.fail("Wand variant aspects missing from the index");
                return;
            }
            if (first.equals(second)) {
                helper.fail("Different cap/rod combinations produced identical aspect lists");
                return;
            }
            helper.succeed();
        });
    }
}
