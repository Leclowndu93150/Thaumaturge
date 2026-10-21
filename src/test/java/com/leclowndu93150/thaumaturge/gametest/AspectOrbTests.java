package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.wands.AspectOrbAccess;
import com.leclowndu93150.thaumaturge.api.wands.AspectOrbPickupEvent;
import com.leclowndu93150.thaumaturge.content.wands.EntityAspectOrb;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class AspectOrbTests {
    private AspectOrbTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("wand/orb_hotbar_search", 20, helper -> {
            ServerPlayer player = player(helper, "orb_hotbar");
            EntityAspectOrb orb = orb(helper, player);
            ItemStack first = player.getInventory().getItem(0);
            ItemStack second = first.copy();
            player.getInventory().setItem(1, second);
            int before = WandVisHelper.capacityOf(first) - 50;
            WandVisHelper.setStored(first, TTAspects.AER, before);
            if (AspectOrbAccess.collect(orb, player) != 100 || !orb.isRemoved() || WandVisHelper.storedIn(first, TTAspects.AER) != before || WandVisHelper.storedIn(second, TTAspects.AER) != 100) {
                helper.fail("Pickup did not skip an insufficient recipient in hotbar order");
                return;
            }
            player.takeXpDelay = 0;
            if (AspectOrbAccess.collect(orb, player) != 0 || WandVisHelper.storedIn(second, TTAspects.AER) != 100) {
                helper.fail("A repeated pickup credited the second recipient again");
                return;
            }
            helper.succeed();
        });
        registrar.add("wand/orb_unknown_aspect", 20, helper -> {
            ServerPlayer player = player(helper, "orb_unknown");
            EntityAspectOrb orb = orb(helper, player);
            orb.setAspect(ResourceKey.create(IAspect.REGISTRY_KEY, Identifier.fromNamespaceAndPath("thaumaturge_test", "missing_orb_aspect")));
            if (AspectOrbAccess.inspect(orb, player).isPresent() || AspectOrbAccess.collect(orb, player) != 0 || orb.isRemoved()
                    || WandVisHelper.storedIn(player.getInventory().getItem(0), TTAspects.AER) != 0) {
                helper.fail("An unknown aspect was inspectable or collectible");
                return;
            }
            orb.discard();
            helper.succeed();
        });
        registrar.add("wand/orb_pickup_once", 20, helper -> {
            ServerPlayer player = player(helper, "orb_pickup");
            EntityAspectOrb orb = orb(helper, player);
            ItemStack wand = player.getInventory().getItem(0);
            if (AspectOrbAccess.inspect(orb, player).orElseThrow().vis() != 1 || AspectOrbAccess.collect(orb, player) != 100 || !orb.isRemoved()
                    || WandVisHelper.storedIn(wand, TTAspects.AER) != 100) {
                helper.fail("A normal pickup did not credit the source exactly once");
                return;
            }
            player.takeXpDelay = 0;
            if (AspectOrbAccess.collect(orb, player) != 0 || WandVisHelper.storedIn(wand, TTAspects.AER) != 100) {
                helper.fail("A removed orb credited its recipient again");
                return;
            }
            helper.succeed();
        });
        registrar.add("wand/orb_insufficient_room", 20, helper -> {
            ServerPlayer player = player(helper, "orb_room");
            EntityAspectOrb orb = orb(helper, player);
            ItemStack wand = player.getInventory().getItem(0);
            int before = WandVisHelper.capacityOf(wand) - 50;
            WandVisHelper.setStored(wand, TTAspects.AER, before);
            if (AspectOrbAccess.collect(orb, player) != 0 || orb.isRemoved() || WandVisHelper.storedIn(wand, TTAspects.AER) != before) {
                helper.fail("Partial capacity consumed source vis or changed the recipient");
                return;
            }
            orb.discard();
            helper.succeed();
        });
        registrar.add("wand/orb_pickup_modifier", 20, helper -> {
            ServerPlayer player = player(helper, "orb_bonus");
            EntityAspectOrb orb = orb(helper, player);
            Consumer<AspectOrbPickupEvent> listener = event -> {
                if (event.getPlayer() == player) {
                    event.setCreditCentivis(175);
                    if (AspectOrbAccess.collect(orb, player) != 0) {
                        helper.fail("Reentrant pickup credited an orb twice");
                    }
                }
            };
            NeoForge.EVENT_BUS.addListener(listener);
            try {
                if (AspectOrbAccess.collect(orb, player) != 175 || WandVisHelper.storedIn(player.getInventory().getItem(0), TTAspects.AER) != 175) {
                    helper.fail("The pickup modifier was not applied in centivis");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
                orb.discard();
            }
        });
        registrar.add("wand/orb_pickup_revalidates", 20, helper -> {
            ServerPlayer player = player(helper, "orb_stale");
            EntityAspectOrb orb = orb(helper, player);
            ItemStack wand = player.getInventory().getItem(0);
            player.takeXpDelay = 2;
            if (AspectOrbAccess.collect(orb, player) != 0 || orb.isRemoved()) {
                helper.fail("Pickup ignored the player cooldown");
                return;
            }
            player.takeXpDelay = 0;
            Consumer<AspectOrbPickupEvent> listener = event -> {
                if (event.getPlayer() == player) {
                    orb.discard();
                }
            };
            NeoForge.EVENT_BUS.addListener(listener);
            try {
                if (AspectOrbAccess.collect(orb, player) != 0 || WandVisHelper.storedIn(wand, TTAspects.AER) != 0) {
                    helper.fail("An orb removed during the pickup hook credited vis");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
                orb.discard();
            }
        });
        registrar.add("wand/orb_pickup_refusals", 20, helper -> {
            ServerPlayer player = player(helper, "orb_refusal");
            EntityAspectOrb orb = orb(helper, player);
            ItemStack wand = player.getInventory().getItem(0);
            WandVisHelper.fill(wand);
            if (AspectOrbAccess.collect(orb, player) != 0 || orb.isRemoved()) {
                helper.fail("A full recipient consumed an orb");
                return;
            }
            WandVisHelper.setStored(wand, TTAspects.AER, 0);
            orb.setAspect(TTAspects.PRAECANTATIO);
            if (AspectOrbAccess.collect(orb, player) != 0 || orb.isRemoved()) {
                helper.fail("A non-primal orb was collected");
                return;
            }
            orb.setAspect(TTAspects.AER);
            Consumer<AspectOrbPickupEvent> listener = event -> {
                if (event.getPlayer() == player) {
                    event.setCreditCentivis(0);
                }
            };
            NeoForge.EVENT_BUS.addListener(listener);
            try {
                if (AspectOrbAccess.collect(orb, player) != 0 || orb.isRemoved() || WandVisHelper.storedIn(wand, TTAspects.AER) != 0) {
                    helper.fail("Zero credit consumed an orb or changed its recipient");
                    return;
                }
                helper.succeed();
            } finally {
                NeoForge.EVENT_BUS.unregister(listener);
                orb.discard();
            }
        });
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name));
        player.getInventory().clearContent();
        ItemStack wand = new ItemStack(TTItems.WAND.get());
        wand.set(TTDataComponents.WAND_PARTS.get(), WandParts.starter());
        player.getInventory().setItem(0, wand);
        player.takeXpDelay = 0;
        BlockPos position = helper.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(position.getX(), position.getY(), position.getZ());
        return player;
    }

    private static EntityAspectOrb orb(GameTestHelper helper, ServerPlayer player) {
        EntityAspectOrb orb = new EntityAspectOrb(helper.getLevel(), player.getX(), player.getY(), player.getZ(), TTAspects.AER, 1);
        helper.getLevel().addFreshEntity(orb);
        return orb;
    }
}
