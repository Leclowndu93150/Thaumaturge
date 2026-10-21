package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.items.VisDiscountAccess;
import com.leclowndu93150.thaumaturge.api.items.VisDiscountEvent;
import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.api.wands.WandPayment;
import com.leclowndu93150.thaumaturge.api.wands.WandSlot;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTAttributes;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

public final class VisDiscountTests {
    private VisDiscountTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("wand/discounted_payment_revalidation_and_rollback", 20, helper -> {
            ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "discount_payment"));
            player.getAttribute(TTAttributes.VIS_DISCOUNT).setBaseValue(0.05);
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            ItemStack wand = chargedWand(false);
            WandSlot slot = new WandSlot() {
                @Override
                public ItemStack stack() {
                    return wand;
                }

                @Override
                public boolean mayAccess(ServerPlayer actor) {
                    return actor == player;
                }

                @Override
                public void changed() {}
            };
            Consumer<VisDiscountEvent> listener = discountListener(player, 0.03F);
            NeoForge.EVENT_BUS.addListener(listener);
            try {
                var costs = Map.of(TTAspects.AER, 1000, TTAspects.IGNIS, 1000);
                var before = WandAccess.getAllVis(wand);
                WandPayment.Quote quote = WandPayment.quote(player, slot, costs, WandPayment.CostKind.CASTING).orElseThrow();
                if (Math.abs(quote.costs().get(TTAspects.AER) - 920) > 1 || quote.costs().get(TTAspects.IGNIS) != 950 || !WandAccess.getAllVis(wand).equals(before)) {
                    helper.fail("Mixed payment did not apply generic discount once and aspect discount only to aer");
                    return;
                }
                try (Transaction transaction = Transaction.openRoot()) {
                    if (!WandPayment.tryPay(player, slot, quote, transaction)) {
                        helper.fail("Discounted payment was refused");
                        return;
                    }
                }
                if (!WandAccess.getAllVis(wand).equals(before)) {
                    helper.fail("Aborted discounted payment did not restore both aspects");
                    return;
                }
                player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                try (Transaction transaction = Transaction.openRoot()) {
                    if (WandPayment.tryPay(player, slot, quote, transaction)) {
                        helper.fail("Payment accepted a quote after discount gear was removed");
                        return;
                    }
                    transaction.commit();
                }
                if (!WandAccess.getAllVis(wand).equals(before)) {
                    helper.fail("Rejected equipment change modified storage");
                    return;
                }
                player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                WandVisHelper.setStored(wand, TTAspects.IGNIS, 949);
                if (WandPayment.quote(player, slot, costs, WandPayment.CostKind.CASTING).isPresent() || WandAccess.getVis(wand, TTAspects.AER) != 2000) {
                    helper.fail("Insufficient ignis allowed a partial discounted payment");
                    return;
                }
                WandVisHelper.setStored(wand, TTAspects.IGNIS, 2000);
                quote = WandPayment.quote(player, slot, costs, WandPayment.CostKind.CASTING).orElseThrow();
                try (Transaction transaction = Transaction.openRoot()) {
                    if (!WandPayment.tryPay(player, slot, quote, transaction)) {
                        helper.fail("Fresh discounted quote could not commit");
                        return;
                    }
                    transaction.commit();
                }
                if (WandAccess.getVis(wand, TTAspects.AER) != 2000 - quote.costs().get(TTAspects.AER) || WandAccess.getVis(wand, TTAspects.IGNIS) != 1050) {
                    helper.fail("Committed discount payment differed from its quote");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
            }
        });
        registrar.add("wand/discount_sceptre_minimum_and_scalar", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.getAttribute(TTAttributes.VIS_DISCOUNT).setBaseValue(0.05);
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            ItemStack sceptre = chargedWand(true);
            Consumer<VisDiscountEvent> listener = discountListener(player, 0.03F);
            NeoForge.EVENT_BUS.addListener(listener);
            try {
                if (Math.abs(WandVisHelper.scaledCost(sceptre, player, TTAspects.AER, 1000, true) - 820) > 1
                        || Math.abs(WandVisHelper.scaledCost(sceptre, player, TTAspects.IGNIS, 1000, true) - 850) > 1) {
                    helper.fail("Cap, generic, aspect and sceptre discounts did not compose");
                    return;
                }
                var costs = Map.of(TTAspects.AER, 1000, TTAspects.IGNIS, 1000);
                if (!WandVisHelper.payScalarCosts(sceptre, player, costs, true, true) || WandAccess.getVis(sceptre, TTAspects.AER) != WandAccess.getVis(sceptre, TTAspects.IGNIS)) {
                    helper.fail("A scalar split received an aspect-specific discount");
                    return;
                }
                player.getAttribute(TTAttributes.VIS_DISCOUNT).setBaseValue(1.0);
                if (WandVisHelper.scaledCost(sceptre, player, TTAspects.AER, 1000, true) != 100 || WandVisHelper.scaledCost(sceptre, player, TTAspects.IGNIS, 1000, true) != 100) {
                    helper.fail("Combined discounts bypassed minimum consumption");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
            }
        });
        if (ModList.get().isLoaded("curios")) {
            registerCurios(registrar);
        }
        registrar.add("wand/aspect_equipment_discount", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
            player.setItemSlot(EquipmentSlot.HEAD, helmet);
            Consumer<VisDiscountEvent> listener = event -> {
                if (event.player() == player && event.aspect().equals(TTAspects.AER) && event.stack().is(Items.LEATHER_HELMET)) {
                    event.stack().setCount(0);
                    event.addDiscount(0.03F);
                }
            };
            NeoForge.EVENT_BUS.addListener(listener);
            try {
                if (Math.abs(VisDiscountAccess.additionalDiscount(player, TTAspects.AER, false, 100) - 0.03F) > 0.00001F
                        || VisDiscountAccess.additionalDiscount(player, TTAspects.IGNIS, false, 100) != 0.0F || VisDiscountAccess.additionalDiscount(player, null, false, 100) != 0.0F
                        || helmet.getCount() != 1) {
                    helper.fail("Aspect discount escaped its aspect or mutated equipment");
                    return;
                }
                player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                player.getInventory().setItem(0, helmet);
                if (VisDiscountAccess.additionalDiscount(player, TTAspects.AER, false, 100) != 0.0F) {
                    helper.fail("Unequipped gear retained its discount");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
            }
        });
        registrar.add("wand/discount_context_and_bounds", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            VisDiscountEvent event = new VisDiscountEvent(player, new ItemStack(Items.LEATHER_HELMET), "equipment:head", TTAspects.AER, true, 123);
            event.addDiscount(0.75F);
            event.addDiscount(0.75F);
            if (event.discount() != 1.0F || !event.crafting() || event.baseCentivis() != 123 || !event.slot().equals("equipment:head")) {
                helper.fail("Discount context or saturation changed");
                return;
            }
            for (float invalid : new float[]{-0.03F, 3.0F, Float.NaN, Float.POSITIVE_INFINITY}) {
                try {
                    event.addDiscount(invalid);
                    helper.fail("Invalid discount fraction accepted");
                    return;
                } catch (IllegalArgumentException expected) {
                }
            }
            helper.succeed();
        });
    }

    private static void registerCurios(TTTestRegistrar registrar) {
        registrar.add("wand/discount_curios_slots", 20, helper -> {
            ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "discount_curios"));
            var inventory = CuriosApi.getCuriosInventory(player).orElseThrow();
            inventory.loadDatapacks();
            var ringSlots = inventory.getStacksHandler("ring").orElseThrow();
            var rings = ringSlots.getStacks();
            var extraSlots = TTIds.rl("test_discount_ring_slots");
            if (rings.getSlots() < 2) {
                ringSlots.addTransientModifier(new AttributeModifier(extraSlots, 2 - rings.getSlots(), AttributeModifier.Operation.ADD_VALUE));
                ringSlots.update();
            }
            if (rings.getSlots() < 2) {
                helper.fail("Curios ring slots were not initialized");
                return;
            }
            rings.setStackInSlot(0, new ItemStack(TTItems.RING_MUNDANE.get()));
            rings.setStackInSlot(1, new ItemStack(TTItems.RING_MUNDANE.get()));
            player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            Consumer<VisDiscountEvent> listener = discountListener(player, 0.03F);
            NeoForge.EVENT_BUS.addListener(listener);
            try {
                if (Math.abs(VisDiscountAccess.additionalDiscount(player, TTAspects.AER, false, 1000) - 0.09F) > 0.00001F
                        || VisDiscountAccess.additionalDiscount(player, TTAspects.IGNIS, false, 1000) != 0.0F) {
                    helper.fail("Armor and two functional Curios slots did not contribute exactly once");
                    return;
                }
                rings.setStackInSlot(0, ItemStack.EMPTY);
                if (Math.abs(VisDiscountAccess.additionalDiscount(player, TTAspects.AER, false, 1000) - 0.06F) > 0.00001F) {
                    helper.fail("Unequipped Curios ring retained its contribution");
                    return;
                }
                rings.setStackInSlot(1, ItemStack.EMPTY);
                player.getInventory().setItem(0, new ItemStack(TTItems.RING_MUNDANE.get()));
                if (Math.abs(VisDiscountAccess.additionalDiscount(player, TTAspects.AER, false, 1000) - 0.03F) > 0.00001F) {
                    helper.fail("Inventory ring contributed a worn discount");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
                rings.setStackInSlot(0, ItemStack.EMPTY);
                rings.setStackInSlot(1, ItemStack.EMPTY);
                ringSlots.removeModifier(extraSlots);
                ringSlots.update();
            }
        });
    }

    private static Consumer<VisDiscountEvent> discountListener(Player player, float fraction) {
        return event -> {
            if (event.player() == player && event.aspect().equals(TTAspects.AER) && (event.stack().is(Items.LEATHER_HELMET) || event.stack().is(TTItems.RING_MUNDANE.get()))) {
                event.addDiscount(fraction);
            }
        };
    }

    private static ItemStack chargedWand(boolean sceptre) {
        ItemStack wand = new ItemStack(TTItems.WAND.get());
        wand.set(TTDataComponents.WAND_PARTS.get(), new WandParts(TTWandParts.CAP_GOLD.get(), TTWandParts.ROD_WOOD.get(), sceptre));
        WandVisHelper.setStored(wand, TTAspects.AER, 2000);
        WandVisHelper.setStored(wand, TTAspects.IGNIS, 2000);
        return wand;
    }
}
