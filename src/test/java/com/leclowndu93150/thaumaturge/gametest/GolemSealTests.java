package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.aspect.AspectIndexHolder;
import com.leclowndu93150.thaumaturge.content.golem.ItemSealPlacer;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
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
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
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

    public static void register(TCTestRegistrar r) {
        r.add("aspects/equipment_bonuses", 20, helper -> {
            requireAspect(helper, Items.IRON_SWORD, TCAspects.AVERSIO, 12);
            requireAspect(helper, Items.DIAMOND_SWORD, TCAspects.AVERSIO, 16);
            requireAspect(helper, Items.IRON_PICKAXE, TCAspects.INSTRUMENTUM, 12);
            requireAspect(helper, Items.STONE_SHOVEL, TCAspects.INSTRUMENTUM, 8);
            requireAspect(helper, Items.IRON_CHESTPLATE, TCAspects.PRAEMUNIO, 24);
            requireAspect(helper, Items.BOW, TCAspects.AVERSIO, 10);
            requireAspect(helper, Items.BOW, TCAspects.VOLATUS, 5);
            helper.succeed();
        });

        r.add("golem/seal_placement_on_chest", 40, helper -> {
            BlockPos chestPos = new BlockPos(2, 1, 2);
            helper.setBlock(chestPos, Blocks.CHEST);
            ServerPlayer player = testPlayer(helper, "tc_seal_chest_test");
            ItemStack stack = new ItemStack(TCItems.SEAL_STOCK.get());
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
            if (SealHandler.getSealEntity(helper.getLevel(), new SealPos(absolute, Direction.UP)) == null) {
                helper.fail("No seal entity registered on the chest after placement");
                return;
            }
            helper.succeed();
        });

        r.add("golem/seal_rejected_on_air", 40, helper -> {
            BlockPos airPos = new BlockPos(2, 2, 2);
            ServerPlayer player = testPlayer(helper, "tc_seal_air_test");
            ItemStack stack = new ItemStack(TCItems.SEAL_BREAKER.get());
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
}
