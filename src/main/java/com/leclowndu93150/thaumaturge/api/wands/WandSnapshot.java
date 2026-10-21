package com.leclowndu93150.thaumaturge.api.wands;

import net.minecraft.resources.ResourceKey;

/** Read-only identity of a valid wand instance; it does not authorize storage changes. */
public record WandSnapshot(ResourceKey<WandRod> rod, ResourceKey<WandCap> cap, Variant variant) {
    public enum Variant {
        WAND, STAFF, SCEPTRE
    }
}
