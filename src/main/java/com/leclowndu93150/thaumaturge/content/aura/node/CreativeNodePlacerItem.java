package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

public final class CreativeNodePlacerItem extends Item {
    public CreativeNodePlacerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (!level.getBlockState(pos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        CustomData nodeData = context.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
        if (nodeData != null) {
            BlockEntityNode copy = new BlockEntityNode(pos, TCBlocks.NODE.get().defaultBlockState());
            if (!nodeData.loadInto(copy, level.registryAccess())) {
                return InteractionResult.FAIL;
            }
            if (!level.setBlock(pos, TCBlocks.NODE.get().defaultBlockState(), 3)
                    || !(level.getBlockEntity(pos) instanceof BlockEntityNode node)
                    || !nodeData.loadInto(node, level.registryAccess())) {
                return InteractionResult.FAIL;
            }
            node.setNodeType(node.getNodeType());
            node.setChanged();
            level.sendBlockUpdated(pos, node.getBlockState(), node.getBlockState(), 3);
            return InteractionResult.CONSUME;
        }
        boolean placed = NodeGenerator.createRandomNodeAt(
                level,
                pos,
                level.getRandom(),
                false,
                false,
                false,
                NodeGenerator.DEFAULT_SPECIAL_RARITY,
                NodeGenerator.DEFAULT_BASE_AURA);
        return placed ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumaturge.creative_only").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
