package com.leclowndu93150.thaumaturge.data.loot;

import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class TreasureLootPools {
    public static final int COMMON = 0;
    public static final int UNCOMMON = 1;
    public static final int RARE = 2;

    private static final List<Holder<Potion>> LOOT_POTIONS = List.of(Potions.HEALING, Potions.REGENERATION, Potions.SWIFTNESS, Potions.STRENGTH, Potions.LEAPING, Potions.FIRE_RESISTANCE,
            Potions.NIGHT_VISION, Potions.WATER_BREATHING);

    private TreasureLootPools() {}

    public static LootPool.Builder treasurePool(HolderLookup.Provider registries, int rarity, NumberProvider rolls) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(rolls);
        pool.add(nugget(rarity));
        pool.add(counted(TCItems.SALIS_MUNDUS.get(), 1, 3 + rarity * 3));
        pool.add(counted(Items.CHORUS_FRUIT, 1, 5));
        pool.add(counted(Items.COMPASS, 1, 5));
        pool.add(counted(Items.COOKIE, 1, 5));
        pool.add(counted(Items.GOLD_INGOT, 1, 100));
        pool.add(counted(Items.ENDER_PEARL, 1, 100));
        pool.add(counted(Items.DIAMOND, 1, rarity == COMMON ? 10 : 50));
        pool.add(counted(Items.EMERALD, 1, rarity == COMMON ? 15 : 75));
        pool.add(counted(Items.EXPERIENCE_BOTTLE, 1, 5 * (1 << rarity)));
        pool.add(counted(Items.ENCHANTED_GOLDEN_APPLE, 1, 1 + rarity));
        pool.add(counted(Items.GOLDEN_APPLE, 1, 3 + rarity * 3));
        pool.add(LootItem.lootTableItem(Items.BOOK).setWeight(10).apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(5.0F, 5.0F + rarity * 13.0F))));
        switch (rarity) {
            case COMMON -> pool.add(pearl(1, 7));
            case UNCOMMON -> {
                pool.add(pearl(3, 7));
                pool.add(pearl(1, 6));
            }
            default -> {
                pool.add(pearl(9, 5));
                pool.add(pearl(3, 3));
                pool.add(pearl(1, 0));
                pool.add(counted(Items.NETHER_STAR, 1, 1));
            }
        }
        for (Holder<Potion> potion : LOOT_POTIONS) {
            pool.add(potionEntry(Items.POTION, potion));
            pool.add(potionEntry(Items.SPLASH_POTION, potion));
            if (rarity > COMMON) {
                pool.add(potionEntry(Items.LINGERING_POTION, potion));
            }
        }
        return pool;
    }

    private static LootPoolEntryContainer.Builder<?> potionEntry(ItemLike item, Holder<Potion> potion) {
        return LootItem.lootTableItem(item).setWeight(2).apply(SetPotionFunction.setPotion(potion));
    }

    private static LootPoolEntryContainer.Builder<?> counted(ItemLike item, int count, int weight) {
        return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)));
    }

    private static LootPoolEntryContainer.Builder<?> nugget(int rarity) {
        int weight = switch (rarity) {
            case UNCOMMON -> 2250;
            case RARE -> 2000;
            default -> 2500;
        };
        return counted(Items.GOLD_NUGGET, 1 + rarity, weight);
    }

    private static LootPoolEntryContainer.Builder<?> pearl(int weight, int damage) {
        return LootItem.lootTableItem(TCItems.PRIMORDIAL_PEARL.get()).setWeight(weight).apply(SetItemDamageFunction.setDamage(ConstantValue.exactly((float) damage / PrimordialPearlItem.MAX_DAMAGE)));
    }
}
