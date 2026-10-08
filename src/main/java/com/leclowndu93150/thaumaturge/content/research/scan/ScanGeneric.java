package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.scan.IScannable;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ScanGeneric implements IScannable {
    @Override
    public boolean matches(Player player, ScanTarget target) {
        return !ScanningManager.aspectsOf(player, target).isEmpty();
    }

    @Override
    public @Nullable ResourceLocation research(Player player, ScanTarget target) {
        Entity creature = target.creature();
        if (creature != null) {
            return ScanKeys.entity(creature.getType());
        }
        ItemStack stack = ScanningManager.stackOf(player, target);
        return stack.isEmpty() ? null : ScanKeys.item(stack.getItem());
    }

    @Override
    public boolean rescannable(Player player, ScanTarget target) {
        return aspects(player, target).anyMatch(aspect -> !AspectPools.isDiscovered(player, aspect));
    }

    @Override
    public @Nullable Component refusal(Player player, ScanTarget target) {
        return aspects(player, target)
                .filter(aspect -> !AspectPools.hasDiscoveredComponents(player, aspect))
                .flatMap(aspect -> aspect.value().components().stream())
                .filter(component -> !AspectPools.isDiscovered(player, component))
                .findFirst()
                .map(component -> AspectPools.missingComponentMessage(player, component))
                .orElse(null);
    }

    @Override
    public void onScanned(Player player, ScanTarget target) {
        AspectList aspects = ScanningManager.aspectsOf(player, target);
        if (player instanceof ServerPlayer serverPlayer && !aspects.isEmpty()) {
            AspectPools.grantAll(serverPlayer, aspects);
        }
    }

    private static Stream<Holder<IAspect>> aspects(Player player, ScanTarget target) {
        return ScanningManager.aspectsOf(player, target).entries().stream().map(AspectInstance::aspect);
    }
}
