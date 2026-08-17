package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.content.infernalfurnace.InfernalBonus;
import com.leclowndu93150.thaumaturge.registry.TCItemTags;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class InfernalBonusProvider extends DataMapProvider {
    public InfernalBonusProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        HolderLookup<Item> items = provider.lookupOrThrow(Registries.ITEM);
        Builder<List<InfernalBonus>, Item> b = builder(InfernalBonus.DATA_MAP);

        add(
                b,
                Tags.Items.ORES_IRON,
                InfernalBonus.builder(items, Tags.Items.NUGGETS_IRON)
                        .count(2)
                        .chance(0.33F)
                        .build());
        add(
                b,
                Tags.Items.ORES_COPPER,
                InfernalBonus.builder(items, TCItemTags.NUGGETS_COPPER)
                        .count(2)
                        .chance(0.33F)
                        .build());
        add(
                b,
                Tags.Items.ORES_GOLD,
                InfernalBonus.builder(items, Tags.Items.NUGGETS_GOLD)
                        .count(2)
                        .chance(0.33F)
                        .build());
        add(
                b,
                Tags.Items.RAW_MATERIALS_IRON,
                InfernalBonus.builder(items, Tags.Items.NUGGETS_IRON)
                        .chance(0.33F)
                        .build());
        add(
                b,
                Tags.Items.RAW_MATERIALS_COPPER,
                InfernalBonus.builder(items, TCItemTags.NUGGETS_COPPER)
                        .chance(0.33F)
                        .build());
        add(
                b,
                Tags.Items.RAW_MATERIALS_GOLD,
                InfernalBonus.builder(items, Tags.Items.NUGGETS_GOLD)
                        .chance(0.33F)
                        .build());
        add(
                b,
                Tags.Items.ORES_QUARTZ,
                InfernalBonus.builder(items, TCItemTags.NUGGETS_QUARTZ)
                        .count(2)
                        .chance(0.33F)
                        .build());
        add(
                b,
                TCItemTags.ORES_CINNABAR,
                InfernalBonus.builder(items, TCItemTags.NUGGETS_QUICKSILVER)
                        .count(2)
                        .chance(0.33F)
                        .build());
        add(
                b,
                TCItemTags.ORES_LEAD,
                InfernalBonus.builder(items, TCItemTags.NUGGETS_LEAD)
                        .count(2)
                        .chance(0.33F)
                        .build());
        add(
                b,
                TCItemTags.ORES_SILVER,
                InfernalBonus.builder(items, TCItemTags.NUGGETS_SILVER)
                        .count(2)
                        .chance(0.33F)
                        .build());
        add(
                b,
                TCItemTags.ORES_TIN,
                InfernalBonus.builder(items, TCItemTags.NUGGETS_TIN)
                        .count(2)
                        .chance(0.33F)
                        .build());

        add(
                b,
                Items.BEEF,
                InfernalBonus.builder(TCItems.CHUNK_BEEF).chance(0.33F).build());
        add(
                b,
                Items.CHICKEN,
                InfernalBonus.builder(TCItems.CHUNK_CHICKEN).chance(0.33F).build());
        add(
                b,
                Items.PORKCHOP,
                InfernalBonus.builder(TCItems.CHUNK_PORK).chance(0.33F).build());
        add(
                b,
                Items.COD,
                InfernalBonus.builder(TCItems.CHUNK_FISH).chance(0.33F).build());
        add(
                b,
                Items.SALMON,
                InfernalBonus.builder(TCItems.CHUNK_FISH).chance(0.33F).build());
        add(
                b,
                Items.TROPICAL_FISH,
                InfernalBonus.builder(TCItems.CHUNK_FISH).chance(0.33F).build());
        add(
                b,
                Items.PUFFERFISH,
                InfernalBonus.builder(TCItems.CHUNK_FISH).chance(0.33F).build());
        add(
                b,
                Items.RABBIT,
                InfernalBonus.builder(TCItems.CHUNK_RABBIT).chance(0.33F).build());
        add(
                b,
                Items.MUTTON,
                InfernalBonus.builder(TCItems.CHUNK_MUTTON).chance(0.33F).build());

        add(
                b,
                TCItemTags.RARE_EARTH_CHANCE_HIGH,
                InfernalBonus.builder(TCItems.RARE_EARTH).chance(0.025F).build());
        add(
                b,
                TCItemTags.RARE_EARTH_CHANCE_NORMAL,
                InfernalBonus.builder(TCItems.RARE_EARTH).chance(0.02F).build());
        add(
                b,
                TCItemTags.RARE_EARTH_CHANCE_LOW,
                InfernalBonus.builder(TCItems.RARE_EARTH).chance(0.01F).build());
    }

    @Override
    public String getName() {
        return "Infernal Furnace Bonus Data Map";
    }

    private static void add(Builder<List<InfernalBonus>, Item> b, ItemLike key, InfernalBonus... values) {
        b.add(key.asItem().builtInRegistryHolder(), Arrays.stream(values).toList(), false);
    }

    private static void add(Builder<List<InfernalBonus>, Item> b, TagKey<Item> key, InfernalBonus... values) {
        b.add(key, Arrays.stream(values).toList(), false);
    }
}
