package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.wands.IWandRodOnUpdate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class WandRodPrimalOnUpdate implements IWandRodOnUpdate {
    private final @Nullable ResourceKey<IAspect> aspect;

    public WandRodPrimalOnUpdate(ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
    }

    public WandRodPrimalOnUpdate() {
        this.aspect = null;
    }

    @Override
    public void onUpdate(ItemStack wand, Player player) {
        int chargeCap = WandVisHelper.getMaxVis(wand) / WandEconomy.ROD_SELF_CHARGE_CAP_DIVISOR;
        if (aspect != null) {
            if (player.tickCount % WandEconomy.ROD_SELF_CHARGE_INTERVAL_TICKS == 0 && WandVisHelper.getVis(wand, aspect) < chargeCap) {
                WandVisHelper.addVis(wand, aspect, 1, true);
            }
        } else if (player.tickCount % WandEconomy.PRIMAL_SELF_CHARGE_INTERVAL_TICKS == 0) {
            List<ResourceKey<IAspect>> depleted = new ArrayList<>();
            for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
                if (WandVisHelper.getVis(wand, primal) < chargeCap) {
                    depleted.add(primal);
                }
            }
            if (!depleted.isEmpty()) {
                WandVisHelper.addVis(wand, depleted.get(player.getRandom().nextInt(depleted.size())), 1, true);
            }
        }
    }
}
