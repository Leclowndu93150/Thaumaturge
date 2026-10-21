package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionDelegation;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class InfusionDelegationTests {
    private InfusionDelegationTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("infusion/delegation_grant_and_revoke", 40, helper -> {
            var player = TTTestPlayers.create(helper);
            var server = helper.getLevel().getServer();
            BlockPos matrixPosition = helper.absolutePos(new BlockPos(1, 3, 1));
            BlockPos devicePosition = helper.absolutePos(new BlockPos(2, 3, 1));
            helper.getLevel().setBlockAndUpdate(matrixPosition, TTBlocks.INFUSION_MATRIX.get().defaultBlockState());
            helper.getLevel().setBlockAndUpdate(matrixPosition.below(2), TTBlocks.PEDESTAL_ARCANE.get().defaultBlockState());
            for (int horizontal : new int[]{-1, 1}) {
                for (int depth : new int[]{-1, 1}) {
                    helper.getLevel().setBlockAndUpdate(matrixPosition.offset(horizontal, -2, depth), TTBlocks.PILLAR_ARCANE.get().defaultBlockState());
                }
            }
            helper.getLevel().setBlockAndUpdate(devicePosition, Blocks.CHEST.defaultBlockState());
            var device = new TestDevice(devicePosition, player.getUUID());
            helper.getLevel().setBlockEntity(device);
            KnowledgeAccess.of(player).addResearch(TTIds.rl("gametest/delegation_knowledge"));
            ResourceKey<Recipe<?>> recipe = ResourceKey.create(Registries.RECIPE, TTIds.rl("gametest/delegation"));
            try {
                InfusionDelegation.configure(server, true, true);
                var handle = InfusionDelegation.grant(player, devicePosition, matrixPosition, Set.of(recipe), 100).orElseThrow();
                var matrix = new InfusionMatrixContext(helper.getLevel(), matrixPosition, player.getUUID());
                var context = new InfusionDelegation.Context(matrix, devicePosition, handle);
                if (InfusionCraftingTransaction.inspectStaged(context).inspection().failure() != InfusionCraftingTransaction.Failure.NO_RECIPE) {
                    helper.fail("An explicitly granted altar did not reach recipe inspection");
                    return;
                }
                var copied = new InfusionDelegation.Context(matrix, devicePosition.above(), handle);
                if (InfusionCraftingTransaction.inspectStaged(copied).inspection().failure() != InfusionCraftingTransaction.Failure.DELEGATION_DENIED) {
                    helper.fail("A copied grant authorized another device position");
                    return;
                }
                if (!InfusionDelegation.revoke(player, handle) || InfusionCraftingTransaction.inspectStaged(context).inspection().failure() != InfusionCraftingTransaction.Failure.DELEGATION_DENIED) {
                    helper.fail("Revoked authority remained usable");
                    return;
                }
                handle = InfusionDelegation.grant(player, devicePosition, matrixPosition, Set.of(recipe), 100).orElseThrow();
                context = new InfusionDelegation.Context(matrix, devicePosition, handle);
                InfusionDelegation.configure(server, true, false);
                InfusionDelegation.configure(server, true, true);
                if (InfusionCraftingTransaction.inspectStaged(context).inspection().failure() != InfusionCraftingTransaction.Failure.DELEGATION_DENIED) {
                    helper.fail("A grant survived a policy revision");
                    return;
                }
                handle = InfusionDelegation.grant(player, devicePosition, matrixPosition, Set.of(recipe), 100).orElseThrow();
                context = new InfusionDelegation.Context(matrix, devicePosition, handle);
                device.owner = UUID.randomUUID();
                if (InfusionCraftingTransaction.inspectStaged(context).inspection().failure() != InfusionCraftingTransaction.Failure.DELEGATION_DENIED) {
                    helper.fail("A replacement owner retained the previous owner's authority");
                    return;
                }
                device.owner = player.getUUID();
                if (InfusionCraftingTransaction.inspectStaged(context).inspection().failure() != InfusionCraftingTransaction.Failure.DELEGATION_DENIED) {
                    helper.fail("Restoring an owner revived revoked authority");
                    return;
                }
                if (InfusionDelegation.grant(player, devicePosition, matrixPosition, Set.of(recipe), 0).isPresent()) {
                    helper.fail("An expired grant was issued");
                    return;
                }
                handle = InfusionDelegation.grant(player, devicePosition, matrixPosition, Set.of(recipe), 100).orElseThrow();
                context = new InfusionDelegation.Context(matrix, devicePosition, handle);
                server.getPlayerList().remove(player);
                if (InfusionCraftingTransaction.inspectStaged(context).inspection().failure() != InfusionCraftingTransaction.Failure.NO_RECIPE) {
                    helper.fail("Persisted offline knowledge did not permit recipe inspection");
                    return;
                }
                var playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getUUID() + ".dat");
                var hiddenFile = playerFile.resolveSibling(player.getUUID() + ".delegation-test");
                try {
                    Files.move(playerFile, hiddenFile);
                    try {
                        if (InfusionCraftingTransaction.inspectStaged(context).inspection().failure() != InfusionCraftingTransaction.Failure.DELEGATION_DENIED) {
                            helper.fail("Missing authoritative player data retained offline authority");
                            return;
                        }
                    } finally {
                        Files.move(hiddenFile, playerFile);
                    }
                } catch (IOException exception) {
                    helper.fail("Could not exercise missing mock-player data: " + exception.getMessage());
                    return;
                }
                helper.succeed();
            } finally {
                InfusionDelegation.configure(server, false, false);
                if (server.getPlayerList().getPlayer(player.getUUID()) != null) {
                    server.getPlayerList().remove(player);
                }
            }
        });
        registrar.add("infusion/delegation_rejects_forged_authority", 40, helper -> {
            BlockPos position = helper.absolutePos(BlockPos.ZERO);
            var matrix = new InfusionMatrixContext(helper.getLevel(), position, UUID.randomUUID());
            var context = new InfusionDelegation.Context(matrix, position.above(), new InfusionDelegation.Handle(UUID.randomUUID()));
            var inspection = InfusionCraftingTransaction.inspectStaged(context).inspection();
            if (inspection.failure() != InfusionCraftingTransaction.Failure.DELEGATION_DENIED) {
                helper.fail("A fabricated delegation did not fail closed");
                return;
            }
            helper.succeed();
        });
        registrar.add("infusion/delegation_requires_present_owner", 40, helper -> {
            var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "delegation_test"));
            BlockPos position = helper.absolutePos(BlockPos.ZERO);
            ResourceKey<Recipe<?>> recipe = ResourceKey.create(Registries.RECIPE, TTIds.rl("gametest/delegation"));
            if (InfusionDelegation.grant(player, position, position.above(), Set.of(recipe), 100).isPresent()) {
                helper.fail("A synthetic player issued delegation authority");
                return;
            }
            if (InfusionDelegation.revoke(player, new InfusionDelegation.Handle(UUID.randomUUID()))) {
                helper.fail("A synthetic player revoked authority");
                return;
            }
            helper.succeed();
        });
    }

    private static final class TestDevice extends ChestBlockEntity implements InfusionDelegation.Device {
        private UUID owner;
        private final UUID device = UUID.randomUUID();

        private TestDevice(BlockPos position, UUID owner) {
            super(position, Blocks.CHEST.defaultBlockState());
            this.owner = owner;
        }

        @Override
        public UUID infusionOwner() {
            return owner;
        }

        @Override
        public UUID infusionDeviceId() {
            return device;
        }

        @Override
        public Identifier infusionAddon() {
            return TTIds.rl("gametest");
        }
    }
}
