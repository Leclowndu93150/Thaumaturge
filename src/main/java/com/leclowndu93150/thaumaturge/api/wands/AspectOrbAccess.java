package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Server-thread access to loaded aspect orbs, without exposing their implementation type. */
public final class AspectOrbAccess {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("AspectOrbAccess");

    private AspectOrbAccess() {}

    /** Returns an immutable source snapshot; unknown entities and off-thread reads are rejected. */
    public static Optional<AspectOrbSnapshot> inspect(Entity entity, ServerPlayer player) {
        return BINDING.get().inspect(entity, player);
    }

    /**
     * Attempts collection within three blocks into the first eligible hotbar wand.
     * Returns credited centivis, or zero without consuming the orb. Pickup is all-or-nothing:
     * the complete modified credit must fit, so partial room never destroys source vis and
     * modifiers require no partial-consumption rounding. Custom storage must support transactions.
     */
    public static int collect(Entity entity, ServerPlayer player) {
        return BINDING.get().collect(entity, player);
    }

    /** Binds the implementation once during mod initialization; addons must not call this. */
    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    /** Implementation hook owned by Thaumaturge. */
    public interface Bindings {
        Optional<AspectOrbSnapshot> inspect(Entity entity, ServerPlayer player);

        int collect(Entity entity, ServerPlayer player);
    }
}
