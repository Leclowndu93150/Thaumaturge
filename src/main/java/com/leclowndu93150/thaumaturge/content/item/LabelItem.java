package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.blocks.ILabelable;
import com.leclowndu93150.thaumaturge.api.items.ILabel;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class LabelItem extends Item implements ILabel {
    public LabelItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack withAspect(Holder<IAspect> aspect) {
        return withAspect(aspect.unwrapKey().orElseThrow());
    }

    public static ItemStack withAspect(ResourceKey<IAspect> aspect) {
        ItemStack stack = new ItemStack(TCItems.LABEL.get());
        stack.set(TCDataComponents.ASPECT_FILTER, aspect);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof ILabelable labelable) {
            if (labelable.applyLabel(player, pos, side, stack)) {
                if (!player.getAbilities().instabuild)
                    stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ILabelable labelable) {
            if (labelable.applyLabel(player, pos, side, stack)) {
                if (!player.getAbilities().instabuild)
                    stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable ResourceKey<IAspect> getFilteredAspect(ItemStack stack) {
        return stack.has(TCDataComponents.ASPECT_FILTER.get()) ? stack.get(TCDataComponents.ASPECT_FILTER.get()) : null;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.has(TCDataComponents.ASPECT_FILTER.get())) {
            return Component.translatable("item.thaumaturge.marked_label");
        }
        return super.getName(stack);
    }
}
