package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.api.items.IRechargable;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class VerdantCharmItem extends Item implements IRechargable {
    public static final int TYPE_BASE = 0;
    public static final int TYPE_LIFE = 1;
    public static final int TYPE_SUSTAIN = 2;
    private static final int MAX_CHARGE = 200;
    private static final int TICK_INTERVAL = 20;
    private static final int WITHER_COST = 20;
    private static final int POISON_COST = 10;
    private static final int FLUX_TAINT_COST = 5;
    private static final int HEAL_COST = 5;
    private static final int SUSTAIN_COST = 1;
    private static final int LOW_AIR = 100;
    private static final int RESTORED_AIR = 300;
    private static final int FOOD_AMOUNT = 1;
    private static final float FOOD_SATURATION = 0.3F;

    public VerdantCharmItem(Properties properties) {
        super(properties);
    }

    public static int type(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.VERDANT_TYPE.get(), TYPE_BASE);
    }

    public void wornTick(ItemStack stack, LivingEntity wearer) {
        if (wearer.level().isClientSide() || wearer.tickCount % TICK_INTERVAL != 0 || !(wearer instanceof Player player)) {
            return;
        }
        if (player.getEffect(MobEffects.WITHER) != null && RechargeAccess.consumeCharge(stack, player, WITHER_COST)) {
            player.removeEffect(MobEffects.WITHER);
            return;
        }
        if (player.getEffect(MobEffects.POISON) != null && RechargeAccess.consumeCharge(stack, player, POISON_COST)) {
            player.removeEffect(MobEffects.POISON);
            return;
        }
        if (player.getEffect(TCMobEffects.FLUX_TAINT) != null && RechargeAccess.consumeCharge(stack, player, FLUX_TAINT_COST)) {
            player.removeEffect(TCMobEffects.FLUX_TAINT);
            return;
        }
        int type = type(stack);
        if (type == TYPE_LIFE && player.getHealth() < player.getMaxHealth() && RechargeAccess.consumeCharge(stack, player, HEAL_COST)) {
            player.heal(1.0F);
            return;
        }
        if (type == TYPE_SUSTAIN) {
            if (player.getAirSupply() < LOW_AIR && RechargeAccess.consumeCharge(stack, player, SUSTAIN_COST)) {
                player.setAirSupply(RESTORED_AIR);
                return;
            }
            if (player.canEat(false) && RechargeAccess.consumeCharge(stack, player, SUSTAIN_COST)) {
                player.getFoodData().eat(FOOD_AMOUNT, FOOD_SATURATION);
            }
        }
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return MAX_CHARGE;
    }

    @Override
    public ChargeDisplay showInHud(ItemStack stack, LivingEntity holder) {
        return ChargeDisplay.NORMAL;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        int type = type(stack);
        if (type == TYPE_LIFE) {
            tooltip.accept(Component.translatable("item.thaumaturge.verdant_charm.life.text").withStyle(ChatFormatting.GOLD));
        } else if (type == TYPE_SUSTAIN) {
            tooltip.accept(Component.translatable("item.thaumaturge.verdant_charm.sustain.text").withStyle(ChatFormatting.GOLD));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
