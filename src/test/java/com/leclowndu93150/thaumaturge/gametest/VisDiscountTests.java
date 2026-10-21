package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.items.VisDiscountAccess;
import com.leclowndu93150.thaumaturge.api.items.VisDiscountEvent;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import java.util.function.Consumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;

public final class VisDiscountTests {
    private VisDiscountTests() {}

    public static void register(TTTestRegistrar registrar) {
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
}
