package com.leclowndu93150.thaumaturge.content.taint.entity.ability;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityBottleTaint;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

final class TaintBrewing {
    private static final float TAINT_BOTTLE_CHANCE = 0.15F;
    private static final int EXTRA_AMPLIFIER = 1;

    private TaintBrewing() {}

    static boolean replacesThrow(ServerLevel level, ThrownPotion potion, LivingEntity witch) {
        if (!ThaumaturgeCommonConfig.WUSS_MODE.get() && level.getRandom().nextFloat() < TAINT_BOTTLE_CHANCE) {
            EntityBottleTaint bottle = new EntityBottleTaint(level, witch, new ItemStack(TTItems.BOTTLE_TAINT.get()));
            bottle.setPos(potion.getX(), potion.getY(), potion.getZ());
            bottle.setDeltaMovement(potion.getDeltaMovement());
            level.addFreshEntity(bottle);
            return true;
        }
        potion.setItem(empowered(potion.getItem()));
        return false;
    }

    private static ItemStack empowered(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return stack;
        }
        List<MobEffectInstance> stronger = new ArrayList<>();
        for (MobEffectInstance effect : contents.getAllEffects()) {
            stronger.add(new MobEffectInstance(
                    effect.getEffect(),
                    effect.getDuration(),
                    effect.getAmplifier() + EXTRA_AMPLIFIER,
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon()));
        }
        if (stronger.isEmpty()) {
            return stack;
        }
        ItemStack copy = stack.copy();
        copy.set(
                DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.of(contents.getColor()), stronger));
        return copy;
    }
}
