package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class VoidHoeItem extends HoeItem implements IWarpingGear {
    public VoidHoeItem(Properties properties) {
        super(TTMaterials.TOOL_VOID, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()) {
            VoidGearItem.selfRepairTick(stack, entity);
        }
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return VoidGearItem.VOID_GEAR_WARP;
    }
}
