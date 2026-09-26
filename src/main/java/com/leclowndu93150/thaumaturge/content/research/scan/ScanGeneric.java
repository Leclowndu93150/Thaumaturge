package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.scan.IScanThing;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ScanGeneric implements IScanThing {
    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        return !aspectsOf(player, target).isEmpty();
    }

    @Override
    public @Nullable Component scanFailure(Player player, @Nullable Object target) {
        for (AspectInstance instance : aspectsOf(player, target).entries()) {
            if (AspectPools.hasDiscoveredComponents(player, instance.aspect())) {
                continue;
            }
            for (Holder<IAspect> component : instance.aspect().value().components()) {
                if (!AspectPools.isDiscovered(player, component)) {
                    return AspectPools.missingComponentMessage(player, component);
                }
            }
        }
        return null;
    }

    @Override
    public void onSuccess(Player player, @Nullable Object target) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AspectList aspects = aspectsOf(player, target);
        if (aspects.isEmpty()) {
            return;
        }
        AspectPools.grantAll(serverPlayer, aspects);
    }

    @Override
    public boolean canScanAfterResearchKnown(Player player, @Nullable Object target) {
        for (AspectInstance instance : aspectsOf(player, target).entries()) {
            if (!AspectPools.isDiscovered(player, instance.aspect())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @Nullable ResourceLocation getResearchKey(Player player, @Nullable Object target) {
        if (target instanceof Entity entity && !(target instanceof ItemEntity)) {
            return ScanKeys.entity(entity.getType());
        }
        ItemStack stack = ScanningManager.getItemFromParms(player, target);
        return stack.isEmpty() ? null : ScanKeys.item(stack.getItem());
    }

    private static AspectList aspectsOf(Player player, @Nullable Object target) {
        if (target == null) {
            return AspectList.EMPTY;
        }
        if (target instanceof Entity entity && !(target instanceof ItemEntity)) {
            return EntityAspects.of(entity);
        }
        ItemStack stack = ScanningManager.getItemFromParms(player, target);
        return stack.isEmpty() ? AspectList.EMPTY : AspectIndexAccess.index().of(stack);
    }
}
