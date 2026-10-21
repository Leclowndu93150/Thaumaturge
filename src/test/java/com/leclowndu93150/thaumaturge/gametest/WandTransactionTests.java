package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.WandAccess;
import com.leclowndu93150.thaumaturge.api.wands.WandChargeQuote;
import com.leclowndu93150.thaumaturge.api.wands.WandPayment;
import com.leclowndu93150.thaumaturge.api.wands.WandSlot;
import com.leclowndu93150.thaumaturge.api.wands.WandSnapshot;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import com.mojang.authlib.GameProfile;
import java.util.Map;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class WandTransactionTests {
    private WandTransactionTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("wand/inspection_rejects_invalid_parts", 20, helper -> {
            ItemStack missing = wand(false);
            missing.remove(TTDataComponents.WAND_PARTS.get());
            ItemStack invalid = wand(false);
            invalid.set(TTDataComponents.WAND_PARTS.get(), new WandParts(TTWandParts.CAP_GOLD.get(), TTWandParts.STAFF_GREATWOOD.get(), true));
            if (WandAccess.inspect(new ItemStack(Items.STICK)).isPresent() || WandAccess.inspect(missing).isPresent() || WandAccess.inspect(invalid).isPresent()) {
                helper.fail("Inspection accepted a non-wand, missing parts or a sceptre staff");
                return;
            }
            helper.succeed();
        });

        registrar.add("wand/inspection_and_effective_capacity", 20, helper -> {
            ItemStack ordinary = wand(false);
            ItemStack sceptre = wand(true);
            WandSnapshot snapshot = WandAccess.inspect(sceptre).orElseThrow();
            if (snapshot.variant() != WandSnapshot.Variant.SCEPTRE || !snapshot.rod().equals(TTWandParts.ROD_WOOD.getKey()) || !snapshot.cap().equals(TTWandParts.CAP_GOLD.getKey())
                    || WandAccess.capacityCentivis(ordinary, TTAspects.IGNIS).orElseThrow() != 2500 || WandAccess.capacityCentivis(sceptre, TTAspects.IGNIS).orElseThrow() != 3750) {
                helper.fail("Inspection or effective sceptre capacity did not match the registered parts");
                return;
            }
            helper.succeed();
        });

        registrar.add("wand/partial_charge_commit", 20, helper -> {
            ServerPlayer player = player(helper, "tc_wand_charge");
            TestSlot slot = new TestSlot(player, WandAccess.withVis(wand(false), TTAspects.IGNIS, 2475));
            WandChargeQuote quote = WandAccess.quoteCharge(player, slot, TTAspects.IGNIS, 100).orElseThrow();
            if (quote.acceptedCentivis() != 25 || WandAccess.getVis(slot.stack(), TTAspects.IGNIS) != 2475) {
                helper.fail("Charge preview did not bound insertion without mutation");
                return;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                if (WandAccess.tryCharge(player, slot, quote, transaction) != 25 || slot.changes != 0) {
                    helper.fail("Charge did not insert the quoted amount or notified before commit");
                    return;
                }
                transaction.commit();
            }
            if (WandAccess.getVis(slot.stack(), TTAspects.IGNIS) != 2500 || slot.changes != 1) {
                helper.fail("Root commit did not retain and notify the bounded charge");
                return;
            }
            helper.succeed();
        });

        registrar.add("wand/charge_nested_and_root_rollback", 20, helper -> {
            ServerPlayer player = player(helper, "tc_wand_rollback");
            TestSlot slot = new TestSlot(player, wand(false));
            try (Transaction root = Transaction.openRoot()) {
                try (Transaction nested = Transaction.open(root)) {
                    WandChargeQuote quote = WandAccess.quoteCharge(player, slot, TTAspects.IGNIS, 100).orElseThrow();
                    if (WandAccess.tryCharge(player, slot, quote, nested) != 100) {
                        helper.fail("Nested charge was refused");
                        return;
                    }
                }
                if (WandAccess.getVis(slot.stack(), TTAspects.IGNIS) != 0 || slot.changes != 0) {
                    helper.fail("Nested abort did not restore the charge silently");
                    return;
                }
                WandChargeQuote quote = WandAccess.quoteCharge(player, slot, TTAspects.IGNIS, 200).orElseThrow();
                if (WandAccess.tryCharge(player, slot, quote, root) != 200) {
                    helper.fail("Charge after nested rollback was refused");
                    return;
                }
            }
            if (WandAccess.getVis(slot.stack(), TTAspects.IGNIS) != 0 || slot.changes != 0) {
                helper.fail("Root abort did not restore the charge silently");
                return;
            }
            helper.succeed();
        });

        registrar.add("wand/quotes_reject_replaced_inventory_stack", 20, helper -> {
            ServerPlayer player = player(helper, "tc_wand_swap");
            ItemStack original = WandAccess.withVis(wand(false), TTAspects.IGNIS, 500);
            player.getInventory().setItem(0, original);
            WandSlot slot = WandSlot.playerInventory(player, 0);
            WandChargeQuote charge = WandAccess.quoteCharge(player, slot, TTAspects.IGNIS, 100).orElseThrow();
            WandPayment.Quote payment = WandPayment.quote(player, slot, Map.of(TTAspects.IGNIS, 100), WandPayment.CostKind.RAW).orElseThrow();
            ItemStack replacement = original.copy();
            player.getInventory().setItem(0, replacement);
            try (Transaction transaction = Transaction.openRoot()) {
                if (WandAccess.tryCharge(player, slot, charge, transaction) != 0 || WandPayment.tryPay(player, slot, payment, transaction)) {
                    helper.fail("A quote authorized an identical replacement stack");
                    return;
                }
                transaction.commit();
            }
            if (WandAccess.getVis(original, TTAspects.IGNIS) != 500 || WandAccess.getVis(replacement, TTAspects.IGNIS) != 500) {
                helper.fail("Rejected stale quotes changed a wand");
                return;
            }
            helper.succeed();
        });

        registrar.add("wand/raw_payment_is_atomic", 20, helper -> {
            ServerPlayer player = player(helper, "tc_wand_payment");
            ItemStack charged = WandAccess.withVis(WandAccess.withVis(wand(false), TTAspects.IGNIS, 500), TTAspects.AQUA, 50);
            TestSlot slot = new TestSlot(player, charged);
            var before = WandAccess.getAllVis(charged);
            if (WandPayment.quote(player, slot, Map.of(TTAspects.IGNIS, 100, TTAspects.AQUA, 51), WandPayment.CostKind.RAW).isPresent() || !WandAccess.getAllVis(charged).equals(before)) {
                helper.fail("An unaffordable multi-aspect payment was quoted or changed storage");
                return;
            }
            Map<ResourceKey<IAspect>, Integer> costs = Map.of(TTAspects.IGNIS, 100, TTAspects.AQUA, 50);
            WandPayment.Quote quote = WandPayment.quote(player, slot, costs, WandPayment.CostKind.RAW).orElseThrow();
            if (!quote.costs().equals(costs)) {
                helper.fail("Raw payment changed the requested costs");
                return;
            }
            try (Transaction transaction = Transaction.openRoot()) {
                if (!WandPayment.tryPay(player, slot, quote, transaction)) {
                    helper.fail("Affordable raw payment was refused");
                    return;
                }
            }
            if (!WandAccess.getAllVis(charged).equals(before) || slot.changes != 0) {
                helper.fail("Payment rollback did not restore every pool silently");
                return;
            }
            quote = WandPayment.quote(player, slot, costs, WandPayment.CostKind.RAW).orElseThrow();
            try (Transaction transaction = Transaction.openRoot()) {
                if (!WandPayment.tryPay(player, slot, quote, transaction)) {
                    helper.fail("Payment after rollback was refused");
                    return;
                }
                transaction.commit();
            }
            if (WandAccess.getVis(charged, TTAspects.IGNIS) != 400 || WandAccess.getVis(charged, TTAspects.AQUA) != 0 || slot.changes != 1) {
                helper.fail("Committed payment did not deduct every raw cost and notify the slot");
                return;
            }
            helper.succeed();
        });
    }

    private static ItemStack wand(boolean sceptre) {
        ItemStack wand = new ItemStack(TTItems.WAND.get());
        wand.set(TTDataComponents.WAND_PARTS.get(), new WandParts(TTWandParts.CAP_GOLD.get(), TTWandParts.ROD_WOOD.get(), sceptre));
        return wand;
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
    }

    private static final class TestSlot implements WandSlot {
        private final ServerPlayer owner;
        private final ItemStack stack;
        private int changes;

        private TestSlot(ServerPlayer owner, ItemStack stack) {
            this.owner = owner;
            this.stack = stack;
        }

        @Override
        public ItemStack stack() {
            return stack;
        }

        @Override
        public boolean mayAccess(ServerPlayer player) {
            return player == owner;
        }

        @Override
        public void changed() {
            changes++;
        }
    }
}
