package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCItemTags;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, TCIds.MODID);

    public static final Tier TOOL_THAUMIUM = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 7.0F, 2.5F, 22, () -> Ingredient.of(TCItemTags.INGOTS_THAUMIUM));
    public static final Tier TOOL_VOID = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            150,
            8.0F,
            3.0F,
            10,
            () -> Ingredient.of(TCItemTags.INGOTS_VOID_METAL));
    public static final Tier TOOL_ELEMENTAL = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1500,
            9.0F,
            3.0F,
            18,
            () -> Ingredient.of(TCItemTags.INGOTS_THAUMIUM));
    public static final Tier TOOL_PRIMAL_VOID = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            500,
            8.0F,
            4.0F,
            20,
            () -> Ingredient.of(TCItemTags.INGOTS_VOID_METAL));
    public static final Tier TOOL_CRIMSON_VOID = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            200,
            8.0F,
            3.5F,
            20,
            () -> Ingredient.of(TCItemTags.INGOTS_VOID_METAL));

    public static final int DURABILITY_THAUMIUM = 25;
    public static final int DURABILITY_ROBES = 25;
    public static final int DURABILITY_TRAVELLER = 25;
    public static final int DURABILITY_VOID = 10;
    public static final int DURABILITY_VOID_ROBE = 18;
    public static final int DURABILITY_FORTRESS = 40;
    public static final int DURABILITY_CULTIST_PLATE = 18;
    public static final int DURABILITY_CULTIST_ROBE = 17;
    public static final int DURABILITY_CULTIST_BOOTS = 15;
    public static final int DURABILITY_CULTIST_LEADER = 30;

    public static final Holder<ArmorMaterial> ARMOR_THAUMIUM = register(
            "thaumium",
            defense(2, 5, 6, 2),
            25,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0F,
            0.0F,
            () -> Ingredient.of(TCItemTags.INGOTS_THAUMIUM),
            false);
    public static final Holder<ArmorMaterial> ARMOR_GOGGLES = register(
            "goggles_revealing",
            defense(0, 0, 0, 0),
            25,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            () -> Ingredient.EMPTY,
            false);
    public static final Holder<ArmorMaterial> ARMOR_ROBES = register(
            "robes",
            defense(1, 2, 3, 1),
            25,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            1.0F,
            0.0F,
            () -> Ingredient.of(ItemTags.WOOL),
            true);
    public static final Holder<ArmorMaterial> ARMOR_TRAVELLER = register(
            "traveller",
            defense(1, 2, 3, 1),
            25,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            1.0F,
            0.0F,
            () -> Ingredient.of(Tags.Items.LEATHERS),
            false);
    public static final Holder<ArmorMaterial> ARMOR_VOID = register(
            "void",
            defense(3, 6, 8, 3),
            10,
            SoundEvents.ARMOR_EQUIP_CHAIN,
            1.0F,
            0.0F,
            () -> Ingredient.of(TCItemTags.INGOTS_VOID_METAL),
            false);
    public static final Holder<ArmorMaterial> ARMOR_VOID_ROBE = register(
            "void_robe",
            defense(4, 7, 9, 4),
            10,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            2.0F,
            0.0F,
            () -> Ingredient.of(TCItemTags.INGOTS_VOID_METAL),
            true);
    public static final Holder<ArmorMaterial> ARMOR_FORTRESS = register(
            "fortress",
            defense(3, 6, 7, 3),
            25,
            SoundEvents.ARMOR_EQUIP_IRON,
            3.0F,
            0.0F,
            () -> Ingredient.of(TCItemTags.INGOTS_THAUMIUM),
            false);
    public static final Holder<ArmorMaterial> ARMOR_CULTIST_PLATE = register(
            "cultist_plate",
            defense(2, 5, 6, 2),
            13,
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0F,
            0.0F,
            () -> Ingredient.of(Tags.Items.INGOTS_IRON),
            false);
    public static final Holder<ArmorMaterial> ARMOR_CULTIST_ROBE = register(
            "cultist_robe",
            defense(2, 4, 5, 2),
            13,
            SoundEvents.ARMOR_EQUIP_CHAIN,
            0.0F,
            0.0F,
            () -> Ingredient.of(ItemTags.WOOL),
            false);
    public static final Holder<ArmorMaterial> ARMOR_CULTIST_BOOTS = register(
            "cultist_boots",
            defense(2, 5, 6, 2),
            9,
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0F,
            0.0F,
            () -> Ingredient.of(Tags.Items.INGOTS_IRON),
            false);
    public static final Holder<ArmorMaterial> ARMOR_CULTIST_LEADER = register(
            "cultist_leader",
            defense(3, 6, 7, 3),
            20,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0F,
            0.0F,
            () -> Ingredient.of(Tags.Items.INGOTS_IRON),
            false);

    private static Holder<ArmorMaterial> register(
            String name,
            Map<ArmorItem.Type, Integer> defense,
            int enchantmentValue,
            Holder<SoundEvent> equipSound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngredient,
            boolean dyeable) {
        List<ArmorMaterial.Layer> layers = dyeable
                ? List.of(
                        new ArmorMaterial.Layer(TCIds.rl(name), "", true),
                        new ArmorMaterial.Layer(TCIds.rl(name), "_over", false))
                : List.of(new ArmorMaterial.Layer(TCIds.rl(name)));
        return ARMOR_MATERIALS.register(
                name,
                () -> new ArmorMaterial(
                        defense,
                        enchantmentValue,
                        equipSound,
                        repairIngredient,
                        layers,
                        toughness,
                        knockbackResistance));
    }

    private static Map<ArmorItem.Type, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
        return Map.of(
                ArmorItem.Type.BOOTS, boots,
                ArmorItem.Type.LEGGINGS, leggings,
                ArmorItem.Type.CHESTPLATE, chestplate,
                ArmorItem.Type.HELMET, helmet);
    }

    public static void register(IEventBus modBus) {
        ARMOR_MATERIALS.register(modBus);
    }

    private TCMaterials() {}
}
