package com.leclowndu93150.thaumaturge.content.equipment;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CrimsonBladeItem extends SwordItem {
    private static final int REPAIR_INTERVAL_TICKS = 20;
    private static final int CRIMSON_BLADE_WARP = 2;
    private static final int WEAKNESS_TICKS = 60;
    private static final int HUNGER_TICKS = 120;

    public CrimsonBladeItem(Properties properties) {
        super(TTMaterials.TOOL_CRIMSON_VOID, GearWarp.with(properties, CRIMSON_BLADE_WARP));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()
                && entity instanceof LivingEntity
                && stack.isDamaged()
                && entity.tickCount % REPAIR_INTERVAL_TICKS == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.level() instanceof ServerLevel server
                && (!(target instanceof Player)
                        || !(attacker instanceof Player)
                        || server.getServer().isPvpAllowed())) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS));
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, HUNGER_TICKS));
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumaturge.crimson_blade.greater_sapping")
                .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
