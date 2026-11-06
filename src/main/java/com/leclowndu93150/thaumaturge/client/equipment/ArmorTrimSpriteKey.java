package com.leclowndu93150.thaumaturge.client.equipment;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public record ArmorTrimSpriteKey(ArmorTrim trim, ResourceKey<EquipmentAsset> equipmentAsset) {
}
