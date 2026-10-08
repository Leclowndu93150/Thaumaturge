package com.leclowndu93150.thaumaturge.content.equipment;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;

public final class PrimalCrusherItem extends Item {
    private static final int CRUSHER_WARP = 2;

    public PrimalCrusherItem(Properties properties) {
        super(GearWarp.with(properties.component(DataComponents.TOOL, crusherTool()), CRUSHER_WARP));
    }

    private static Tool crusherTool() {
        return new Tool(
                List.of(
                        Tool.Rule.deniesDrops(TTMaterials.TOOL_PRIMAL_VOID.getIncorrectBlocksForDrops()),
                        Tool.Rule.minesAndDrops(
                                BlockTags.MINEABLE_WITH_PICKAXE, TTMaterials.TOOL_PRIMAL_VOID.getSpeed()),
                        Tool.Rule.minesAndDrops(
                                BlockTags.MINEABLE_WITH_SHOVEL, TTMaterials.TOOL_PRIMAL_VOID.getSpeed())),
                1.0F,
                1);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return TTMaterials.TOOL_PRIMAL_VOID.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return TTMaterials.TOOL_PRIMAL_VOID.getRepairIngredient().test(repairCandidate);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()) {
            VoidGearItem.selfRepairTick(stack, entity);
        }
    }
}
