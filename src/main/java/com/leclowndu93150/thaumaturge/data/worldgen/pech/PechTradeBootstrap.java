package com.leclowndu93150.thaumaturge.data.worldgen.pech;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.pech.PechTradeTable;
import com.leclowndu93150.thaumaturge.content.pech.PechTradeTable.PechTrade;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

public final class PechTradeBootstrap {
    private PechTradeBootstrap() {}

    public static void bootstrap(BootstrapContext<PechTradeTable> ctx) {
        HolderGetter<IAspect> aspects = ctx.lookup(IAspect.REGISTRY_KEY);
        HolderGetter<Enchantment> enchantments = ctx.lookup(Registries.ENCHANTMENT);

        List<PechTrade> forager = new ArrayList<>();
        add(forager, 1, TTItems.CLUSTER_IRON.get());
        add(forager, 1, TTItems.CLUSTER_GOLD.get());
        add(forager, 1, TTItems.CLUSTER_CINNABAR.get());
        add(forager, 1, TTItems.CLUSTER_QUARTZ.get());
        add(forager, 1, TTItems.CLUSTER_COPPER.get());
        add(forager, 2, Items.BLAZE_ROD);
        add(forager, 2, TTBlocks.SAPLING_GREATWOOD.get());
        add(forager, 2, Items.DRAGON_BREATH);
        add(forager, 2, Items.COMPASS);
        add(forager, 3, Items.EXPERIENCE_BOTTLE);
        add(forager, 3, Items.EXPERIENCE_BOTTLE);
        add(forager, 3, Items.GOLDEN_APPLE);
        add(forager, 4, TTItems.THAUMIUM_PICKAXE.get());
        add(forager, 4, TTItems.THAUMIUM_AXE.get());
        add(forager, 4, TTItems.THAUMIUM_HOE.get());
        add(forager, 4, Items.SPECTRAL_ARROW);
        add(forager, 5, Items.ENCHANTED_GOLDEN_APPLE);
        add(forager, 5, TTBlocks.SAPLING_SILVERWOOD.get());
        add(forager, 5, Items.TOTEM_OF_UNDYING);
        add(forager, 5, TTItems.CURIO_KNOWLEDGE.get());
        register(ctx, "forager", forager);

        List<PechTrade> mage = new ArrayList<>();
        mage.add(crystal(aspects, 1, TTAspects.AER));
        mage.add(crystal(aspects, 1, TTAspects.TERRA));
        mage.add(crystal(aspects, 1, TTAspects.IGNIS));
        mage.add(crystal(aspects, 1, TTAspects.AQUA));
        mage.add(crystal(aspects, 1, TTAspects.ORDO));
        mage.add(crystal(aspects, 1, TTAspects.PERDITIO));
        mage.add(potion(2, Potions.REGENERATION));
        mage.add(potion(2, Potions.HEALING));
        mage.add(crystal(aspects, 2, TTAspects.VITIUM));
        add(mage, 3, Items.EXPERIENCE_BOTTLE);
        add(mage, 3, Items.EXPERIENCE_BOTTLE);
        mage.add(crystal(aspects, 3, TTAspects.AURAM));
        add(mage, 3, Items.GOLDEN_APPLE);
        add(mage, 4, TTItems.CLOTH_BOOTS.get());
        add(mage, 4, TTItems.CLOTH_CHEST.get());
        add(mage, 4, TTItems.CLOTH_LEGS.get());
        add(mage, 5, Items.ENCHANTED_GOLDEN_APPLE);
        add(mage, 5, TTItems.PECH_WAND.get());
        add(mage, 5, TTItems.CURIO_KNOWLEDGE.get());
        register(ctx, "mage", mage);

        List<PechTrade> stalker = new ArrayList<>();
        for (DyeColor dye : DyeColor.values()) {
            if (dye != DyeColor.WHITE) {
                add(stalker, 1, TTBlocks.CANDLES.get(dye).get());
            }
        }
        add(stalker, 2, Items.GHAST_TEAR);
        stalker.add(book(enchantments, 2, Enchantments.POWER));
        add(stalker, 3, Items.EXPERIENCE_BOTTLE);
        add(stalker, 3, Items.EXPERIENCE_BOTTLE);
        add(stalker, 3, Items.GOLDEN_APPLE);
        add(stalker, 4, TTItems.ELDRITCH_EYE.get());
        add(stalker, 4, Items.ENCHANTED_GOLDEN_APPLE);
        stalker.add(book(enchantments, 5, Enchantments.FLAME));
        stalker.add(book(enchantments, 5, Enchantments.INFINITY));
        add(stalker, 5, TTItems.CURIO_KNOWLEDGE.get());
        register(ctx, "stalker", stalker);
    }

    private static void register(BootstrapContext<PechTradeTable> ctx, String name, List<PechTrade> trades) {
        ctx.register(ResourceKey.create(PechTradeTable.REGISTRY_KEY, TTIds.rl(name)), new PechTradeTable(trades));
    }

    private static void add(List<PechTrade> trades, int tier, ItemLike item) {
        trades.add(new PechTrade(tier, new ItemStack(item)));
    }

    private static PechTrade crystal(HolderGetter<IAspect> aspects, int tier, ResourceKey<IAspect> aspect) {
        ItemStack stack = new ItemStack(TTItems.ESSENTIA_CRYSTAL.get());
        stack.set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspects.getOrThrow(aspect), 1));
        return new PechTrade(tier, stack);
    }

    private static PechTrade potion(int tier, Holder<Potion> potion) {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return new PechTrade(tier, stack);
    }

    private static PechTrade book(
            HolderGetter<Enchantment> enchantments, int tier, ResourceKey<Enchantment> enchantment) {
        ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        stored.set(enchantments.getOrThrow(enchantment), 1);
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        stack.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
        return new PechTrade(tier, stack);
    }
}
