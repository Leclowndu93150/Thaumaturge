package com.leclowndu93150.thaumaturge.content.taint.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ItemEssentiaCrystal extends Item {
    public ItemEssentiaCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Holder<IAspect> aspect = aspectOf(stack);
        if (aspect == null) {
            return Component.translatable("item.thaumaturge.essentia_crystal.unknown");
        }
        return Component.translatable("item.thaumaturge.essentia_crystal", AspectComponents.name(aspect));
    }

    /*    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Holder<IAspect> aspect = aspectOf(stack);
        if (aspect != null) {
            int color = aspect.value().color();
            Component line = Component.translatable("aspect.thaumaturge." + aspect.value().tag() + ".desc")
                    .withStyle(style -> style.withColor(color));
            tooltip.add(line);
        }
    }*/

    public static Holder<IAspect> aspectOf(ItemStack stack) {
        AspectInstance instance = stack.get(TTDataComponents.CRYSTAL_ASPECT.get());
        return instance == null ? null : instance.aspect();
    }

    public static int colorOf(ItemStack stack) {
        Holder<IAspect> aspect = aspectOf(stack);
        return aspect == null ? 0xFFFFFF : aspect.value().color();
    }
}
