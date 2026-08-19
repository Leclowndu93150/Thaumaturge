package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IGoggles;
import com.leclowndu93150.thaumaturge.api.items.IRevealer;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class FortressArmorItem extends Item implements IGoggles, IRevealer {
    public static final int NO_MASK = -1;

    public FortressArmorItem(Properties properties) {
        super(properties);
    }

    public static boolean hasGoggles(ItemStack stack) {
        return stack.has(TCDataComponents.GOGGLES_UPGRADE.get());
    }

    public static int mask(ItemStack stack) {
        Integer mask = stack.get(TCDataComponents.FORTRESS_MASK.get());
        return mask == null ? NO_MASK : mask;
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity wearer) {
        return hasGoggles(stack);
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity wearer) {
        return hasGoggles(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (hasGoggles(stack)) {
            tooltip.accept(Component.translatable("item.thaumaturge.goggles_revealing").withStyle(ChatFormatting.DARK_PURPLE));
        }
        int mask = mask(stack);
        if (mask != NO_MASK) {
            tooltip.accept(Component.translatable("item.thaumaturge.fortress_helm.mask." + mask).withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
