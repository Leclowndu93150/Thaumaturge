package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.equipment.runic.RunicShieldState;
import com.leclowndu93150.thaumaturge.content.equipment.runic.RunicShielding;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionInput;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class RunicShieldingTests {
    private RunicShieldingTests() {}

    private static ServerPlayer testPlayer(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
    }

    private static InfusionRunicAugmentRecipe recipe(GameTestHelper helper) {
        return helper.getLevel().getServer().getRecipeManager().getRecipes().stream().filter(holder -> holder.value().getType() == TCRecipeTypes.RUNIC_AUGMENT.get()).findFirst()
                .map(holder -> (InfusionRunicAugmentRecipe) holder.value()).orElse(null);
    }

    public static void register(TCTestRegistrar r) {
        r.add("runic/augment_recipe_scaling", 20, helper -> {
            InfusionRunicAugmentRecipe recipe = recipe(helper);
            if (recipe == null) {
                helper.fail("No runic augment recipe loaded");
                return;
            }
            ItemStack fresh = new ItemStack(Items.IRON_CHESTPLATE);
            InfusionInput freshInput = new InfusionInput(fresh, List.of(new ItemStack(TCItems.SALIS_MUNDUS.get()), new ItemStack(TCItems.AMBER.get())));
            if (!recipe.matches(freshInput, helper.getLevel())) {
                helper.fail("Fresh chestplate + salis + amber did not match");
                return;
            }
            AspectList cost = recipe.scaledAspects(fresh);
            if (cost.entries().size() != 3 || cost.entries().get(0).amount() != 40) {
                helper.fail("Charge-0 aspect cost wrong: " + cost);
                return;
            }
            ItemStack once = recipe.augmentedResult(fresh);
            if (InfusionRunicAugmentRecipe.charge(once) != 1) {
                helper.fail("Augment did not set charge 1");
                return;
            }
            if (recipe.matches(new InfusionInput(once, freshInput.components()), helper.getLevel())) {
                helper.fail("Charged item matched without the extra amber");
                return;
            }
            InfusionInput scaledInput = new InfusionInput(once, List.of(new ItemStack(TCItems.SALIS_MUNDUS.get()), new ItemStack(TCItems.AMBER.get()), new ItemStack(TCItems.AMBER.get())));
            if (!recipe.matches(scaledInput, helper.getLevel())) {
                helper.fail("Charged item did not match with the extra amber");
                return;
            }
            AspectList scaledCost = recipe.scaledAspects(once);
            if (scaledCost.entries().get(0).amount() != 60) {
                helper.fail("Charge-1 praemunio cost expected 60, got " + scaledCost.entries().get(0).amount());
                return;
            }
            if (recipe.scaledInstability(once) != 5) {
                helper.fail("Charge-1 instability expected 5, got " + recipe.scaledInstability(once));
                return;
            }
            if (recipe.matches(new InfusionInput(new ItemStack(Items.STICK), freshInput.components()), helper.getLevel())) {
                helper.fail("A stick should not be runic-shieldable");
                return;
            }
            helper.succeed();
        });

        r.add("runic/shield_recharges_from_aura", 40, helper -> {
            ServerPlayer player = testPlayer(helper, "tc_runic_test");
            player.snapTo(helper.absolutePos(new BlockPos(2, 1, 2)).getX() + 0.5, helper.absolutePos(new BlockPos(2, 1, 2)).getY(), helper.absolutePos(new BlockPos(2, 1, 2)).getZ() + 0.5, 0.0F, 0.0F);
            player.setAbsorptionAmount(0.0F);
            ItemStack armor = new ItemStack(Items.IRON_CHESTPLATE);
            armor.set(TCDataComponents.RUNIC_CHARGE.get(), 3);
            player.setItemSlot(EquipmentSlot.CHEST, armor);
            AuraHelper.addVis(helper.getLevel(), player.blockPosition(), 20.0F);
            float visBefore = AuraHelper.getVis(helper.getLevel(), player.blockPosition());
            RunicShieldState state = player.getData(TCAttachments.RUNIC_SHIELD.get());
            state.maxCharge = 0;
            state.lastCharge = 0;
            state.nextCycle = 0;
            RunicShielding.tick(player);
            var attribute = player.getAttribute(Attributes.MAX_ABSORPTION);
            if (attribute == null || attribute.getModifier(RunicShielding.MODIFIER_ID) == null) {
                helper.fail("Max absorption modifier not applied");
                return;
            }
            if (player.getAbsorptionAmount() < 1.0F) {
                helper.fail("Shield did not recharge a point, absorption " + player.getAbsorptionAmount());
                return;
            }
            float visAfter = AuraHelper.getVis(helper.getLevel(), player.blockPosition());
            if (visAfter >= visBefore) {
                helper.fail("Recharge did not drain vis: before " + visBefore + " after " + visAfter);
                return;
            }
            state.nextCycle = 0;
            RunicShielding.tick(player);
            state.nextCycle = 0;
            RunicShielding.tick(player);
            if ((int) player.getAbsorptionAmount() != 3) {
                helper.fail("Shield should cap at charge 3, absorption " + player.getAbsorptionAmount());
                return;
            }
            state.nextCycle = 0;
            RunicShielding.tick(player);
            if ((int) player.getAbsorptionAmount() != 3) {
                helper.fail("Shield exceeded its charge cap");
                return;
            }
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            RunicShielding.tick(player);
            if (player.getAbsorptionAmount() != 0.0F) {
                helper.fail("Removing the armor should strip the granted absorption, has " + player.getAbsorptionAmount());
                return;
            }
            helper.succeed();
        });
    }
}
