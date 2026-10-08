package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VoidGearItem extends ArmorItem {
    private static final int REPAIR_INTERVAL_TICKS = 20;
    static final int VOID_GEAR_WARP = 1;

    public VoidGearItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Properties properties) {
        super(material, type, GearWarp.with(properties, VOID_GEAR_WARP));
    }

    static void selfRepairTick(ItemStack stack, Entity entity) {
        if (entity instanceof LivingEntity && stack.isDamaged() && entity.tickCount % REPAIR_INTERVAL_TICKS == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()) {
            selfRepairTick(stack, entity);
        }
    }
}
