package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class WandVisHelper {

    private WandVisHelper() {}

    public static WandParts getParts(ItemStack stack) {
        WandParts parts = stack.get(TCDataComponents.WAND_PARTS.get());
        return parts != null ? parts : WandParts.starter();
    }

    public static WandVis getAllVis(ItemStack stack) {
        WandVis vis = stack.get(TCDataComponents.WAND_VIS.get());
        return vis != null ? vis : WandVis.EMPTY;
    }

    public static int getMaxVis(ItemStack stack) {
        return getParts(stack).maxCentivis();
    }

    public static int getVis(ItemStack stack, ResourceKey<IAspect> aspect) {
        return getAllVis(stack).amount(aspect);
    }

    public static void storeVis(ItemStack stack, ResourceKey<IAspect> aspect, int centivis) {
        stack.set(TCDataComponents.WAND_VIS.get(), getAllVis(stack).with(aspect, centivis));
    }

    public static int addVis(ItemStack stack, ResourceKey<IAspect> aspect, int vis, boolean doit) {
        return addRealVis(stack, aspect, vis * WandEconomy.CENTIVIS_PER_VIS, doit) / WandEconomy.CENTIVIS_PER_VIS;
    }

    public static int addRealVis(ItemStack stack, ResourceKey<IAspect> aspect, int centivis, boolean doit) {
        if (!TCAspects.PRIMALS.contains(aspect)) {
            return 0;
        }
        int stored = getVis(stack, aspect) + centivis;
        int max = getMaxVis(stack);
        int leftover = Math.max(stored - max, 0);
        if (doit) {
            storeVis(stack, aspect, Math.min(stored, max));
        }
        return leftover;
    }

    public static float getConsumptionModifier(ItemStack stack, @Nullable Player player, ResourceKey<IAspect> aspect, boolean crafting) {
        WandParts parts = getParts(stack);
        float modifier = parts.cap().costModifier(aspect);
        if (player != null) {
            modifier -= CasterManager.getTotalVisDiscount(player);
        }
        if (parts.sceptre()) {
            modifier -= WandEconomy.SCEPTRE_DISCOUNT;
        }
        return Math.max(modifier, WandEconomy.MIN_CONSUMPTION_MODIFIER);
    }

    public static boolean consumeVis(ItemStack stack, @Nullable Player player, ResourceKey<IAspect> aspect, int centivis, boolean crafting) {
        int cost = (int) (centivis * getConsumptionModifier(stack, player, aspect, crafting));
        if (getVis(stack, aspect) < cost) {
            return false;
        }
        storeVis(stack, aspect, getVis(stack, aspect) - cost);
        return true;
    }

    public static boolean consumeAllVis(ItemStack stack, @Nullable Player player, Map<ResourceKey<IAspect>, Integer> centivisCosts, boolean doit, boolean crafting) {
        if (centivisCosts.isEmpty()) {
            return false;
        }
        Map<ResourceKey<IAspect>, Integer> modified = new LinkedHashMap<>();
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : centivisCosts.entrySet()) {
            int cost = (int) (entry.getValue() * getConsumptionModifier(stack, player, entry.getKey(), crafting));
            modified.put(entry.getKey(), cost);
        }
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : modified.entrySet()) {
            if (getVis(stack, entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        if (doit) {
            for (Map.Entry<ResourceKey<IAspect>, Integer> entry : modified.entrySet()) {
                storeVis(stack, entry.getKey(), getVis(stack, entry.getKey()) - entry.getValue());
            }
        }
        return true;
    }

    public static boolean consumeAllVisRaw(ItemStack stack, Map<ResourceKey<IAspect>, Integer> centivisCosts, boolean simulate) {
        if (centivisCosts.isEmpty()) {
            return false;
        }
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : centivisCosts.entrySet()) {
            if (getVis(stack, entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        if (!simulate) {
            for (Map.Entry<ResourceKey<IAspect>, Integer> entry : centivisCosts.entrySet()) {
                storeVis(stack, entry.getKey(), getVis(stack, entry.getKey()) - entry.getValue());
            }
        }
        return true;
    }

    private static final int HOTBAR_SIZE = 9;

    public static Map<ResourceKey<IAspect>, Integer> evenSplit(int centivis) {
        Map<ResourceKey<IAspect>, Integer> split = new LinkedHashMap<>();
        int base = centivis / WandEconomy.PRIMAL_COUNT;
        int remainder = centivis % WandEconomy.PRIMAL_COUNT;
        for (int i = 0; i < TCAspects.PRIMALS.size(); i++) {
            int share = base + (i < remainder ? 1 : 0);
            if (share > 0) {
                split.put(TCAspects.PRIMALS.get(i), share);
            }
        }
        return split;
    }

    public static boolean consumeSpecificFromHotbar(Player player, Map<ResourceKey<IAspect>, Integer> centivisCosts, boolean doit) {
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof ItemWand && consumeAllVis(stack, player, centivisCosts, doit, true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean consumeVisFromHotbar(Player player, float vis, boolean doit) {
        if (vis <= 0.0F) {
            return true;
        }
        Map<ResourceKey<IAspect>, Integer> split = evenSplit(Math.round(vis * WandEconomy.CENTIVIS_PER_VIS));
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof ItemWand && consumeAllVis(stack, player, split, doit, false)) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack findWandInHotbarWithRoom(Player player, ResourceKey<IAspect> aspect, int vis) {
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof ItemWand && addVis(stack, aspect, vis, false) < vis) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void fill(ItemStack stack) {
        int max = getMaxVis(stack);
        WandVis vis = WandVis.EMPTY;
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            vis = vis.with(primal, max);
        }
        stack.set(TCDataComponents.WAND_VIS.get(), vis);
    }
}
