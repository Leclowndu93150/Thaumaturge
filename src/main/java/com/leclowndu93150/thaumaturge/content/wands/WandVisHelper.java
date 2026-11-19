package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.IWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class WandVisHelper {

    private WandVisHelper() {}

    public static WandParts getParts(ItemStack stack) {
        WandParts parts = stack.get(TTDataComponents.WAND_PARTS.get());
        return parts != null ? parts : WandParts.starter();
    }

    public static WandVis getAllVis(ItemStack stack) {
        IWandVisStorage storage = getParts(stack).rod().visStorage();
        if (storage != null) {
            return storage.getVis(stack);
        }
        WandVis vis = stack.get(TTDataComponents.WAND_VIS.get());
        return vis != null ? vis : WandVis.EMPTY;
    }

    public static void setAllVis(ItemStack stack, WandVis vis) {
        IWandVisStorage storage = getParts(stack).rod().visStorage();
        if (storage != null) {
            storage.setVis(stack, vis);
        } else {
            stack.set(TTDataComponents.WAND_VIS.get(), vis);
        }
    }

    public static int getMaxVis(ItemStack stack) {
        return getParts(stack).maxCentivis();
    }

    public static int getVis(ItemStack stack, ResourceKey<IAspect> aspect) {
        return getAllVis(stack).amount(aspect);
    }

    public static void storeVis(ItemStack stack, ResourceKey<IAspect> aspect, int centivis) {
        setAllVis(stack, getAllVis(stack).with(aspect, centivis));
    }

    public static int addVis(ItemStack stack, ResourceKey<IAspect> aspect, int vis, boolean doit) {
        // Keep whole-vis transfers atomic: a half-vis gap cannot accept one whole vis.
        long offered = (long) vis * WandEconomy.CENTIVIS_PER_VIS;
        return (int) (addCentivis(stack, aspect, offered, WandEconomy.CENTIVIS_PER_VIS, doit)
                / WandEconomy.CENTIVIS_PER_VIS);
    }

    public static int addRealVis(ItemStack stack, ResourceKey<IAspect> aspect, int centivis, boolean doit) {
        return (int) addCentivis(stack, aspect, centivis, 1, doit);
    }

    private static long addCentivis(
            ItemStack stack, ResourceKey<IAspect> aspect, long offered, int step, boolean doit) {
        if (!TTAspects.PRIMALS.contains(aspect)) {
            return 0L;
        }
        int stored = getVis(stack, aspect);
        if (offered < 0L) {
            // Preserve the historical negative-add (drain) behavior without wrapping below zero.
            if (doit) {
                storeVis(stack, aspect, WandVisNumbers.clampToStorage((long) stored + offered));
            }
            return 0L;
        }
        long accepted = WandVisNumbers.accepted(stored, getMaxVis(stack), offered, step);
        if (doit && accepted > 0L) {
            storeVis(stack, aspect, (int) (stored + accepted));
        }
        return offered - accepted;
    }

    public static float getConsumptionModifier(
            ItemStack stack, @Nullable Player player, ResourceKey<IAspect> aspect, boolean crafting) {
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

    public static boolean drainOne(
            ItemStack stack, @Nullable Player player, ResourceKey<IAspect> aspect, int centivis, boolean crafting) {
        long owed = scaledCost(stack, player, aspect, centivis, crafting);
        int available = getVis(stack, aspect);
        boolean affordable = available >= owed;
        if (affordable) {
            storeVis(stack, aspect, WandVisNumbers.clampToStorage((long) available - owed));
        }
        return affordable;
    }

    private static long scaledCost(
            ItemStack stack, @Nullable Player player, ResourceKey<IAspect> aspect, int base, boolean crafting) {
        return WandVisNumbers.scaledCost(base, getConsumptionModifier(stack, player, aspect, crafting));
    }

    public static boolean payCosts(
            ItemStack stack,
            @Nullable Player player,
            Map<ResourceKey<IAspect>, Integer> centivisCosts,
            boolean doit,
            boolean crafting) {
        if (centivisCosts.isEmpty()) {
            return false;
        }
        WandVis before = getAllVis(stack);
        WandVis after = before;
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : centivisCosts.entrySet()) {
            ResourceKey<IAspect> aspect = entry.getKey();
            long owed = scaledCost(stack, player, aspect, entry.getValue(), crafting);
            int held = before.amount(aspect);
            if (held < owed) {
                return false;
            }
            after = after.with(aspect, WandVisNumbers.clampToStorage((long) after.amount(aspect) - owed));
        }
        if (doit) {
            setAllVis(stack, after);
        }
        return true;
    }

    public static boolean payExact(
            ItemStack stack, Map<ResourceKey<IAspect>, Integer> centivisCosts, boolean simulate) {
        if (centivisCosts.isEmpty()) {
            return false;
        }
        WandVis before = getAllVis(stack);
        WandVis after = before;
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : centivisCosts.entrySet()) {
            ResourceKey<IAspect> aspect = entry.getKey();
            if (before.amount(aspect) < entry.getValue()) {
                return false;
            }
            after = after.with(aspect, WandVisNumbers.clampToStorage((long) after.amount(aspect) - entry.getValue()));
        }
        if (!simulate) {
            setAllVis(stack, after);
        }
        return true;
    }

    private static final int HOTBAR_SIZE = 9;

    public static Map<ResourceKey<IAspect>, Integer> evenSplit(int centivis) {
        Map<ResourceKey<IAspect>, Integer> split = new LinkedHashMap<>();
        int base = centivis / WandEconomy.PRIMAL_COUNT;
        int remainder = centivis % WandEconomy.PRIMAL_COUNT;
        for (int i = 0; i < TTAspects.PRIMALS.size(); i++) {
            int share = base + (i < remainder ? 1 : 0);
            if (share > 0) {
                split.put(TTAspects.PRIMALS.get(i), share);
            }
        }
        return split;
    }

    public static boolean consumeSpecificFromHotbar(
            Player player, Map<ResourceKey<IAspect>, Integer> centivisCosts, boolean doit) {
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof ItemWand && payCosts(stack, player, centivisCosts, doit, true)) {
                return true;
            }
        }
        return false;
    }

    public static Map<ResourceKey<IAspect>, Integer> primalSplit(int centivis, AspectList aspects) {
        Map<ResourceKey<IAspect>, Integer> weights = WandChargingEvents.reduceToPrimals(aspects);
        long totalWeight = 0L;
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            totalWeight += weights.getOrDefault(primal, 0);
        }
        if (totalWeight <= 0) {
            return evenSplit(centivis);
        }
        Map<ResourceKey<IAspect>, Integer> split = new LinkedHashMap<>();
        long remainder = centivis;
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            int weight = weights.getOrDefault(primal, 0);
            if (weight > 0) {
                int share = (int) ((long) centivis * weight / totalWeight);
                split.put(primal, share);
                remainder -= share;
            }
        }
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : split.entrySet()) {
            if (remainder <= 0) {
                break;
            }
            entry.setValue(entry.getValue() + 1);
            remainder--;
        }
        split.values().removeIf(share -> share <= 0);
        return split;
    }

    public static boolean consumeVisFromHotbar(
            Player player, float vis, @Nullable ResourceKey<IAspect> aspect, boolean doit) {
        if (vis <= 0.0F) {
            return true;
        }
        AspectList aspects = aspect == null
                ? AspectList.EMPTY
                : player.level()
                        .registryAccess()
                        .lookupOrThrow(IAspect.REGISTRY_KEY)
                        .get(aspect)
                        .map(holder -> AspectList.EMPTY.add(holder, 1))
                        .orElse(AspectList.EMPTY);
        double centivis = (double) vis * WandEconomy.CENTIVIS_PER_VIS;
        if (!Double.isFinite(centivis) || centivis > Integer.MAX_VALUE) {
            return false;
        }
        Map<ResourceKey<IAspect>, Integer> split = primalSplit((int) Math.round(centivis), aspects);
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof ItemWand && payCosts(stack, player, split, doit, false)) {
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
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            vis = vis.with(primal, max);
        }
        setAllVis(stack, vis);
    }
}
