package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

public final class VoidSwordItem extends SwordItem {
    public VoidSwordItem(Properties properties) {
        super(TTMaterials.TOOL_VOID, GearWarp.with(properties, VoidGearItem.VOID_GEAR_WARP));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()) {
            VoidGearItem.selfRepairTick(stack, entity);
        }
    }
}
