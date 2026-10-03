package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;

public final class PrimalCrusherItem extends Item implements IWarpingGear {
    private static final int CRUSHER_WARP = 2;

    public PrimalCrusherItem(Properties properties) {
        super(properties.component(DataComponents.TOOL, crusherTool()));
    }

    private static Tool crusherTool() {
        return new Tool(
                List.of(
                        Tool.Rule.deniesDrops(TCMaterials.TOOL_PRIMAL_VOID.getIncorrectBlocksForDrops()),
                        Tool.Rule.minesAndDrops(
                                BlockTags.MINEABLE_WITH_PICKAXE, TCMaterials.TOOL_PRIMAL_VOID.getSpeed()),
                        Tool.Rule.minesAndDrops(
                                BlockTags.MINEABLE_WITH_SHOVEL, TCMaterials.TOOL_PRIMAL_VOID.getSpeed())),
                1.0F,
                1);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return TCMaterials.TOOL_PRIMAL_VOID.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return TCMaterials.TOOL_PRIMAL_VOID.getRepairIngredient().test(repairCandidate);
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
        return CRUSHER_WARP;
    }
}
