package com.leclowndu93150.thaumaturge.api.entity.trait;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Static facade over the mob trait engine. Adding or removing a trait applies or removes its
 * attribute modifiers and AI goals immediately, and the change is synced to watching clients.
 *
 * <p>Mutators are server side and ignore client entities. Queries work on both sides, because
 * the trait list is synced.
 *
 * @since 1.0.0
 */
public final class MobTraits {
    /** The taint infection trait: hostile AI for passive mobs, taint immunity and the taint overlay. */
    public static final ResourceKey<MobTrait> TAINTED =
            ResourceKey.create(MobTrait.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("thaumaturge", "tainted"));

    private static Bindings impl;

    private MobTraits() {}

    /**
     * Adds a trait to a mob. Adding a trait the mob already carries does nothing.
     *
     * @param mob   the carrier
     * @param trait the trait to add
     * @return {@code true} when the trait was added
     */
    public static boolean add(LivingEntity mob, Holder<MobTrait> trait) {
        return bindingOrThrow().add(mob, trait);
    }

    /**
     * Removes a trait from a mob and undoes its modifiers and goals.
     *
     * @param mob   the carrier
     * @param trait the trait to remove
     * @return {@code true} when the trait was present
     */
    public static boolean remove(LivingEntity mob, Holder<MobTrait> trait) {
        return bindingOrThrow().remove(mob, trait);
    }

    /**
     * Returns the traits a mob carries, in the order they were added.
     *
     * @param mob the mob
     * @return the traits, never null
     */
    public static List<Holder<MobTrait>> traits(LivingEntity mob) {
        return bindingOrThrow().traits(mob);
    }

    /**
     * Returns whether a mob carries the given trait.
     *
     * @param mob   the mob
     * @param trait the trait key
     * @return {@code true} when present
     */
    public static boolean has(LivingEntity mob, ResourceKey<MobTrait> trait) {
        for (Holder<MobTrait> held : traits(mob)) {
            if (held.is(trait)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the first champion trait a mob carries.
     *
     * @param mob the mob
     * @return the champion trait, or empty when the mob is not a champion
     */
    public static Optional<Holder<MobTrait>> champion(LivingEntity mob) {
        for (Holder<MobTrait> held : traits(mob)) {
            if (held.value().isChampion()) {
                return Optional.of(held);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns whether a mob is a champion, meaning it carries at least one champion trait.
     *
     * @param mob the mob
     * @return {@code true} for champions
     */
    public static boolean isChampion(LivingEntity mob) {
        return champion(mob).isPresent();
    }

    /**
     * Returns whether a mob belongs to the taint: either a native taint creature implementing
     * {@link ITaintedMob} or a mob carrying a trait whose {@link MobTrait#isTaint()} is true.
     *
     * @param mob the mob
     * @return {@code true} for tainted mobs
     */
    public static boolean isTainted(LivingEntity mob) {
        if (mob instanceof ITaintedMob) {
            return true;
        }
        for (Holder<MobTrait> held : traits(mob)) {
            if (held.value().isTaint()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Installs the engine implementation. Called once during mod construction.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when called twice
     */
    public static void bind(Bindings bindings) {
        if (impl != null) {
            throw new IllegalStateException("MobTraits already bound");
        }
        impl = bindings;
    }

    private static Bindings bindingOrThrow() {
        if (impl == null) {
            throw new IllegalStateException("MobTraits used before the mod bound it");
        }
        return impl;
    }

    /**
     * Engine hooks behind the facade.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * @param mob   the carrier
         * @param trait the trait to add
         * @return {@code true} when the trait was added
         */
        boolean add(LivingEntity mob, Holder<MobTrait> trait);

        /**
         * @param mob   the carrier
         * @param trait the trait to remove
         * @return {@code true} when the trait was present
         */
        boolean remove(LivingEntity mob, Holder<MobTrait> trait);

        /**
         * @param mob the mob
         * @return the traits it carries
         */
        List<Holder<MobTrait>> traits(LivingEntity mob);
    }
}
