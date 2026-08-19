package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.scan.IScanThing;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ScanAspectDiscovery implements IScanThing {
    private final ResourceKey<IAspect> aspect;

    public ScanAspectDiscovery(ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        if (target == null) {
            return false;
        }
        Holder<IAspect> holder = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(aspect).orElse(null);
        if (holder == null) {
            return false;
        }
        AspectList aspects;
        if (target instanceof Entity entity && !(target instanceof ItemEntity)) {
            aspects = ScanningManager.entityAspects(entity);
        } else {
            ItemStack stack = ScanningManager.getItemFromParms(player, target);
            if (stack.isEmpty()) {
                return false;
            }
            aspects = ScanningManager.itemAspects(stack);
        }
        return aspects.amountOf(holder) > 0 && AspectPools.hasDiscoveredComponents(player, holder);
    }

    @Override
    public void onSuccess(Player player, @Nullable Object target) {}

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return ScanKeys.aspect(aspect);
    }
}
