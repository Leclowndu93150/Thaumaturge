package com.leclowndu93150.thaumaturge.content.wands;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemPrimalCharm extends Item {
    private static final int FLAVOR_LINE_COUNT = 5;
    private static final int FLAVOR_LINE_TICKS = 100;

    public ItemPrimalCharm(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> builder, TooltipFlag flag) {
        Level level = context.level();
        long time = level == null ? 0L : level.getGameTime();
        int line = (int) Math.floorMod(time / FLAVOR_LINE_TICKS, (long) FLAVOR_LINE_COUNT);
        builder.add(Component.translatable("tooltip.thaumaturge.primal_charm." + line)
                .withStyle(ChatFormatting.GOLD));
    }
}
