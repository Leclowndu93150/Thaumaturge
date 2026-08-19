package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class ItemWandRod extends Item {
    private final Supplier<WandRod> rod;

    public ItemWandRod(Item.Properties properties, Supplier<WandRod> rod) {
        super(properties);
        this.rod = rod;
    }

    public WandRod rod() {
        return rod.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.thaumaturge.wand.capacity", rod.get().capacity()).withStyle(ChatFormatting.GOLD));
    }
}
