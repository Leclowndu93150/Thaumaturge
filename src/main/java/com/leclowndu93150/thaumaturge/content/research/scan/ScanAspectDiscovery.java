package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.scan.IScannable;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedSky;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public final class ScanAspectDiscovery implements IScannable {
    private final ResourceKey<IAspect> aspect;

    public ScanAspectDiscovery(ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
    }

    @Override
    public boolean matches(Player player, ScanTarget target) {
        if (target instanceof ScannedSky) {
            return false;
        }
        Holder<IAspect> holder = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(aspect).orElse(null);
        if (holder == null) {
            return false;
        }
        return ScanningManager.aspectsOf(player, target).amountOf(holder) > 0 && AspectPools.hasDiscoveredComponents(player, holder);
    }

    @Override
    public Identifier research(Player player, ScanTarget target) {
        return ScanKeys.aspect(aspect);
    }
}
