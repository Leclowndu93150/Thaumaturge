package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCItemTags;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.common.Tags;

public final class TCMaterials {
    public static final ToolMaterial TOOL_THAUMIUM = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 7.0F, 2.5F, 22, TCItemTags.INGOTS_THAUMIUM);
    public static final ToolMaterial TOOL_VOID = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 150, 8.0F, 3.0F, 10, TCItemTags.INGOTS_VOID_METAL);
    public static final ToolMaterial TOOL_ELEMENTAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 9.0F, 3.0F, 18, TCItemTags.INGOTS_THAUMIUM);
    public static final ToolMaterial TOOL_PRIMAL_VOID = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 500, 8.0F, 4.0F, 20, TCItemTags.INGOTS_VOID_METAL);
    public static final ToolMaterial TOOL_CRIMSON_VOID = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 200, 8.0F, 3.5F, 20, TCItemTags.INGOTS_VOID_METAL);

    public static final ResourceKey<EquipmentAsset> ASSET_THAUMIUM = asset("thaumium");
    public static final ResourceKey<EquipmentAsset> ASSET_TRAVELLER = asset("traveller");
    public static final ResourceKey<EquipmentAsset> ASSET_ROBES = asset("robes");
    public static final ResourceKey<EquipmentAsset> ASSET_VOID = asset("void");
    public static final ResourceKey<EquipmentAsset> ASSET_VOID_ROBE = asset("void_robe");
    public static final ResourceKey<EquipmentAsset> ASSET_FORTRESS = asset("fortress");
    public static final ResourceKey<EquipmentAsset> ASSET_CULTIST_PLATE = asset("cultist_plate");
    public static final ResourceKey<EquipmentAsset> ASSET_ZOMBIE_PLATE = asset("zombie_plate");
    public static final ResourceKey<EquipmentAsset> ASSET_CULTIST_ROBE = asset("cultist_robe");
    public static final ResourceKey<EquipmentAsset> ASSET_CULTIST_BOOTS = asset("cultist_boots");
    public static final ResourceKey<EquipmentAsset> ASSET_CULTIST_LEADER = asset("cultist_leader");

    public static final ArmorMaterial ARMOR_THAUMIUM = new ArmorMaterial(25, defense(2, 5, 6, 2), 25, SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F, TCItemTags.INGOTS_THAUMIUM, ASSET_THAUMIUM);
    public static final ArmorMaterial ARMOR_ROBES = new ArmorMaterial(25, defense(1, 2, 3, 1), 25, SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.0F, ItemTags.WOOL, ASSET_ROBES);
    public static final ArmorMaterial ARMOR_TRAVELLER = new ArmorMaterial(25, defense(1, 2, 3, 1), 25, SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 0.0F, Tags.Items.LEATHERS, ASSET_TRAVELLER);
    public static final ArmorMaterial ARMOR_VOID = new ArmorMaterial(10, defense(3, 6, 8, 3), 10, SoundEvents.ARMOR_EQUIP_CHAIN, 1.0F, 0.0F, TCItemTags.INGOTS_VOID_METAL, ASSET_VOID);
    public static final ArmorMaterial ARMOR_VOID_ROBE = new ArmorMaterial(18, defense(4, 7, 9, 4), 10, SoundEvents.ARMOR_EQUIP_LEATHER, 2.0F, 0.0F, TCItemTags.INGOTS_VOID_METAL, ASSET_VOID_ROBE);
    public static final ArmorMaterial ARMOR_FORTRESS = new ArmorMaterial(40, defense(3, 6, 7, 3), 25, SoundEvents.ARMOR_EQUIP_IRON, 3.0F, 0.0F, TCItemTags.INGOTS_THAUMIUM, ASSET_FORTRESS);
    public static final ArmorMaterial ARMOR_CULTIST_PLATE = new ArmorMaterial(18, defense(2, 5, 6, 2), 13, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Tags.Items.INGOTS_IRON, ASSET_CULTIST_PLATE);
    public static final ArmorMaterial ARMOR_CULTIST_ROBE = new ArmorMaterial(17, defense(2, 4, 5, 2), 13, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, ItemTags.WOOL, ASSET_CULTIST_ROBE);
    public static final ArmorMaterial ARMOR_CULTIST_BOOTS = new ArmorMaterial(15, defense(2, 5, 6, 2), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, Tags.Items.INGOTS_IRON, ASSET_CULTIST_BOOTS);
    public static final ArmorMaterial ARMOR_CULTIST_LEADER = new ArmorMaterial(30, defense(3, 6, 7, 3), 20, SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 0.0F, Tags.Items.INGOTS_IRON, ASSET_CULTIST_LEADER);

    private static Map<ArmorType, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
        return Map.of(ArmorType.BOOTS, boots, ArmorType.LEGGINGS, leggings, ArmorType.CHESTPLATE, chestplate, ArmorType.HELMET, helmet);
    }

    private static ResourceKey<EquipmentAsset> asset(String path) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, TCIds.rl(path));
    }

    private TCMaterials() {}
}
