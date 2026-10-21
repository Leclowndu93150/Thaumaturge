package com.leclowndu93150.thaumaturge.gametest;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneShapedCraftingRecipe;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.workbench.InventoryArcaneWorkbench;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import com.leclowndu93150.thaumaturge.api.wands.RegisterWandAssemblyEvent;
import com.leclowndu93150.thaumaturge.api.wands.WandAssemblyPart;
import com.leclowndu93150.thaumaturge.api.wands.WandAssemblyPart.Variant;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.wands.assembly.WandAssemblyPackResources;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HexFormat;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public final class WandAssemblyApiTests {
    private WandAssemblyApiTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("wand_assembly/cross_addon_recipe", 20, helper -> {
            PackLocationInfo location = new PackLocationInfo("assembly_test", Component.literal("Assembly test"), PackSource.BUILT_IN, Optional.empty());
            Identifier recipeId = combination(WandAssemblyFixtures.ROD);
            try (var pack = new WandAssemblyPackResources(location)) {
                helper.assertTrue(pack.getResource(PackType.SERVER_DATA, combination(WandAssemblyFixtures.OTHER_ROD)) != null, "Equal part paths in different namespaces must not collide");
                helper.assertTrue(pack.getResource(PackType.SERVER_DATA, combination(WandAssemblyFixtures.DISABLED_ROD)) == null, "Disabled descriptors must not generate recipes");
                var resource = pack.getResource(PackType.SERVER_DATA, recipeId);
                helper.assertTrue(resource != null, "Parts from separate addons must generate their cross combination");
                var ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
                ArcaneShapedCraftingRecipe decoded;
                try (var input = resource.get()) {
                    decoded = (ArcaneShapedCraftingRecipe) Recipe.CODEC.parse(ops, JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))).getOrThrow();
                }
                var roundTrip = (ArcaneShapedCraftingRecipe) Recipe.CODEC.parse(ops, Recipe.CODEC.encodeStart(ops, decoded).getOrThrow()).getOrThrow();
                var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
                ArcaneShapedCraftingRecipe network;
                try {
                    ArcaneShapedCraftingRecipe.STREAM_CODEC.encode(buffer, roundTrip);
                    network = ArcaneShapedCraftingRecipe.STREAM_CODEC.decode(buffer);
                    helper.assertTrue(!buffer.isReadable(), "Recipe network decoding must consume the entire payload");
                } finally {
                    buffer.release();
                }
                var player = TTTestPlayers.create(helper);
                try {
                    var inventory = new InventoryArcaneWorkbench();
                    inventory.setItem(2, new ItemStack(Items.GOLD_NUGGET));
                    inventory.setItem(4, new ItemStack(Items.BLAZE_ROD));
                    inventory.setItem(6, new ItemStack(Items.GOLD_NUGGET));
                    var input = inventory.asArcaneCraftInput().withPlayer(player);
                    var context = ArcaneWorkbenchContext.virtual(player, UUID.randomUUID(), null);
                    PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
                    knowledge.addResearch(WandAssemblyFixtures.ROD_RESEARCH);
                    knowledge.markComplete(WandAssemblyFixtures.ROD_RESEARCH);
                    var locked = ArcaneCraftingTransaction.inspect(context, player, input);
                    helper.assertTrue(locked.failure() == ArcaneCraftingTransaction.Failure.RESEARCH_LOCKED, "The cap gate must remain required after satisfying the rod gate");
                    knowledge.addResearch(WandAssemblyFixtures.CAP_RESEARCH);
                    knowledge.markComplete(WandAssemblyFixtures.CAP_RESEARCH);
                    var inspection = ArcaneCraftingTransaction.inspect(context, player, input);
                    helper.assertTrue(inspection.failure() == ArcaneCraftingTransaction.Failure.NONE, "Both addon gates must unlock the generated recipe");
                    helper.assertTrue(inspection.requirements().baseVis() == 36 && decoded.visCost() == 36 && network.visCost() == 36, "Inspection and codecs must preserve the generated vis cost");
                    helper.assertTrue(network.researchGate().equals(decoded.researchGate()), "Recipe codecs must preserve both research gates");
                    helper.assertTrue(decoded.matches(input, helper.getLevel()) && network.matches(input, helper.getLevel()), "Recipe codecs must preserve addon ingredients");
                    helper.assertTrue(ItemStack.matches(inspection.output(), decoded.assemble(input)) && ItemStack.matches(inspection.output(), network.assemble(input)),
                            "Inspection and recipe assembly must produce identical registered parts");
                } finally {
                    player.connection.disconnect(Component.literal("Assembly test complete"));
                }
            } catch (java.io.IOException exception) {
                helper.fail("Could not read cross-addon assembly recipe: " + exception.getMessage());
            }
            helper.succeed();
        });
        registrar.add("wand_assembly/registration_conflicts", 20, helper -> {
            RegisterWandAssemblyEvent event = new RegisterWandAssemblyEvent();
            var part = new WandAssemblyPart<>(ResourceKey.create(WandRod.REGISTRY_KEY, Identifier.parse("fixture:rod")), Ingredient.of(Items.STICK), List.of(), Set.of(Variant.WAND), true);
            event.registerRod(part);
            boolean duplicateRejected = false;
            try {
                event.registerRod(part);
            } catch (IllegalArgumentException expected) {
                duplicateRejected = true;
            }
            helper.assertTrue(duplicateRejected, "Duplicate part registrations must fail");
            event.close();
            boolean lateRejected = false;
            try {
                event.registerRod(part);
            } catch (IllegalStateException expected) {
                lateRejected = true;
            }
            helper.assertTrue(lateRejected, "Retained registration events must reject changes");
            helper.succeed();
        });
        registrar.add("wand_assembly/generated_pack_reload", 20, helper -> {
            PackLocationInfo location = new PackLocationInfo("assembly_test", Component.literal("Assembly test"), PackSource.BUILT_IN, Optional.empty());
            Identifier recipe = Identifier.parse("thaumaturge:recipe/wand/assembly/iron_greatwood.json");
            try (var first = new WandAssemblyPackResources(location); var second = new WandAssemblyPackResources(location)) {
                var firstResource = first.getResource(PackType.SERVER_DATA, recipe);
                var secondResource = second.getResource(PackType.SERVER_DATA, recipe);
                helper.assertTrue(firstResource != null && secondResource != null, "Base recipe identifiers must survive reopening");
                try (var firstInput = firstResource.get(); var secondInput = secondResource.get()) {
                    var firstJson = JsonParser.parseReader(new InputStreamReader(firstInput, StandardCharsets.UTF_8));
                    var secondJson = JsonParser.parseReader(new InputStreamReader(secondInput, StandardCharsets.UTF_8));
                    helper.assertTrue(firstJson.equals(secondJson), "Generated recipes must be stable across reloads");
                    helper.assertTrue(firstJson.getAsJsonObject().getAsJsonObject("research").has("all_of"), "Generated recipes must retain conjunctive gates");
                }
                helper.assertTrue(first.getResource(PackType.SERVER_DATA, Identifier.parse("thaumaturge:recipe/wand/sceptre/iron_greatwood_staff.json")) == null,
                        "Staff rods must not generate sceptres");
            } catch (java.io.IOException exception) {
                helper.fail("Could not read generated assembly recipes: " + exception.getMessage());
            }
            helper.succeed();
        });
    }

    private static Identifier combination(Identifier rod) {
        String capId = HexFormat.of().formatHex(WandAssemblyFixtures.CAP.toString().getBytes(StandardCharsets.UTF_8));
        String rodId = HexFormat.of().formatHex(rod.toString().getBytes(StandardCharsets.UTF_8));
        return Identifier.parse("thaumaturge:recipe/wand/assembly/external/" + capId + "/" + rodId + ".json");
    }
}
