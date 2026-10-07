package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntityType;

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
        TypedEntityData<BlockEntityType<?>> nodeData = context.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
        if (nodeData != null) {
            if (nodeData.type() != TTBlockEntities.NODE.get() || !level.setBlock(pos, TTBlocks.NODE.get().defaultBlockState(), 3) || !(level.getBlockEntity(pos) instanceof BlockEntityNode node)) {
                return InteractionResult.FAIL;
            }
            nodeData.loadInto(node, level.registryAccess());
            node.applyComponentsFromItemStack(context.getItemInHand());
            node.setChanged();
            level.sendBlockUpdated(pos, node.getBlockState(), node.getBlockState(), 3);
            return InteractionResult.SUCCESS_SERVER;
        }
        boolean placed = NodeGenerator.createRandomNodeAt(level, pos, level.getRandom(), false, false, false, NodeGenerator.DEFAULT_SPECIAL_RARITY, NodeGenerator.DEFAULT_BASE_AURA);
        return placed ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.thaumaturge.creative_only").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
