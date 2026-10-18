package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class VoidAxeItem extends AxeItem {
    public VoidAxeItem(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Properties properties) {
        super(material, attackDamageBaseline, attackSpeedBaseline, GearWarp.with(properties, VoidGearItem.VOID_GEAR_WARP));
    }

    @Override
    public void inventoryTick(
            @NonNull ItemStack stack,
            @NonNull ServerLevel level,
            @NonNull Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        VoidGearItem.selfRepairTick(stack, entity);
    }
}
