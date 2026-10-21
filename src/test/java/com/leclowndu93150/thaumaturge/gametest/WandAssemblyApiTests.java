package com.leclowndu93150.thaumaturge.gametest;

import com.google.gson.JsonParser;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public final class WandAssemblyApiTests {
    private WandAssemblyApiTests() {}

    public static void register(TTTestRegistrar registrar) {
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
}
