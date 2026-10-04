package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.*;
import com.leclowndu93150.thaumaturge.api.recipe.*;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TCWorkbenchSources;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class WorkbenchPayment {

    private WorkbenchPayment() {}

    public static Plan plan(IArcaneRecipe recipe, IArcaneWorkbench inventory, Player player) {
        return plan(recipe, inventory, player, null, null);
    }

    public static Plan plan(IArcaneRecipe recipe, IArcaneWorkbench inventory, Player player, @Nullable ArcaneWorkbenchContext context, @Nullable TransactionContext outer) {
        ItemStack wand = inventory.wandStack();
        boolean hasWand = wand.getItem() instanceof ItemWand;

        // Primal vis cost calculation
        Map<ResourceKey<IAspect>, Integer> wandCentivis = new LinkedHashMap<>();
        Map<ResourceKey<IAspect>, Integer> sourceCentivis = new LinkedHashMap<>();
        AspectList crystalNeeds = calculateCrystalNeeds(recipe, inventory, player, context, outer, hasWand, wand, wandCentivis, sourceCentivis);

        // Vis cost calculation
        boolean fullWand = hasWand && crystalNeeds.isEmpty() && sourceCentivis.isEmpty();
        float modifier;
        if (fullWand) {
            // Wand-only craft: discount comes from the wand's caps (averaged across primals).
            modifier = averageCraftModifier(wand, player);
        } else if (!crystalNeeds.isEmpty()) {
            // Crystal fallback in play: apply the aura surcharge on top of gear discounts.
            modifier = WandEconomy.CRAFT_AURA_SURCHARGE * gearModifier(player);
        } else {
            // Source-paid (no wand, no crystals): only gear discounts apply.
            modifier = gearModifier(player);
        }
        int auraVis = recipe.visCost() <= 0 ? 0 : Math.max(1, Mth.ceil(recipe.visCost() * modifier));

        // Plan making
        Plan plan = new Plan(fullWand, wandCentivis, sourceCentivis, crystalNeeds, auraVis, hasCrystals(inventory, crystalNeeds));
        return applyCostEvent(recipe, inventory, player, plan);
    }

    private static AspectList calculateCrystalNeeds(IArcaneRecipe recipe, IArcaneWorkbench inventory, Player player, @Nullable ArcaneWorkbenchContext context, @Nullable TransactionContext outer, boolean hasWand, ItemStack wand, Map<ResourceKey<IAspect>, Integer> wandCentivis, Map<ResourceKey<IAspect>, Integer> sourceCentivis) {
        AspectList crystalNeeds = AspectList.EMPTY;
        for (AspectInstance entry : recipe.crystalCost().entries()) {
            ResourceKey<IAspect> primal = entry.aspect().getKey();
            int centivis = entry.amount() * WandEconomy.CRYSTAL_SUBSTITUTE_VIS * WandEconomy.CENTIVIS_PER_VIS;
            Map<ResourceKey<IAspect>, Integer> single = new LinkedHashMap<>();
            single.put(primal, centivis);
            if (hasWand && WandVisHelper.consumeAllVisRaw(wand, single, true)) { // Pay from the wand
                wandCentivis.put(primal, centivis);
            } else if (canSupplyFromSources(context, player, inventory, entry.aspect(), centivis, outer)) { // Pay from external sources
                sourceCentivis.put(primal, centivis);
            } else {
                crystalNeeds = crystalNeeds.add(entry.aspect(), entry.amount()); // Pay from the inventory
            }
        }
        return crystalNeeds;
    }

    private static Plan applyCostEvent(IArcaneRecipe recipe, IArcaneWorkbench inventory, Player player, Plan plan) {
        ArcaneCraftCost initial = plan.cost();
        ArcaneCraftCostEvent event = new ArcaneCraftCostEvent(recipe, inventory, player, initial);
        NeoForge.EVENT_BUS.post(event);
        ArcaneCraftCost result = event.getCost();
        if (result == initial) {
            return plan;
        }
        return new Plan(result.paidFromWand(), result.wandCentivis(), plan.sourceCentivis(), result.crystalsNeeded(), result.auraVis(),
                result.affordable() && hasCrystals(inventory, result.crystalsNeeded()));
    }

    public static ArcaneCraftCost cost(IArcaneRecipe recipe, IArcaneWorkbench workbench, Player player, @Nullable ArcaneWorkbenchContext context) {
        return plan(recipe, workbench, player, context, null).cost();
    }

    public static @Nullable ItemStack paidWand(Plan plan, ItemStack wand) {
        ItemStack paid = wand.copy();
        if (plan.wandCentivis().isEmpty()) {
            return paid;
        }
        return WandVisHelper.consumeAllVisRaw(paid, plan.wandCentivis(), false) ? paid : null;
    }

    public static boolean paySources(Plan plan, ArcaneWorkbenchContext context, ServerPlayer player, IArcaneWorkbench inventory, TransactionContext transaction) {
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : plan.sourceCentivis().entrySet()) {
            Holder<IAspect> aspect = Aspects.resolve(player.level(), entry.getKey());
            if (aspect == null || supplyFromSources(context, player, inventory, aspect, entry.getValue(), transaction) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public static boolean payAura(Plan plan, ArcaneWorkbenchContext context, ServerPlayer player, IArcaneWorkbench inventory, TransactionContext transaction) {
        if (plan.auraVis() <= 0) {
            return true;
        }
        BlockEntityArcaneWorkbench tile = placedWorkbench(context);
        if (tile != null) {
            return tile.spendAura(plan.auraVis(), transaction);
        }
        int remaining = plan.auraVis();
        for (IWorkbenchAuraSource source : TCWorkbenchSources.auraSources()) {
            if (remaining <= 0) {
                break;
            }
            remaining -= clampSupply(source.supply(context, player, inventory, remaining, transaction), remaining);
        }
        return remaining <= 0;
    }

    private static @Nullable BlockEntityArcaneWorkbench placedWorkbench(ArcaneWorkbenchContext context) {
        return context.blockPosition().map(context.level()::getBlockEntity).filter(BlockEntityArcaneWorkbench.class::isInstance).map(BlockEntityArcaneWorkbench.class::cast).orElse(null);
    }

    private static boolean canSupplyFromSources(@Nullable ArcaneWorkbenchContext context, Player player, IArcaneWorkbench inventory, Holder<IAspect> aspect, int need, @Nullable TransactionContext outer) {
        if (context == null || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        try (Transaction simulation = Transaction.open(outer)) {
            return supplyFromSources(context, serverPlayer, inventory, aspect, need, simulation) >= need;
        }
    }

    private static int supplyFromSources(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneWorkbench inventory, Holder<IAspect> aspect, int need, TransactionContext transaction) {
        int supplied = 0;
        for (IWorkbenchVisSource source : TCWorkbenchSources.visSources()) {
            if (supplied >= need) {
                break;
            }
            supplied += clampSupply(source.supply(context, player, inventory, aspect, need - supplied, transaction), need - supplied);
        }
        return supplied;
    }

    private static int clampSupply(int supplied, int need) {
        return Math.max(0, Math.min(need, supplied));
    }

    public static int crudeCost(IArcaneRecipe recipe) {
        return Mth.ceil(recipe.visCost() * WandEconomy.CRAFT_AURA_SURCHARGE);
    }

    private static float averageCraftModifier(ItemStack wand, Player player) {
        float total = 0.0F;
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            total += WandVisHelper.getConsumptionModifier(wand, player, primal, true);
        }
        return total / WandEconomy.PRIMAL_COUNT;
    }

    private static float gearModifier(Player player) {
        return Math.max(1.0F - CasterManager.getTotalVisDiscount(player), WandEconomy.MIN_CONSUMPTION_MODIFIER);
    }

    private static boolean hasCrystals(IArcaneWorkbench inventory, AspectList needs) {
        for (AspectInstance entry : needs.entries()) {
            int found = 0;
            for (AspectInstance crystalEntry : inventory.availableCrystals().entries()) {
                Holder<IAspect> holder = crystalEntry.aspect();
                if (holder != null && holder.value().tag().equals(entry.aspect().value().tag())) {
                    found += crystalEntry.amount();
                }
            }
            if (found < entry.amount()) {
                return false;
            }
        }
        return true;
    }

    public record Plan(boolean fullWand, Map<ResourceKey<IAspect>, Integer> wandCentivis, Map<ResourceKey<IAspect>, Integer> sourceCentivis, AspectList crystalsToConsume, int auraVis,
            boolean crystalsSatisfied) {

        public ArcaneCraftCost cost() {
            return new ArcaneCraftCost(fullWand, wandCentivis, crystalsToConsume, auraVis, crystalsSatisfied);
        }
    }
}
