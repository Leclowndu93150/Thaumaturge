package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPlacementEvent;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumaturge.content.golem.ItemSealPlacer;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.NbtOps;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class GolemSealTests {
    private GolemSealTests() {}

    private static ServerPlayer testPlayer(GameTestHelper helper, String name) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 1, 2))));
        return player;
    }

    private static void requireAspect(GameTestHelper helper, Item item, ResourceKey<IAspect> key, int expected) {
        Holder<IAspect> aspect = Aspects.resolve(helper.getLevel().registryAccess(), key);
        if (aspect == null) {
            helper.fail("Aspect " + key.identifier() + " not resolvable");
            return;
        }
        AspectList list = AspectIndexHolder.getConcrete().of(item);
        int amount = list.amountOf(aspect);
        if (amount != expected) {
            helper.fail(item + " has " + amount + " " + key.identifier() + ", expected " + expected);
        }
    }

    public static void register(TTTestRegistrar r) {
        registerPlacement(r);
        r.add("aspects/equipment_bonuses", 20, helper -> {
            requireAspect(helper, Items.IRON_SWORD, TTAspects.AVERSIO, 12);
            requireAspect(helper, Items.DIAMOND_SWORD, TTAspects.AVERSIO, 16);
            requireAspect(helper, Items.IRON_PICKAXE, TTAspects.INSTRUMENTUM, 12);
            requireAspect(helper, Items.STONE_SHOVEL, TTAspects.INSTRUMENTUM, 8);
            requireAspect(helper, Items.IRON_CHESTPLATE, TTAspects.PRAEMUNIO, 24);
            requireAspect(helper, Items.BOW, TTAspects.AVERSIO, 10);
            requireAspect(helper, Items.BOW, TTAspects.VOLATUS, 5);
            helper.succeed();
        });

        r.add("golem/seal_placement_on_chest", 40, helper -> {
            BlockPos chestPos = new BlockPos(2, 1, 2);
            helper.setBlock(chestPos, Blocks.CHEST);
            ServerPlayer player = testPlayer(helper, "tc_seal_chest_test");
            ItemStack stack = new ItemStack(TTItems.SEAL_STOCK.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockPos absolute = helper.absolutePos(chestPos);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute).add(0.0, 0.5, 0.0), Direction.UP, absolute, false);
            UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
            ItemSealPlacer placer = (ItemSealPlacer) stack.getItem();
            var result = placer.onItemUseFirst(stack, context);
            if (!result.consumesAction()) {
                helper.fail("Seal placement on a chest was rejected: " + result);
                return;
            }
            if (SealHandler.lookup(helper.getLevel(), new SealPos(absolute, Direction.UP)) == null) {
                helper.fail("No seal entity registered on the chest after placement");
                return;
            }
            helper.succeed();
        });

        r.add("golem/seal_rejected_on_air", 40, helper -> {
            BlockPos airPos = new BlockPos(2, 2, 2);
            ServerPlayer player = testPlayer(helper, "tc_seal_air_test");
            ItemStack stack = new ItemStack(TTItems.SEAL_BREAKER.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockPos absolute = helper.absolutePos(airPos);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
            UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
            ItemSealPlacer placer = (ItemSealPlacer) stack.getItem();
            var result = placer.onItemUseFirst(stack, context);
            if (result.consumesAction()) {
                helper.fail("Breaker seal placement on air should fail");
                return;
            }
            helper.succeed();
        });
    }

    private static void registerPlacement(TTTestRegistrar registrar) {
        Identifier type = Identifier.fromNamespaceAndPath("thaumaturge", "guard");
        registrar.add("golem/seal_api_placement_and_duplicate", 20, helper -> {
            UseOnContext context = placementContext(helper, "seal_api_owner");
            ItemStack stack = context.getItemInHand();
            if (!GolemHelper.tryPlaceSeal(context, type) || stack.getCount() != 1) {
                helper.fail("Seal placement did not consume exactly one real hand item");
            }
            var seal = GolemHelper.getSealEntity(helper.getLevel(), new SealPos(context.getClickedPos(), context.getClickedFace()));
            if (seal == null || !context.getPlayer().getUUID().equals(seal.owner())) {
                helper.fail("Placed seal did not retain its owner");
            }
            seal.setLocked(true);
            seal.setPriority((byte) 3);
            var operations = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var encoded = SealEntity.CODEC.encodeStart(operations, (SealEntity) seal).getOrThrow();
            var restored = SealEntity.CODEC.parse(operations, encoded).getOrThrow();
            if (!context.getPlayer().getUUID().equals(restored.owner()) || !restored.isLocked() || restored.priority() != 3) {
                helper.fail("Placed seal ownership or configuration did not survive serialization");
            }
            if (GolemHelper.tryPlaceSeal(context, type) || stack.getCount() != 1) {
                helper.fail("Duplicate placement changed payment");
            }
            helper.succeed();
        });
        registrar.add("golem/seal_api_invalid_slot_and_type", 20, helper -> {
            UseOnContext context = placementContext(helper, "seal_api_slot");
            ItemStack original = context.getItemInHand();
            if (GolemHelper.tryPlaceSeal(context, Identifier.fromNamespaceAndPath("missing", "seal"))) {
                helper.fail("An unknown seal type was placed");
            }
            context.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, original.copy());
            if (GolemHelper.tryPlaceSeal(context, type) || original.getCount() != 2 || context.getPlayer().getMainHandItem().getCount() != 2) {
                helper.fail("A stale context consumed an unrelated hand stack");
            }
            helper.succeed();
        });
        registrar.add("golem/seal_api_protection_veto", 20, helper -> {
            UseOnContext context = placementContext(helper, "seal_api_veto");
            Consumer<SealPlacementEvent> veto = event -> {
                if (event.player() == context.getPlayer()) {
                    event.setCanceled(true);
                }
            };
            NeoForge.EVENT_BUS.addListener(veto);
            try {
                if (GolemHelper.tryPlaceSeal(context, type) || context.getItemInHand().getCount() != 2
                        || GolemHelper.getSealEntity(helper.getLevel(), new SealPos(context.getClickedPos(), context.getClickedFace())) != null) {
                    helper.fail("A protection veto changed the world or payment");
                }
            } finally {
                NeoForge.EVENT_BUS.unregister(veto);
            }
            helper.succeed();
        });
        registrar.add("golem/seal_api_listener_mutation", 20, helper -> {
            UseOnContext context = placementContext(helper, "seal_api_mutation");
            ItemStack replacement = context.getItemInHand().copy();
            Consumer<SealPlacementEvent> mutate = event -> {
                if (event.player() == context.getPlayer()) {
                    event.player().setItemInHand(InteractionHand.MAIN_HAND, replacement);
                }
            };
            NeoForge.EVENT_BUS.addListener(mutate);
            try {
                if (GolemHelper.tryPlaceSeal(context, type) || replacement.getCount() != 2 || context.getItemInHand().getCount() != 2
                        || GolemHelper.getSealEntity(helper.getLevel(), new SealPos(context.getClickedPos(), context.getClickedFace())) != null) {
                    helper.fail("Placement ignored a listener's hand-slot replacement");
                }
            } finally {
                NeoForge.EVENT_BUS.unregister(mutate);
            }
            helper.succeed();
        });
        registrar.add("golem/seal_api_range_and_thread", 20, helper -> {
            UseOnContext context = placementContext(helper, "seal_api_range");
            context.getPlayer().setPos(context.getPlayer().position().add(100, 0, 0));
            if (GolemHelper.tryPlaceSeal(context, type) || context.getItemInHand().getCount() != 2) {
                helper.fail("Remote seal placement succeeded");
            }
            boolean rejected = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try {
                    GolemHelper.tryPlaceSeal(context, type);
                    return false;
                } catch (IllegalStateException expected) {
                    return true;
                }
            }).join();
            if (!rejected) {
                helper.fail("Off-thread seal placement was accepted");
            }
            helper.succeed();
        });
    }

    private static UseOnContext placementContext(GameTestHelper helper, String name) {
        BlockPos relative = new BlockPos(2, 1, 2);
        helper.setBlock(relative, Blocks.STONE);
        ServerPlayer player = testPlayer(helper, name);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TTItems.SEAL_GUARD.get(), 2));
        BlockPos absolute = helper.absolutePos(relative);
        return new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
    }
}
