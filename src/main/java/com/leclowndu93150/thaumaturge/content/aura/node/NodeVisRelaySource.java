package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;

public record NodeVisRelaySource(BlockEntityNode node) implements IVisRelaySource {

    @Override
    public boolean canSupply() {
        return node.isEnergized();
    }

    @Override
    public int availableCentivis(ResourceKey<IAspect> primal) {
        Holder<IAspect> aspect = Aspects.resolve(node.getLevel(), primal);
        return aspect == null ? 0 : node.availableCentivis(aspect);
    }

    @Override
    public int drainCentivis(ResourceKey<IAspect> primal, int amount, boolean simulate) {
        Holder<IAspect> aspect = Aspects.resolve(node.getLevel(), primal);
        if (aspect == null) {
            return 0;
        }
        return simulate ? Math.min(amount, node.availableCentivis(aspect)) : node.drainCentivis(aspect, amount);
    }
}
