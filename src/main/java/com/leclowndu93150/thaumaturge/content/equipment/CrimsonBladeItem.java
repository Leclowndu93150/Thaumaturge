package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IWarpingGear;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

public class CrimsonBladeItem extends Item implements IWarpingGear {
    private static final int REPAIR_INTERVAL_TICKS = 20;
    private static final int CRIMSON_BLADE_WARP = 2;
    private static final int WEAKNESS_TICKS = 60;
    private static final int HUNGER_TICKS = 120;

    public CrimsonBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (entity instanceof LivingEntity && stack.isDamaged() && entity.tickCount % REPAIR_INTERVAL_TICKS == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.level() instanceof ServerLevel server && (!(target instanceof Player) || !(attacker instanceof Player) || server.isPvpAllowed())) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS));
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, HUNGER_TICKS));
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("enchantment.special.sapgreat").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public int getWarp(ItemStack stack, LivingEntity wearer) {
        return CRIMSON_BLADE_WARP;
    }
}
