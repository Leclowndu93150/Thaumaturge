package com.leclowndu93150.thaumaturge.api.rifts;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/** Server-defined vessel limits and raw centivis costs; an empty cost map explicitly permits free capture. */
public record RiftCapturePolicy(int maximumSize, Optional<Identifier> research, Map<ResourceKey<IAspect>, Integer> costs) {
    public RiftCapturePolicy {
        research = java.util.Objects.requireNonNull(research);
        costs = Map.copyOf(costs);
        if (maximumSize < 1 || maximumSize > 100 || costs.size() > 6 || costs.values().stream().anyMatch(amount -> amount < 0 || amount > 1_000_000)) {
            throw new IllegalArgumentException("Invalid rift capture policy");
        }
    }
}
