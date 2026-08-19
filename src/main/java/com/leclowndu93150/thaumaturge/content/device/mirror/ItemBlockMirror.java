package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class ItemBlockMirror extends BlockItem {
    public ItemBlockMirror(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (!(level.getBlockState(pos).getBlock() instanceof BlockMirror clicked) || player == null) {
            return InteractionResult.PASS;
        }
        if (clicked != getBlock()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityMirrorBase mirror) {
            if (mirror.isLinkValid()) {
                player.sendSystemMessage(Component.translatable("tc.mirrorlinkedalready").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                return InteractionResult.SUCCESS;
            }
            ItemStack linkedStack = stack.copyWithCount(1);
            linkedStack.set(TCDataComponents.MIRROR_LINK.get(), GlobalPos.of(level.dimension(), pos));
            level.playSound(null, pos, TCSounds.JAR.get(), SoundSource.BLOCKS, 1.0F, 2.0F);
            if (!player.getInventory().add(linkedStack)) {
                player.drop(linkedStack, false);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        GlobalPos link = stack.get(TCDataComponents.MIRROR_LINK.get());
        if (link != null) {
            tooltip.accept(Component.translatable("tc.handmirrorlinkedto.full", link.pos().getX(), link.pos().getY(), link.pos().getZ(), link.dimension().identifier().toString()));
        }
    }
}
