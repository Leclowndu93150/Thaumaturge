package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.warp.WarpStrengthEvent;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;

public final class WarpStrengthApiTests {
    private WarpStrengthApiTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("warp_api/additive_bounds", 20, helper -> {
            ServerPlayer player = TTTestPlayers.create(helper);
            try {
                WarpStrengthEvent event = new WarpStrengthEvent(player, 10);
                event.reduce(2);
                event.reduce(5);
                if (event.originalStrength() != 10 || event.strength() != 3) {
                    helper.fail("Warp reductions did not compose additively");
                    return;
                }
                event.reduce(Integer.MAX_VALUE);
                event.reduce(Integer.MAX_VALUE);
                if (event.strength() != 0 || new WarpStrengthEvent(player, Integer.MIN_VALUE).strength() != 0) {
                    helper.fail("Warp strength escaped its nonnegative bounds");
                    return;
                }
                try {
                    event.reduce(-1);
                    helper.fail("Negative warp reduction was accepted");
                    return;
                } catch (IllegalArgumentException expected) {
                }
                helper.succeed();
            } finally {
                helper.getLevel().getServer().getPlayerList().remove(player);
            }
        });

        registrar.add("warp_api/current_equipment", 20, helper -> {
            ServerPlayer player = TTTestPlayers.create(helper);
            AtomicInteger callbacks = new AtomicInteger();
            RandomSource random = RandomSource.create(42);
            Consumer<WarpStrengthEvent> mask = event -> {
                if (event.player() == player) {
                    callbacks.incrementAndGet();
                    if (event.player().getItemBySlot(EquipmentSlot.HEAD).is(Items.LEATHER_HELMET)) {
                        event.reduce(random.nextIntBetweenInclusive(2, 5));
                    }
                }
            };
            Consumer<WarpStrengthEvent> additional = event -> {
                if (event.player() == player) {
                    event.reduce(1);
                }
            };
            NeoForge.EVENT_BUS.addListener(WarpStrengthEvent.class, mask);
            NeoForge.EVENT_BUS.addListener(WarpStrengthEvent.class, additional);
            try {
                player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
                boolean minimum = false;
                boolean maximum = false;
                for (int iteration = 0; iteration < 64; iteration++) {
                    WarpStrengthEvent event = NeoForge.EVENT_BUS.post(new WarpStrengthEvent(player, 20));
                    int reduction = 19 - event.strength();
                    minimum |= reduction == 2;
                    maximum |= reduction == 5;
                    if (reduction < 2 || reduction > 5) {
                        helper.fail("Equipment callback did not apply exactly one bounded reduction");
                        return;
                    }
                }
                player.getInventory().setItem(0, player.getItemBySlot(EquipmentSlot.HEAD));
                player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                WarpStrengthEvent unequipped = NeoForge.EVENT_BUS.post(new WarpStrengthEvent(player, 20));
                if (!minimum || !maximum || callbacks.get() != 65 || unequipped.strength() != 19) {
                    helper.fail("Removed equipment remained active or listener invocation was duplicated");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(mask);
                NeoForge.EVENT_BUS.unregister(additional);
                helper.getLevel().getServer().getPlayerList().remove(player);
            }
        });
    }
}
