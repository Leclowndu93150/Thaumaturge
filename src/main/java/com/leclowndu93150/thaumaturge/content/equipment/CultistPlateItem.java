package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityInhabitedZombie;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public final class CultistPlateItem extends ArmorItem {
    private static final ResourceLocation ZOMBIE_PLATE_OUTER =
            TTIds.rl("textures/models/armor/zombie_plate_layer_1.png");
    private static final ResourceLocation ZOMBIE_PLATE_INNER =
            TTIds.rl("textures/models/armor/zombie_plate_layer_2.png");

    public CultistPlateItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ResourceLocation getArmorTexture(
            ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        if (entity instanceof EntityInhabitedZombie) {
            return innerModel ? ZOMBIE_PLATE_INNER : ZOMBIE_PLATE_OUTER;
        }
        return null;
    }
}
