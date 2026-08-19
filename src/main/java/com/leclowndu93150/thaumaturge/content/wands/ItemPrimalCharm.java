package com.leclowndu93150.thaumaturge.content.wands;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class ItemPrimalCharm extends Item {
    private static final int FLAVOR_LINE_COUNT = 5;

    public ItemPrimalCharm(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        int line = Math.floorMod(System.identityHashCode(stack), FLAVOR_LINE_COUNT);
        builder.accept(Component.translatable("tooltip.thaumaturge.primal_charm." + line).withStyle(ChatFormatting.GOLD));
    }
}
