package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.IWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.ITransactionalWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.api.items.VisDiscountAccess;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class WandVisHelper {
    private static final int HOTBAR_SIZE = 9;
    private static final int WHITE_RGB = 0xFFFFFF;
    private static final int RGB_MASK = 0xFFFFFF;
    private static final int PRIMAL_COUNT = WandEconomy.PRIMAL_COUNT;
    private static final int CENTIVIS_PER_VIS = WandEconomy.CENTIVIS_PER_VIS;

    private WandVisHelper() {}

    public static WandParts partsOf(ItemStack wand) {
        WandParts parts = wand.get(TTDataComponents.WAND_PARTS.get());
        return parts == null ? WandParts.starter() : parts;
    }

    public static WandVis chargeOf(ItemStack wand) {
        IWandVisStorage override = partsOf(wand).rod().visStorage();
        return override != null ? override.getVis(wand) : wand.getOrDefault(TTDataComponents.WAND_VIS.get(), WandVis.EMPTY);
    }

    public static void writeCharge(ItemStack wand, WandVis vis) {
        IWandVisStorage override = partsOf(wand).rod().visStorage();
        if (override == null) {
            wand.set(TTDataComponents.WAND_VIS.get(), vis);
            return;
        }
        override.setVis(wand, vis);
    }

    public static int capacityOf(ItemStack wand) {
        return partsOf(wand).maxCentivis();
    }

    public static int storedIn(ItemStack wand, ResourceKey<IAspect> aspect) {
        return chargeOf(wand).amount(aspect);
    }

    public static void setStored(ItemStack wand, ResourceKey<IAspect> aspect, int centivis) {
        writeCharge(wand, chargeOf(wand).with(aspect, centivis));
    }

    public static int topUp(ItemStack wand, ResourceKey<IAspect> aspect, int vis, boolean apply) {
        int overflowCentivis = topUpCentivis(wand, aspect, vis * CENTIVIS_PER_VIS, apply);
        return overflowCentivis / CENTIVIS_PER_VIS;
    }

    public static int topUpCentivis(ItemStack wand, ResourceKey<IAspect> aspect, int centivis, boolean apply) {
        if (centivis <= 0) {
            return 0;
        }
        if (!TTAspects.PRIMALS.contains(aspect)) {
            return centivis;
        }
        IWandVisStorage storage = partsOf(wand).rod().visStorage();
        int capacity = storage instanceof ITransactionalWandVisStorage transactional ? transactional.capacityCentivis(wand, aspect) : capacityOf(wand);
        int stored = storedIn(wand, aspect);
        int accepted = (int) Math.min(centivis, Math.max(0L, (long) capacity - stored));
        if (apply && accepted > 0) {
            setStored(wand, aspect, stored + accepted);
        }
        return centivis - accepted;
    }

    public static float costFactor(ItemStack wand, @Nullable Player player, ResourceKey<IAspect> aspect, boolean crafting) {
        return costFactor(wand, player, aspect, crafting, CENTIVIS_PER_VIS);
    }

    public static float costFactor(ItemStack wand, @Nullable Player player, ResourceKey<IAspect> aspect, boolean crafting, int baseCentivis) {
        WandParts parts = partsOf(wand);
        float capBase = parts.cap().costModifier(aspect) - VisDiscountAccess.additionalDiscount(player, aspect, crafting, baseCentivis);
        return modifier(parts, player, capBase);
    }

    public static float scalarCostFactor(ItemStack wand, @Nullable Player player, ResourceKey<IAspect> aspect) {
        WandParts parts = partsOf(wand);
        return modifier(parts, player, parts.cap().costModifier(aspect));
    }

    static float modifier(WandParts parts, @Nullable Player player, float base) {
        float afterPlayer = player == null ? base : base - CasterManager.visDiscountOf(player);
        float afterSceptre = parts.sceptre() ? afterPlayer - WandEconomy.SCEPTRE_DISCOUNT : afterPlayer;
        return Math.max(WandEconomy.MIN_CONSUMPTION_MODIFIER, afterSceptre);
    }

    public static boolean drainOne(ItemStack wand, Player player, ResourceKey<IAspect> aspect, int centivis, boolean crafting) {
        int owed = scaledCost(wand, player, aspect, centivis, crafting);
        int available = storedIn(wand, aspect);
        boolean affordable = available >= owed;
        if (affordable) {
            setStored(wand, aspect, available - owed);
        }
        return affordable;
    }

    public static int scaledCost(ItemStack wand, @Nullable Player player, ResourceKey<IAspect> aspect, int base, boolean crafting) {
        return (int) (base * costFactor(wand, player, aspect, crafting, base));
    }

    public static boolean payCosts(ItemStack wand, Player player, Map<ResourceKey<IAspect>, Integer> costs, boolean apply, boolean crafting) {
        return payCosts(wand, player, costs, apply, crafting, true);
    }

    public static boolean payScalarCosts(ItemStack wand, Player player, Map<ResourceKey<IAspect>, Integer> costs, boolean apply, boolean crafting) {
        return payCosts(wand, player, costs, apply, crafting, false);
    }

    private static boolean payCosts(ItemStack wand, Player player, Map<ResourceKey<IAspect>, Integer> costs, boolean apply, boolean crafting, boolean aspectDiscounts) {
        if (costs.isEmpty()) {
            return false;
        }
        WandVis before = chargeOf(wand);
        WandVis after = before;
        for (Map.Entry<ResourceKey<IAspect>, Integer> line : costs.entrySet()) {
            ResourceKey<IAspect> aspect = line.getKey();
            int owed = aspectDiscounts ? scaledCost(wand, player, aspect, line.getValue(), crafting) : (int) (line.getValue() * scalarCostFactor(wand, player, aspect));
            int held = before.amount(aspect);
            if (held < owed) {
                return false;
            }
            after = after.with(aspect, after.amount(aspect) - owed);
        }
        if (apply) {
            writeCharge(wand, after);
        }
        return true;
    }

    public static boolean payExact(ItemStack wand, Map<ResourceKey<IAspect>, Integer> costs, boolean simulate) {
        if (costs.isEmpty()) {
            return false;
        }
        WandVis before = chargeOf(wand);
        WandVis after = before;
        for (Map.Entry<ResourceKey<IAspect>, Integer> line : costs.entrySet()) {
            ResourceKey<IAspect> aspect = line.getKey();
            if (before.amount(aspect) < line.getValue()) {
                return false;
            }
            after = after.with(aspect, after.amount(aspect) - line.getValue());
        }
        if (!simulate) {
            writeCharge(wand, after);
        }
        return true;
    }

    public static Map<ResourceKey<IAspect>, Integer> evenSplit(int centivis) {
        int floorShare = centivis / PRIMAL_COUNT;
        int bonusRecipients = centivis % PRIMAL_COUNT;
        Map<ResourceKey<IAspect>, Integer> result = new LinkedHashMap<>();
        int position = 0;
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            int amount = position < bonusRecipients ? floorShare + 1 : floorShare;
            position++;
            if (amount > 0) {
                result.put(primal, amount);
            }
        }
        return result;
    }

    public static Map<ResourceKey<IAspect>, Integer> primalSplit(int centivis, AspectList aspects) {
        Map<ResourceKey<IAspect>, Integer> reduced = WandChargingEvents.reduceToPrimals(aspects);
        Map<ResourceKey<IAspect>, Integer> weights = new LinkedHashMap<>();
        long weightSum = 0L;
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            int weight = reduced.getOrDefault(primal, 0);
            if (weight > 0) {
                weights.put(primal, weight);
                weightSum += weight;
            }
        }
        if (weightSum <= 0L) {
            return evenSplit(centivis);
        }
        Map<ResourceKey<IAspect>, Integer> result = new LinkedHashMap<>();
        long handedOut = 0L;
        for (Map.Entry<ResourceKey<IAspect>, Integer> weighted : weights.entrySet()) {
            int portion = (int) ((long) centivis * weighted.getValue() / weightSum);
            handedOut += portion;
            result.put(weighted.getKey(), portion);
        }
        long leftover = centivis - handedOut;
        for (Map.Entry<ResourceKey<IAspect>, Integer> slot : result.entrySet()) {
            if (leftover <= 0L) {
                break;
            }
            slot.setValue(slot.getValue() + 1);
            leftover--;
        }
        result.values().removeIf(portion -> portion <= 0);
        return result;
    }

    public static boolean consumeSpecificFromHotbar(Player player, Map<ResourceKey<IAspect>, Integer> costs, boolean apply) {
        return firstHotbarWand(player, wand -> payCosts(wand, player, costs, apply, true)) != null;
    }

    public static boolean consumeVisFromHotbar(Player player, float amount, @Nullable ResourceKey<IAspect> aspect, boolean apply) {
        if (amount <= 0.0F) {
            return true;
        }
        AspectList basis = unitOf(player.level().registryAccess(), aspect);
        Map<ResourceKey<IAspect>, Integer> split = primalSplit(Math.round(amount * CENTIVIS_PER_VIS), basis);
        if (split.isEmpty()) {
            return false;
        }
        return firstHotbarWand(player, wand -> aspect == null ? payScalarCosts(wand, player, split, apply, false) : payCosts(wand, player, split, apply, false)) != null;
    }

    private static @Nullable ItemStack firstHotbarWand(Player player, Predicate<ItemStack> accepts) {
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.getItem() instanceof ItemWand && accepts.test(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static Optional<Holder<IAspect>> holderOf(HolderLookup.Provider registries, ResourceKey<IAspect> aspect) {
        return registries.lookup(IAspect.REGISTRY_KEY).flatMap(lookup -> lookup.get(aspect)).map(found -> (Holder<IAspect>) found);
    }

    private static AspectList unitOf(HolderLookup.Provider registries, @Nullable ResourceKey<IAspect> aspect) {
        if (aspect == null) {
            return AspectList.EMPTY;
        }
        return holderOf(registries, aspect).map(holder -> AspectList.of(new AspectInstance(holder, 1))).orElse(AspectList.EMPTY);
    }

    public static ItemStack findWandInHotbarWithRoom(Player player, ResourceKey<IAspect> aspect, int vis) {
        ItemStack found = firstHotbarWand(player, wand -> topUp(wand, aspect, vis, false) < vis);
        return found == null ? ItemStack.EMPTY : found;
    }

    public static void fill(ItemStack wand) {
        int capacity = capacityOf(wand);
        WandVis full = WandVis.EMPTY;
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            full = full.with(primal, capacity);
        }
        writeCharge(wand, full);
    }

    static int colorOf(HolderLookup.Provider registries, ResourceKey<IAspect> aspect) {
        return holderOf(registries, aspect).map(holder -> holder.value().color() & RGB_MASK).orElse(WHITE_RGB);
    }
}
