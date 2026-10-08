package com.leclowndu93150.thaumaturge.content.spell.manipulator;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record InscriptionCheck(
        boolean focused,
        boolean busy,
        boolean valid,
        int missingLevels,
        Map<ResourceKey<IAspect>, Integer> missingCrystals) {
    public static InscriptionCheck of(
            Player player, boolean focused, boolean busy, SpellSummary summary, AspectList cost) {
        if (player.getAbilities().instabuild) {
            return new InscriptionCheck(focused, busy, summary.valid(), 0, Map.of());
        }
        Map<ResourceKey<IAspect>, Integer> missing = new LinkedHashMap<>();
        for (AspectInstance instance : cost.entries()) {
            int lacking = instance.amount()
                    - carried(player, EssentiaCrystalFactory.of(instance.aspect(), instance.amount()));
            if (lacking > 0) {
                instance.aspect().unwrapKey().ifPresent(key -> missing.put(key, lacking));
            }
        }
        return new InscriptionCheck(
                focused, busy, summary.valid(), Math.max(0, summary.xp() - player.experienceLevel), missing);
    }

    public boolean ready() {
        return focused && !busy && valid && missingLevels == 0 && missingCrystals.isEmpty();
    }

    private static int carried(Player player, ItemStack wanted) {
        int found = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize() && found < wanted.getCount(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                found += stack.getCount();
            }
        }
        return found;
    }
}
