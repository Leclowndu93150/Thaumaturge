package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.research.scan.IScanThing;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jspecify.annotations.Nullable;

public final class ScanEnchantment implements IScanThing {
    private final Identifier enchantment;

    public ScanEnchantment(Identifier enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        ItemStack stack = ScanningManager.getItemFromParms(player, target);
        if (stack.isEmpty()) {
            return false;
        }
        for (Holder<Enchantment> holder : EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet()) {
            if (holder.unwrapKey().map(key -> key.identifier().equals(enchantment)).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return ScanKeys.enchantment(enchantment);
    }
}
