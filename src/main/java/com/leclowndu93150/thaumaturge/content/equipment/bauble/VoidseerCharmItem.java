package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TCAttributes;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class VoidseerCharmItem extends Item implements IVisDiscountGear, IWarpingGear {
    public static final Identifier DISCOUNT_MODIFIER_ID = TCIds.rl("voidseer_discount");
    private static final int WARP_CAP = 100;
    private static final float MAX_DISCOUNT = 25.0F;
    private static final int WARP_PER_DISCOUNT = 5;

    public VoidseerCharmItem(Properties properties) {
        super(properties);
    }

    public static int discountFor(LivingEntity wearer) {
        if (!(wearer instanceof Player player)) {
            return 0;
        }
        int permanent = Math.min(WARP_CAP, WarpHelper.getWarp(player).get(WarpType.PERMANENT));
        return Mth.floor(permanent / (float) WARP_CAP * MAX_DISCOUNT);
    }

    public void wornTick(ItemStack stack, LivingEntity wearer) {
        AttributeInstance attribute = wearer.getAttribute(TCAttributes.VIS_DISCOUNT);
        if (attribute == null) {
            return;
        }
        double contribution = discountFor(wearer) / 100.0;
        AttributeModifier existing = attribute.getModifier(DISCOUNT_MODIFIER_ID);
        if (existing != null && existing.amount() == contribution) {
            return;
        }
        attribute.removeModifier(DISCOUNT_MODIFIER_ID);
        if (contribution != 0.0) {
            attribute.addTransientModifier(new AttributeModifier(DISCOUNT_MODIFIER_ID, contribution, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void clearDiscount(LivingEntity wearer) {
        AttributeInstance attribute = wearer.getAttribute(TCAttributes.VIS_DISCOUNT);
        if (attribute != null) {
            attribute.removeModifier(DISCOUNT_MODIFIER_ID);
        }
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return 0;
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return discountFor(wearer) / WARP_PER_DISCOUNT;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.thaumaturge.voidseer_charm.text").withStyle(ChatFormatting.DARK_BLUE, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
