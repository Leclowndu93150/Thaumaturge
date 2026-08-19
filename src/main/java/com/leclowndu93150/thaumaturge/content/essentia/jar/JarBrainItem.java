package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public final class JarBrainItem extends BlockItem {
    public JarBrainItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        Integer xp = stack.get(TCDataComponents.STORED_XP.get());
        if (xp != null && xp > 0) {
            tooltip.accept(Component.literal(xp + " xp").withStyle(ChatFormatting.GREEN));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
