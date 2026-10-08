package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
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
                player.sendSystemMessage(Component.translatable("message.thaumaturge.mirror.already_linked")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                return InteractionResult.SUCCESS;
            }
            ItemStack linkedStack = stack.copyWithCount(1);
            linkedStack.set(TTDataComponents.MIRROR_LINK.get(), GlobalPos.of(level.dimension(), pos));
            level.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, 1.0F, 2.0F);
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
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        GlobalPos link = stack.get(TTDataComponents.MIRROR_LINK.get());
        if (link != null) {
            tooltip.add(Component.translatable(
                    "tooltip.thaumaturge.mirror.linked_to",
                    link.pos().getX(),
                    link.pos().getY(),
                    link.pos().getZ(),
                    link.dimension().location().toString()));
        }
    }
}
