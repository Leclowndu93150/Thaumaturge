package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.api.infusion.InfusionAutomationPolicy;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.Failure;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.State;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class InfusionAutomationTests {
    private InfusionAutomationTests() {}

    public static void register(TTTestRegistrar registrar) {
        Identifier surcharge = Identifier.fromNamespaceAndPath("thaumaturge_test", "infusion_surcharge");
        Identifier invalidSurcharge = Identifier.fromNamespaceAndPath("thaumaturge_test", "invalid_infusion_surcharge");
        InfusionAutomationPolicy.register(surcharge, (context, player) -> 23);
        InfusionAutomationPolicy.register(invalidSurcharge, (context, player) -> Integer.MAX_VALUE);

        registrar.add("api/infusion_activity", 20, helper -> {
            ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                    new GameProfile(UUID.nameUUIDFromBytes("infusion_activity".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "infusion_activity"));
            BlockPos matrix = helper.absolutePos(new BlockPos(2, 3, 2));
            InfusionMatrixContext context = InfusionMatrixContext.of(player, matrix);
            helper.assertTrue(InfusionCraftingTransaction.status(context, player).state() == State.UNAVAILABLE, "Missing matrix must be unavailable");
            helper.setBlock(new BlockPos(2, 3, 2), BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("thaumaturge", "infusion_matrix")));
            helper.setBlock(new BlockPos(2, 1, 2), BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("thaumaturge", "pedestal_arcane")));
            for (int offsetX : new int[]{-1, 1}) {
                for (int offsetZ : new int[]{-1, 1}) {
                    helper.setBlock(new BlockPos(2 + offsetX, 1, 2 + offsetZ), BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("thaumaturge", "pillar_arcane")));
                }
            }
            helper.assertTrue(InfusionCraftingTransaction.status(context, player).state() == State.INACTIVE, "New matrix must be inactive");
            IInteractWithCaster activation = (IInteractWithCaster) helper.getLevel().getBlockEntity(matrix);
            activation.onCasterRightClick(helper.getLevel(), ItemStack.EMPTY, player, matrix, Direction.UP, InteractionHand.MAIN_HAND);
            var before = InfusionCraftingTransaction.status(context, player);
            helper.assertTrue(before.state() == State.IDLE && !before.isCrafting(), "Activated empty matrix must be idle");
            helper.assertTrue(InfusionCraftingTransaction.inspectStaged(context, player).inspection().failure() == Failure.NO_RECIPE, "Empty pedestals must not match");
            helper.assertTrue(InfusionCraftingTransaction.status(context, player).equals(before), "Inspection must not change activity or revision");
            InfusionMatrixContext foreign = new InfusionMatrixContext(helper.getLevel(), matrix, UUID.randomUUID());
            helper.assertTrue(InfusionCraftingTransaction.status(foreign, player).failure() == Failure.INVALID_CONTEXT, "A mismatched actor must be rejected");
            helper.succeed();
        });

        registrar.add("api/infusion_policy_bounds", 20, helper -> {
            ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                    new GameProfile(UUID.nameUUIDFromBytes("infusion_policy".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "infusion_policy"));
            InfusionMatrixContext context = InfusionMatrixContext.of(player, helper.absolutePos(BlockPos.ZERO));
            helper.assertTrue(InfusionAutomationPolicy.resolve(surcharge, context, player) == 23, "Trusted policy surcharge must be preserved");
            helper.assertTrue(InfusionAutomationPolicy.resolve(invalidSurcharge, context, player) == -1, "Overflowing surcharge must be denied");
            helper.assertTrue(InfusionAutomationPolicy.resolve(Identifier.fromNamespaceAndPath("thaumaturge_test", "unknown_policy"), context, player) == -1, "Unknown policies must be denied");
            helper.succeed();
        });
    }
}
