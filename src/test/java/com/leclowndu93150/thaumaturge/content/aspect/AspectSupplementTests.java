package com.leclowndu93150.thaumaturge.content.aspect;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectSupplement;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectRecipeContributor;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AspectSupplementTests {
    private AspectSupplementTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("aspects/supplement_dependencies_and_rebuild", 20, helper -> {
            var registries = helper.getLevel().registryAccess();
            var terra = Aspects.resolve(registries, TTAspects.TERRA);
            var magic = Aspects.resolve(registries, TTAspects.PRAECANTATIO);
            Map<Item, AspectList> base = Map.of(Items.STONE, AspectList.EMPTY.add(terra, 2));
            Map<Item, AspectSupplement> supplements = Map.of(Items.STONE_BRICKS, new AspectSupplement(Map.of(TTAspects.PRAECANTATIO.identifier(), 3)));
            AtomicInteger derived = new AtomicInteger();
            IAspectRecipeContributor contributor = (item, recipes, lookup, partial) -> {
                if (item == Items.STONE_BRICKS) {
                    derived.incrementAndGet();
                    return Optional.of(partial.of(Items.STONE));
                }
                return item == Items.CHISELED_STONE_BRICKS ? Optional.of(partial.of(Items.STONE_BRICKS)) : Optional.empty();
            };
            for (int rebuild = 0; rebuild < 2; rebuild++) {
                var index = new AspectIndexBuilder.RecursiveIndex(base, supplements, helper.getLevel().getServer().getRecipeManager(), registries, List.of(contributor));
                AspectList dependent = index.of(Items.CHISELED_STONE_BRICKS);
                helper.assertTrue(dependent.amountOf(terra) == 2 && dependent.amountOf(magic) == 3, "Dependent recipe lost material or supplement aspects");
                helper.assertTrue(index.of(Items.STONE_BRICKS).amountOf(magic) == 3, "Repeated lookup added a supplement twice");
                AspectIndex snapshot = AspectIndex.of(index.resolved());
                RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
                try {
                    AspectIndex.STREAM_CODEC.encode(buffer, snapshot);
                    AspectIndex client = AspectIndex.STREAM_CODEC.decode(buffer);
                    helper.assertTrue(client.of(new ItemStack(Items.CHISELED_STONE_BRICKS)).equals(dependent), "Client snapshot differs from server composition");
                } finally {
                    buffer.release();
                }
            }
            helper.assertTrue(derived.get() == 2, "Each build must derive an item exactly once");
            helper.succeed();
        });

        registrar.add("aspects/explicit_empty_and_caps", 20, helper -> {
            var registries = helper.getLevel().registryAccess();
            var terra = Aspects.resolve(registries, TTAspects.TERRA);
            AspectList material = AspectList.EMPTY.add(terra, 490);
            Map<Item, AspectList> base = Map.of(Items.STONE, AspectList.EMPTY, Items.COBBLESTONE, material);
            AspectSupplement supplement = new AspectSupplement(Map.of(TTAspects.TERRA.identifier(), 20, Identifier.fromNamespaceAndPath("example", "missing_aspect"), 5));
            Map<Item, AspectSupplement> supplements = Map.of(Items.STONE, supplement, Items.COBBLESTONE, supplement, Items.STONE_BRICKS, supplement);
            IAspectRecipeContributor contributor = (item, recipes, lookup, partial) -> Optional.of(material);
            var index = new AspectIndexBuilder.RecursiveIndex(base, supplements, helper.getLevel().getServer().getRecipeManager(), registries, List.of(contributor));
            helper.assertTrue(index.of(Items.STONE).isEmpty(), "An explicit empty base must suppress recipes and supplements");
            helper.assertTrue(index.of(Items.COBBLESTONE).amountOf(terra) == 490, "An explicit base must suppress supplements");
            helper.assertTrue(index.of(Items.STONE_BRICKS).amountOf(terra) == 500 && index.of(Items.STONE_BRICKS).size() == 1, "Derived aspects must be capped and unknown supplements discarded");
            var absent = new AspectIndexBuilder.RecursiveIndex(Map.of(), Map.of(), helper.getLevel().getServer().getRecipeManager(), registries, List.of(contributor));
            helper.assertTrue(absent.of(Items.STONE).amountOf(terra) == 490, "An absent base must still derive aspects");
            helper.succeed();
        });

        registrar.add("aspects/supplement_validation_and_cycles", 20, helper -> {
            Map<Identifier, Integer> source = new HashMap<>(Map.of(TTAspects.TERRA.identifier(), 1));
            AspectSupplement supplement = new AspectSupplement(source);
            source.clear();
            helper.assertTrue(supplement.amounts().size() == 1, "Supplement retained mutable input");
            for (String invalid : List.of("{\"thaumaturge:terra\":-1}", "{\"thaumaturge:terra\":501}")) {
                helper.assertTrue(AspectSupplement.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(invalid)).result().isEmpty(), "Out-of-range supplement was accepted");
            }
            IAspectRecipeContributor contributor = (item, recipes, lookup, partial) -> Optional.of(partial.of(item == Items.STONE ? Items.COBBLESTONE : Items.STONE));
            var index = new AspectIndexBuilder.RecursiveIndex(Map.of(), Map.of(), helper.getLevel().getServer().getRecipeManager(), helper.getLevel().registryAccess(), List.of(contributor));
            helper.assertTrue(index.of(Items.STONE).isEmpty(), "Recipe cycles must terminate without inventing aspects");
            helper.succeed();
        });
    }
}
