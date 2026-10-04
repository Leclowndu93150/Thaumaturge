package com.leclowndu93150.thaumaturge.content.research.scan;

import com.google.common.collect.Iterables;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import java.util.function.BiPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class CarriedTraits {
    private CarriedTraits() {}

    public static BiPredicate<Player, ScanTarget> effect(Holder<MobEffect> effect) {
        return (player, target) -> {
            if (target.creature() instanceof LivingEntity living) {
                return living.hasEffect(effect);
            }
            PotionContents contents = ScanningManager.stackOf(player, target).getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            return Iterables.any(contents.getAllEffects(), instance -> instance.is(effect));
        };
    }

    public static BiPredicate<Player, ScanTarget> enchantment(ResourceKey<Enchantment> enchantment) {
        return (player, target) -> EnchantmentHelper.getEnchantmentsForCrafting(ScanningManager.stackOf(player, target)).keySet().stream().anyMatch(holder -> holder.is(enchantment));
    }
}
