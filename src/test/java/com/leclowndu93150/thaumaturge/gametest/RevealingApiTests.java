package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.items.RevealingEvent;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;

public final class RevealingApiTests {
    private RevealingApiTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("api/revealing/live_property", 20, helper -> {
            Consumer<RevealingEvent> listener = event -> {
                ItemStack queried = event.stack();
                if (queried.is(Items.LEATHER_HELMET) && queried.has(DataComponents.CUSTOM_NAME)) {
                    event.grant();
                }
                queried.setCount(0);
            };
            NeoForge.EVENT_BUS.addListener(RevealingEvent.class, listener);
            try {
                ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
                helper.assertTrue(!GogglesAccess.isRevealing(helmet), "Plain gear must not reveal");
                helmet.set(DataComponents.CUSTOM_NAME, Component.literal("Revealing fixture"));
                helper.assertTrue(GogglesAccess.isRevealing(helmet), "Derived property must reveal");
                helper.assertTrue(helmet.getCount() == 1, "Listeners must receive a defensive copy");
                var player = helper.makeMockPlayer(GameType.SURVIVAL);
                player.setItemSlot(EquipmentSlot.MAINHAND, helmet);
                helper.assertTrue(!GogglesAccess.wearsRevealingGear(player), "Held gear must not reveal");
                player.setItemSlot(EquipmentSlot.HEAD, helmet);
                helper.assertTrue(GogglesAccess.wearsRevealingGear(player), "Worn derived gear must reveal");
                helmet.remove(DataComponents.CUSTOM_NAME);
                helper.assertTrue(!GogglesAccess.isRevealing(helmet), "Removing the property must remove derived revealing");
                helmet.set(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE);
                helper.assertTrue(GogglesAccess.isRevealing(helmet), "Permanent upgrades must remain revealing");
                helper.assertTrue(!GogglesAccess.isRevealing(ItemStack.EMPTY), "Empty stacks must not reveal");
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
            }
            helper.succeed();
        });
    }
}
