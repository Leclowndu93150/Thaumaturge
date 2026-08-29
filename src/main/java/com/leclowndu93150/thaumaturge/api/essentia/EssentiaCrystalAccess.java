package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Supported access to genuine, configured essentia-crystal item stacks. */
public final class EssentiaCrystalAccess {
    private EssentiaCrystalAccess() {}

    /** Creates a new configured crystal stack. The caller owns the returned stack. */
    public static ItemStack create(Holder<IAspect> aspect, int count) {
        if (count <= 0) return ItemStack.EMPTY;
        return EssentiaCrystalFactory.of(aspect, count).copy();
    }

    /** Classifies a stack without treating other aspect-bearing items as crystals. */
    public static State state(ItemStack stack) {
        if (!stack.is(TCItems.ESSENTIA_CRYSTAL.get())) return State.NOT_CRYSTAL;
        AspectInstance configured = stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
        if (configured == null) return State.UNCONFIGURED;
        return configured.amount() == 1 ? State.CONFIGURED : State.MALFORMED;
    }

    public static boolean isConfigured(ItemStack stack) {
        return state(stack) == State.CONFIGURED;
    }

    /** Returns the aspect only for a genuine, well-formed configured crystal. */
    public static @Nullable Holder<IAspect> aspect(ItemStack stack) {
        if (!isConfigured(stack)) return null;
        return stack.get(TCDataComponents.CRYSTAL_ASPECT.get()).aspect();
    }

    public enum State {
        NOT_CRYSTAL,
        UNCONFIGURED,
        MALFORMED,
        CONFIGURED
    }
}
