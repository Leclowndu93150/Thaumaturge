package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionRecipeInput;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.InfusionMatrixContext;
import com.leclowndu93150.thaumaturge.api.infusion.InfusionAutomationPolicy;
import com.leclowndu93150.thaumaturge.api.recipe.MinecraftEnchantmentInfusionRecipe;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionCraftJob;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipePreviews;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRecipeDisplay;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class DynamicInfusionTests {
    private DynamicInfusionTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("api/infusion_native_preview", 20, helper -> {
            var registries = helper.getLevel().registryAccess();
            var base = recipe(Enchantments.SHARPNESS, registries);
            var recipe = new MinecraftEnchantmentInfusionRecipe(base.enchantment(), Ingredient.of(Items.STICK, Items.DIAMOND_SWORD), base.components(), base.aspects(), base.instability(),
                    base.maxLevel(), base.researchGate());
            var preview = recipe.preview(registries).orElseThrow();
            helper.assertTrue(preview.input().catalyst().is(Items.DIAMOND_SWORD), "Preview must skip unsupported catalyst candidates");
            helper.assertTrue(ItemStack.matches(preview.output(), recipe.prepareResult(preview.input(), registries)) && preview.essentia().equals(recipe.quote(preview.input(), registries)),
                    "Preview result and cost must use the execution rules");
            preview.output().setDamageValue(50);
            helper.assertTrue(preview.output().getDamageValue() == 0, "Preview stacks must be defensive copies");
            var invalid = new InfusionRecipeInput(new ItemStack(Items.STICK), preview.input().components());
            helper.assertTrue(recipe.preview(invalid, registries).isEmpty(), "Invalid inputs must never advertise a result");
            var displays = InfusionRecipePreviews.displays(recipe, registries);
            helper.assertTrue(displays.size() == 1 && displays.getFirst() instanceof InfusionRecipeDisplay, "Research book must receive the native infusion display");
            var context = net.minecraft.world.item.crafting.display.SlotDisplayContext.fromLevel(helper.getLevel());
            helper.assertTrue(ItemStack.matches(displays.getFirst().result().resolveForFirstStack(context), preview.output()), "Book output must match the prepared preview");
            helper.succeed();
        });

        Identifier surcharge = Identifier.parse("thaumaturge_test:dynamic_infusion_surcharge");
        InfusionAutomationPolicy.register(surcharge, (context, player) -> 23);
        registrar.add("api/infusion_dynamic_matrix_start", 20, helper -> {
            var level = helper.getLevel();
            var player = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("dynamic_infusion".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "dynamic_infusion"));
            BlockPos matrixPos = new BlockPos(2, 3, 2);
            BlockPos centerPos = new BlockPos(2, 1, 2);
            BlockPos componentPos = new BlockPos(0, 1, 2);
            var pedestalBlock = BuiltInRegistries.BLOCK.getValue(Identifier.parse("thaumaturge:pedestal_arcane"));
            helper.setBlock(matrixPos, BuiltInRegistries.BLOCK.getValue(Identifier.parse("thaumaturge:infusion_matrix")));
            helper.setBlock(centerPos, pedestalBlock);
            helper.setBlock(componentPos, pedestalBlock);
            for (int offsetX : new int[]{-1, 1}) {
                for (int offsetZ : new int[]{-1, 1}) {
                    helper.setBlock(centerPos.offset(offsetX, 0, offsetZ), BuiltInRegistries.BLOCK.getValue(Identifier.parse("thaumaturge:pillar_arcane")));
                }
            }
            var matrix = (BlockEntityInfusionMatrix) level.getBlockEntity(helper.absolutePos(matrixPos));
            var center = (BlockEntityPedestal) level.getBlockEntity(helper.absolutePos(centerPos));
            var component = (BlockEntityPedestal) level.getBlockEntity(helper.absolutePos(componentPos));
            ItemStack catalyst = new ItemStack(Items.DIAMOND_SWORD);
            catalyst.setDamageValue(19);
            catalyst.set(DataComponents.CUSTOM_NAME, Component.literal("First blade"));
            center.setItem(catalyst);
            component.setItem(new ItemStack(Items.DIAMOND));
            matrix.onRightClick(level, player);
            var context = InfusionMatrixContext.of(player, helper.absolutePos(matrixPos));
            var inspected = InfusionCraftingTransaction.inspectStaged(context, player);
            helper.assertTrue(inspected.inspection().successful() && inspected.inspection().exactOutput(), "Native dynamic recipe must be matched by the real matrix");
            catalyst.set(DataComponents.CUSTOM_NAME, Component.literal("Replaced blade"));
            helper.assertTrue(InfusionCraftingTransaction.startAutomated(context, player, inspected, surcharge) == InfusionCraftingTransaction.Failure.INGREDIENTS_CHANGED,
                    "Same-item component changes must invalidate a staged inspection");
            helper.assertTrue(!matrix.isCrafting() && component.getItem().getCount() == 1 && matrix.remainingEssentia().isEmpty(), "Rejected start must not consume payment or inputs");
            var current = InfusionCraftingTransaction.inspectStaged(context, player);
            helper.assertTrue(InfusionCraftingTransaction.startAutomated(context, player, current, surcharge) == InfusionCraftingTransaction.Failure.NONE, "Revalidated dynamic recipe must start");
            helper.assertTrue(matrix.isCrafting() && matrix.craftRevision() == current.revision() + 1 && matrix.remainingEssentia().equals(current.inspection().essentia()),
                    "Start must retain the quoted multi-tick payment and advance revision");
            var clientData = matrix.getUpdateTag(level.registryAccess());
            helper.assertTrue(clientData.getIntOr("Instability", 0) == 27 && !clientData.contains("Job"), "Surcharge must apply once and mutable prepared jobs must stay server-side");
            helper.assertTrue(InfusionCraftingTransaction.startAutomated(context, player, current, surcharge) != InfusionCraftingTransaction.Failure.NONE,
                    "An accepted inspection must not start a second craft");
            helper.setBlock(matrixPos, net.minecraft.world.level.block.Blocks.AIR);
            helper.setBlock(matrixPos, BuiltInRegistries.BLOCK.getValue(Identifier.parse("thaumaturge:infusion_matrix")));
            matrix = (BlockEntityInfusionMatrix) level.getBlockEntity(helper.absolutePos(matrixPos));
            center.setItem(new ItemStack(Items.GLASS));
            component.setItem(new ItemStack(Items.EMERALD));
            matrix.onRightClick(level, player);
            var legacy = InfusionCraftingTransaction.inspectStaged(context, player);
            helper.assertTrue(legacy.inspection().successful() && legacy.inspection().instability() == 100, "Legacy recipe instability must retain its original bound");
            helper.assertTrue(InfusionCraftingTransaction.startAutomated(context, player, legacy, surcharge) == InfusionCraftingTransaction.Failure.NONE,
                    "Bounded automation surcharge must not cap an existing recipe's base instability");
            helper.assertTrue(matrix.getUpdateTag(level.registryAccess()).getIntOr("Instability", 0) == 123, "Legacy base instability and surcharge must be added without clamping");
            helper.succeed();
        });

        registrar.add("api/infusion_input_snapshot", 20, helper -> {
            ItemStack catalyst = new ItemStack(Items.DIAMOND_SWORD);
            ItemStack component = new ItemStack(Items.DIAMOND, 4);
            InfusionRecipeInput input = new InfusionRecipeInput(catalyst, List.of(component));
            catalyst.setDamageValue(20);
            component.setCount(1);
            input.catalyst().setDamageValue(30);
            input.components().getFirst().setCount(10);
            helper.assertTrue(input.catalyst().getDamageValue() == 0 && input.components().getFirst().getCount() == 1, "Input snapshots must not expose mutable stacks");
            helper.succeed();
        });

        registrar.add("api/infusion_enchantment_result", 20, helper -> {
            var registries = helper.getLevel().registryAccess();
            var enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
            ItemStack catalyst = new ItemStack(Items.DIAMOND_SWORD);
            catalyst.setDamageValue(31);
            catalyst.set(DataComponents.CUSTOM_NAME, Component.literal("Tempered blade"));
            catalyst.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 1);
            var recipe = recipe(Enchantments.SHARPNESS, registries);
            var input = new InfusionRecipeInput(catalyst, List.of(new ItemStack(Items.DIAMOND)));
            helper.assertTrue(recipe.matches(input, registries), "Compatible named damaged catalyst must match");
            ItemStack result = recipe.prepareResult(input, registries);
            ItemStack expected = catalyst.copy();
            expected.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 1);
            helper.assertTrue(ItemStack.matches(result, expected), "Only the requested enchantment may change");
            helper.assertTrue(catalyst.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(enchantments.getOrThrow(Enchantments.SHARPNESS)) == 0,
                    "Preparation must not mutate the catalyst");
            var upgradedInput = new InfusionRecipeInput(result, input.components());
            helper.assertTrue(
                    recipe.prepareResult(upgradedInput, registries).getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(enchantments.getOrThrow(Enchantments.SHARPNESS)) == 2,
                    "Repeated infusion must advance one level");
            result.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 5);
            helper.assertTrue(!recipe.matches(new InfusionRecipeInput(result, input.components()), registries), "Maximum level must fail before payment");
            catalyst.enchant(enchantments.getOrThrow(Enchantments.SMITE), 1);
            helper.assertTrue(!recipe.matches(new InfusionRecipeInput(catalyst, input.components()), registries), "Exclusive enchantments must be rejected");
            helper.succeed();
        });

        registrar.add("api/infusion_enchantment_codecs", 20, helper -> {
            var registries = helper.getLevel().registryAccess();
            var recipe = recipe(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse("addon:binding")), registries);
            var ops = registries.createSerializationContext(JsonOps.INSTANCE);
            var encoded = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow();
            var decoded = (MinecraftEnchantmentInfusionRecipe) Recipe.CODEC.parse(ops, encoded).getOrThrow();
            helper.assertTrue(decoded.enchantment().equals(recipe.enchantment()), "Recipe codec must preserve addon enchantment keys");
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
            try {
                MinecraftEnchantmentInfusionRecipe.STREAM_CODEC.encode(buffer, recipe);
                var network = MinecraftEnchantmentInfusionRecipe.STREAM_CODEC.decode(buffer);
                helper.assertTrue(network.enchantment().equals(recipe.enchantment()), "Network codec must preserve resource keys");
                helper.assertTrue(!network.matches(new InfusionRecipeInput(new ItemStack(Items.DIAMOND_SWORD), List.of(new ItemStack(Items.DIAMOND))), registries),
                        "Unknown enchantments must be rejected without preparing a result");
            } finally {
                buffer.release();
            }
            helper.succeed();
        });

        registrar.add("api/infusion_prepared_save", 20, helper -> {
            var registries = helper.getLevel().registryAccess();
            ItemStack catalyst = new ItemStack(Items.DIAMOND_SWORD);
            catalyst.set(DataComponents.CUSTOM_NAME, Component.literal("Original"));
            var recipe = recipe(Enchantments.SHARPNESS, registries);
            var input = new InfusionRecipeInput(catalyst, List.of(new ItemStack(Items.DIAMOND)));
            ItemStack result = recipe.prepareResult(input, registries);
            var id = ResourceKey.create(Registries.RECIPE, Identifier.parse("addon:prepared"));
            var job = new InfusionCraftJob(input.components(), recipe.quote(input, registries), recipe.quote(input, registries), result, catalyst, 4, Optional.empty(),
                    Optional.of(new InfusionCraftJob.PreparedRecipe(id, 1, input.components())));
            job.ingredients().clear();
            job.setEssentia(AspectList.EMPTY);
            var ops = registries.createSerializationContext(JsonOps.INSTANCE);
            var decoded = InfusionCraftJob.CODEC.parse(ops, InfusionCraftJob.CODEC.encodeStart(ops, job).getOrThrow()).getOrThrow();
            helper.assertTrue(decoded.ingredients().isEmpty() && decoded.essentia().isEmpty(), "Reload must not replay completed payment");
            helper.assertTrue(ItemStack.matches(decoded.result(), result) && decoded.preparedRecipe().orElseThrow().id().equals(id), "Reload must preserve the accepted result and recipe identity");
            helper.assertTrue(decoded.preparedRecipe().orElseThrow().components().size() == 1, "Original components must survive consumption");
            helper.succeed();
        });
    }

    private static MinecraftEnchantmentInfusionRecipe recipe(ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment, net.minecraft.core.HolderLookup.Provider registries) {
        return new MinecraftEnchantmentInfusionRecipe(enchantment, Ingredient.of(Items.DIAMOND_SWORD), List.of(Ingredient.of(Items.DIAMOND)),
                AspectList.of(new AspectInstance(Aspects.resolve(registries, TTAspects.AER), 10)), 4, 5, Optional.empty());
    }
}
