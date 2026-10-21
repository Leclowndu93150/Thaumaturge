package com.leclowndu93150.thaumaturge.api.golems;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.context.UseOnContext;

/** Compile-only delegation preserving the clicked face, actor and actual hand-slot payment. */
final class SealPlacementExample {
    private SealPlacementExample() {}

    static boolean place(UseOnContext context, Identifier registeredSealType) {
        return GolemHelper.tryPlaceSeal(context, registeredSealType);
    }
}
