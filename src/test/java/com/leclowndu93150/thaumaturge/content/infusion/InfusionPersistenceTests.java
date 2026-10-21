package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class InfusionPersistenceTests {
    private InfusionPersistenceTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("api/infusion_absent_recipe_checkpoint", 20, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos matrixPosition = helper.absolutePos(new BlockPos(2, 3, 2));
            BlockPos centerPosition = helper.absolutePos(new BlockPos(2, 1, 2));
            BlockPos componentPosition = helper.absolutePos(new BlockPos(0, 1, 2));
            var matrixState = BuiltInRegistries.BLOCK.getValue(Identifier.parse("thaumaturge:infusion_matrix")).defaultBlockState();
            var pedestalState = BuiltInRegistries.BLOCK.getValue(Identifier.parse("thaumaturge:pedestal_arcane")).defaultBlockState();
            level.setBlockAndUpdate(matrixPosition, matrixState);
            level.setBlockAndUpdate(centerPosition, pedestalState);
            level.setBlockAndUpdate(componentPosition, pedestalState);
            for (int offsetX : new int[]{-1, 1}) {
                for (int offsetZ : new int[]{-1, 1}) {
                    level.setBlockAndUpdate(centerPosition.offset(offsetX, 0, offsetZ), BuiltInRegistries.BLOCK.getValue(Identifier.parse("thaumaturge:pillar_arcane")).defaultBlockState());
                }
            }
            var matrix = (BlockEntityInfusionMatrix) level.getBlockEntity(matrixPosition);
            var center = (BlockEntityPedestal) level.getBlockEntity(centerPosition);
            var component = (BlockEntityPedestal) level.getBlockEntity(componentPosition);
            ItemStack catalyst = new ItemStack(Items.DIAMOND_SWORD);
            catalyst.setDamageValue(37);
            catalyst.set(DataComponents.CUSTOM_NAME, Component.literal("Persistent blade"));
            catalyst.set(DataComponents.REPAIR_COST, 7);
            catalyst.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING), 2);
            ItemStack expected = catalyst.copy();
            expected.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), 1);
            center.setItem(catalyst.copy());
            component.setItem(new ItemStack(Items.DIAMOND));
            var player = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "infusion_persistence"));
            matrix.onRightClick(level, player);
            var context = InfusionMatrixContext.of(player, matrixPosition);
            var inspection = InfusionCraftingTransaction.inspectStaged(context, player);
            helper.assertTrue(ItemStack.matches(expected, inspection.inspection().output()), "Native inspection must preserve every unrelated catalyst component");
            helper.assertTrue(InfusionCraftingTransaction.start(context, player, inspection) == InfusionCraftingTransaction.Failure.NONE, "Native infusion must start before checkpointing");

            CompoundTag checkpoint = save(level, matrix);
            var operations = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var accepted = InfusionCraftJob.CODEC.parse(operations, checkpoint.getCompound("Job").orElseThrow()).getOrThrow();
            ResourceKey<Recipe<?>> absentRecipe = ResourceKey.create(Registries.RECIPE, Identifier.parse("thaumaturge_test:removed_infusion_checkpoint"));
            helper.assertTrue(level.recipeAccess().byKey(absentRecipe).isEmpty(), "Checkpoint recipe must be absent from the recipe manager");
            var prepared = accepted.preparedRecipe().orElseThrow();
            var paid = new InfusionCraftJob(accepted.ingredients(), AspectList.EMPTY, accepted.quotedEssentia(), accepted.result(), accepted.catalyst(), accepted.instability(), accepted.player(),
                    Optional.of(new InfusionCraftJob.PreparedRecipe(absentRecipe, prepared.version(), prepared.components())));
            checkpoint.put("Job", InfusionCraftJob.CODEC.encodeStart(operations, paid).getOrThrow());
            checkpoint.putFloat("Stability", BlockEntityInfusionMatrix.STABILITY_CAP);
            matrix = restore(level, matrix, checkpoint);
            for (int tick = 0; tick < 100 && !component.getItem().isEmpty(); tick++) {
                BlockEntityInfusionMatrix.serverTick(level, matrixPosition, matrixState, matrix);
            }
            helper.assertTrue(component.getItem().isEmpty() && matrix.isCrafting(), "Restored paid job must consume its component before finishing");
            helper.assertTrue(ItemStack.matches(center.getItem(), catalyst), "Ingredient consumption must not replace the catalyst early");
            checkpoint = save(level, matrix);
            var interrupted = InfusionCraftJob.CODEC.parse(operations, checkpoint.getCompound("Job").orElseThrow()).getOrThrow();
            helper.assertTrue(interrupted.ingredients().isEmpty() && interrupted.essentia().isEmpty(), "Checkpoint must retain completed payment and ingredient consumption");
            matrix = restore(level, matrix, checkpoint);
            component.setItem(new ItemStack(Items.DIAMOND));
            long runningRevision = matrix.craftRevision();
            for (int tick = 0; tick < 100 && matrix.isCrafting(); tick++) {
                BlockEntityInfusionMatrix.serverTick(level, matrixPosition, matrixState, matrix);
            }
            helper.assertTrue(!matrix.isCrafting() && matrix.craftRevision() == runningRevision + 1, "Absent recipe checkpoint must finish exactly once");
            helper.assertTrue(ItemStack.matches(expected, center.getItem()), "Restart must preserve the prepared enchantment result and all catalyst components");
            helper.assertTrue(component.getItem().is(Items.DIAMOND), "Restart must not consume an already paid component again");
            long finishedRevision = matrix.craftRevision();
            matrix = restore(level, matrix, save(level, matrix));
            center.setItem(ItemStack.EMPTY);
            for (int tick = 0; tick < 40; tick++) {
                BlockEntityInfusionMatrix.serverTick(level, matrixPosition, matrixState, matrix);
            }
            helper.assertTrue(!matrix.isCrafting() && matrix.craftRevision() == finishedRevision && center.getItem().isEmpty(), "Reloading a completed job must not recreate collected output");
            helper.assertTrue(component.getItem().is(Items.DIAMOND), "Completed reload must not repeat ingredient consumption");
            helper.succeed();
        });
    }

    private static CompoundTag save(ServerLevel level, BlockEntityInfusionMatrix matrix) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        matrix.saveAdditional(output);
        return output.buildResult();
    }

    private static BlockEntityInfusionMatrix restore(ServerLevel level, BlockEntityInfusionMatrix previous, CompoundTag checkpoint) {
        var matrix = new BlockEntityInfusionMatrix(previous.getBlockPos(), previous.getBlockState());
        matrix.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), checkpoint));
        level.removeBlockEntity(previous.getBlockPos());
        level.setBlockEntity(matrix);
        return matrix;
    }
}
