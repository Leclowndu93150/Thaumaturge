package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.content.infernalfurnace.InfernalBonus;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
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
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class InfernalBonusProvider extends DataMapProvider {
    public InfernalBonusProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        HolderLookup<Item> items = provider.lookupOrThrow(Registries.ITEM);
        Builder<List<InfernalBonus>, Item> b = builder(InfernalBonus.DATA_MAP);

        add(b, Tags.Items.ORES_IRON, InfernalBonus.builder(items, Tags.Items.NUGGETS_IRON).count(2).chance(0.33F).build());
        add(b, Tags.Items.ORES_COPPER, InfernalBonus.builder(items, Tags.Items.NUGGETS_COPPER).count(2).chance(0.33F).build());
        add(b, Tags.Items.ORES_GOLD, InfernalBonus.builder(items, Tags.Items.NUGGETS_GOLD).count(2).chance(0.33F).build());
        add(b, Tags.Items.ORES_QUARTZ, InfernalBonus.builder(items, TTItemTags.NUGGETS_QUARTZ).count(2).chance(0.33F).build());
        add(b, TTItemTags.ORES_CINNABAR, InfernalBonus.builder(items, TTItemTags.NUGGETS_QUICKSILVER).count(2).chance(0.33F).build());
        add(b, TTItemTags.RAW_MATERIALS_CINNABAR, InfernalBonus.builder(items, TTItemTags.NUGGETS_QUICKSILVER).chance(0.33F).build());
        addConditional(b, TTItemTags.ORES_LEAD, new NotCondition(new TagEmptyCondition<>(TTItemTags.NUGGETS_LEAD)),
                InfernalBonus.builder(items, TTItemTags.NUGGETS_LEAD).count(2).chance(0.33F).build());
        addConditional(b, TTItemTags.ORES_SILVER, new NotCondition(new TagEmptyCondition<>(TTItemTags.NUGGETS_SILVER)),
                InfernalBonus.builder(items, TTItemTags.NUGGETS_SILVER).count(2).chance(0.33F).build());
        addConditional(b, TTItemTags.ORES_TIN, new NotCondition(new TagEmptyCondition<>(TTItemTags.NUGGETS_TIN)), InfernalBonus.builder(items, TTItemTags.NUGGETS_TIN).count(2).chance(0.33F).build());

        add(b, Tags.Items.RAW_MATERIALS_IRON, InfernalBonus.builder(items, Tags.Items.NUGGETS_IRON).chance(0.33F).build());
        add(b, Tags.Items.RAW_MATERIALS_COPPER, InfernalBonus.builder(items, Tags.Items.NUGGETS_COPPER).chance(0.33F).build());
        add(b, Tags.Items.RAW_MATERIALS_GOLD, InfernalBonus.builder(items, Tags.Items.NUGGETS_GOLD).chance(0.33F).build());

        add(b, Items.BEEF, InfernalBonus.builder(TTItems.CHUNK_BEEF).chance(0.33F).build());
        add(b, Items.CHICKEN, InfernalBonus.builder(TTItems.CHUNK_CHICKEN).chance(0.33F).build());
        add(b, Items.PORKCHOP, InfernalBonus.builder(TTItems.CHUNK_PORK).chance(0.33F).build());
        add(b, Items.COD, InfernalBonus.builder(TTItems.CHUNK_FISH).chance(0.33F).build());
        add(b, Items.SALMON, InfernalBonus.builder(TTItems.CHUNK_FISH).chance(0.33F).build());
        add(b, Items.TROPICAL_FISH, InfernalBonus.builder(TTItems.CHUNK_FISH).chance(0.33F).build());
        add(b, Items.PUFFERFISH, InfernalBonus.builder(TTItems.CHUNK_FISH).chance(0.33F).build());
        add(b, Items.RABBIT, InfernalBonus.builder(TTItems.CHUNK_RABBIT).chance(0.33F).build());
        add(b, Items.MUTTON, InfernalBonus.builder(TTItems.CHUNK_MUTTON).chance(0.33F).build());

        add(b, TTItemTags.RARE_EARTH_CHANCE_HIGH, InfernalBonus.builder(TTItems.RARE_EARTH).chance(0.025F).build());
        add(b, TTItemTags.RARE_EARTH_CHANCE_NORMAL, InfernalBonus.builder(TTItems.RARE_EARTH).chance(0.02F).build());
        add(b, TTItemTags.RARE_EARTH_CHANCE_LOW, InfernalBonus.builder(TTItems.RARE_EARTH).chance(0.01F).build());
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

    private static void addConditional(Builder<List<InfernalBonus>, Item> b, TagKey<Item> key, ICondition condition, InfernalBonus... values) {
        b.add(key, Arrays.stream(values).toList(), false, condition);
    }
}
