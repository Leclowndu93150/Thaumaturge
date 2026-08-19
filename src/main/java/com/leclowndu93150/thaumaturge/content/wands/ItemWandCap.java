package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class ItemWandCap extends Item {
    private final Supplier<WandCap> cap;

    public ItemWandCap(Item.Properties properties, Supplier<WandCap> cap) {
        super(properties);
        this.cap = cap;
    }

    public WandCap cap() {
        return cap.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(WandTooltips.capCostSummary(context.registries(), cap.get()));
    }
}
