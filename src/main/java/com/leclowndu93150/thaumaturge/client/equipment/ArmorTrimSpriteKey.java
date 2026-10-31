package com.leclowndu93150.thaumaturge.client.equipment;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;

public record ArmorTrimSpriteKey(ArmorTrim trim, Holder<ArmorMaterial> armorMaterial) {}
