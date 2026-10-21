package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpStrengthEvent;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.warp.WarpGear;
import com.leclowndu93150.thaumaturge.content.warp.roll.WarpCheck;
import com.leclowndu93150.thaumaturge.content.warp.roll.WarpReading;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public final class WarpStrengthTests {
    private WarpStrengthTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("warp_api/native_roll_and_pools", 20, helper -> {
            ServerPlayer player = TTTestPlayers.create(helper);
            AtomicInteger original = new AtomicInteger();
            AtomicInteger callbacks = new AtomicInteger();
            Consumer<WarpStrengthEvent> suppress = event -> {
                if (event.player() == player) {
                    original.set(event.originalStrength());
                    callbacks.incrementAndGet();
                    event.reduce(Integer.MAX_VALUE);
                }
            };
            NeoForge.EVENT_BUS.addListener(WarpStrengthEvent.class, suppress);
            try {
                var warp = WarpHelper.getWarp(player);
                for (boolean fortress : new boolean[]{false, true}) {
                    ItemStack head = fortress ? new ItemStack(TTItems.FORTRESS_HELM.get()) : ItemStack.EMPTY;
                    if (fortress) {
                        head.set(TTDataComponents.FORTRESS_MASK.get(), 0);
                    }
                    player.setItemSlot(EquipmentSlot.HEAD, head);
                    int gear = WarpGear.worn(player);
                    for (int seed = 0; seed < 32; seed++) {
                        warp.clear();
                        warp.set(WarpType.NORMAL, 100);
                        warp.set(WarpType.TEMPORARY, 2);
                        warp.setCounter(10000);
                        WarpReading reading = new WarpReading(1, 100, 0, gear, 10000);
                        RandomSource expected = RandomSource.create(seed);
                        expected.nextDouble();
                        int penalty = fortress ? expected.nextIntBetweenInclusive(2, 5) : 0;
                        int expectedStrength = expected.nextInt(reading.effective()) + gear - penalty;
                        player.getRandom().setSeed(seed);
                        WarpCheck.run(player);
                        if (original.get() != expectedStrength || warp.get(WarpType.NORMAL) != 100 || warp.get(WarpType.PERMANENT) != 0 || warp.get(WarpType.TEMPORARY) != 1
                                || warp.getCounter() != reading.counterAfterEvent() || !player.getActiveEffects().isEmpty()) {
                            helper.fail("Warp modifier changed native roll, fortress penalty, pools, counter, or applied a suppressed effect");
                            return;
                        }
                    }
                }
                if (callbacks.get() != 64) {
                    helper.fail("Warp modifier was not called exactly once per successful check");
                    return;
                }
                warp.clear();
                WarpCheck.run(player);
                if (callbacks.get() != 64) {
                    helper.fail("Warp modifier ran without a successful roll");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(suppress);
                helper.getLevel().getServer().getPlayerList().remove(player);
            }
        });
    }
}
