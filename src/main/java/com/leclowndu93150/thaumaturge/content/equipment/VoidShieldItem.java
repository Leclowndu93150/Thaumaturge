package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.content.equipment.shield.ThaumicShieldItem;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class VoidShieldItem extends ThaumicShieldItem {
    public VoidShieldItem(Properties properties) {
        super(
                GearWarp.with(properties, VoidGearItem.VOID_GEAR_WARP),
                TTItemTags.INGOTS_VOID_METAL,
                TTMaterials.TOOL_VOID.getEnchantmentValue());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()) {
            VoidGearItem.selfRepairTick(stack, entity);
        }
    }
}
