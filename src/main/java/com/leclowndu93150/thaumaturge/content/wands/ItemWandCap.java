package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> builder, TooltipFlag flag) {
        Component discount = WandTooltips.capCostSummary(context.registries(), cap.get());
        if (discount != null) {
            builder.add(discount);
        }
    }
}
