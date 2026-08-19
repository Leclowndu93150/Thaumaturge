package com.leclowndu93150.thaumaturge.content.equipment;

import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;

public final class PrimalCrusherItem extends VoidGearItem {
    private static final int CRUSHER_WARP = 2;

    public PrimalCrusherItem(Properties properties) {
        super(properties.component(DataComponents.TOOL, crusherTool()));
    }

    private static Tool crusherTool() {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return new Tool(List.of(Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.MINEABLE_WITH_PICKAXE), TCMaterials.TOOL_PRIMAL_VOID.speed()),
                Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.MINEABLE_WITH_SHOVEL), TCMaterials.TOOL_PRIMAL_VOID.speed())), 1.0F, 1, true);
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return CRUSHER_WARP;
    }
}
