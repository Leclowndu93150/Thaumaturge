package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IGoggles;
import com.leclowndu93150.thaumaturge.api.items.IRevealer;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class FortressArmorItem extends ArmorItem implements IGoggles, IRevealer {
    public static final int NO_MASK = -1;

    public FortressArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    public static boolean hasGoggles(ItemStack stack) {
        return stack.has(TTDataComponents.GOGGLES_UPGRADE.get());
    }

    public static int mask(ItemStack stack) {
        Integer mask = stack.get(TTDataComponents.FORTRESS_MASK.get());
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
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (hasGoggles(stack)) {
            tooltip.add(
                    Component.translatable("item.thaumaturge.goggles_revealing").withStyle(ChatFormatting.DARK_PURPLE));
        }
        int mask = mask(stack);
        if (mask != NO_MASK) {
            tooltip.add(Component.translatable("item.thaumaturge.fortress_helm.mask." + mask)
                    .withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
