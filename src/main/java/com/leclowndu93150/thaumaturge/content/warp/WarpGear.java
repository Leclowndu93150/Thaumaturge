package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.warp.ItemWarp;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class WarpGear {
    private static final List<EquipmentSlot> WORN_SLOTS = List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.BODY);
    private static final int NO_STACK_WARP = 0;

    private WarpGear() {}

    public static int stackWarp(ItemStack stack, LivingEntity wearer) {
        if (stack.isEmpty()) {
            return 0;
        }
        int warp = stack.getOrDefault(TTDataComponents.STACK_WARP, NO_STACK_WARP);
        if (stack.getItem() instanceof IWarpingGear gear) {
            warp += gear.warp(stack, wearer);
        }
        return warp + craftingWarp(stack);
    }

    public static int craftingWarp(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemWarp mapped = stack.typeHolder().getData(TTDataMaps.ITEM_WARP);
        return mapped == null ? 0 : mapped.amount();
    }

    public static int worn(LivingEntity wearer) {
        int warp = 0;
        for (EquipmentSlot slot : WORN_SLOTS) {
            warp += stackWarp(wearer.getItemBySlot(slot), wearer);
        }
        if (ModList.get().isLoaded(TTIds.CURIOS)) {
            for (ItemStack curio : ThaumaturgeCuriosCompat.equippedCurios(wearer)) {
                warp += stackWarp(curio, wearer);
            }
        }
        return warp;
    }
}
