package com.leclowndu93150.thaumaturge.api.entity.trait;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jspecify.annotations.Nullable;

/**
 * A behaviour package that can be attached to any living entity at runtime. Champion modifiers
 * and taint are both mob traits.
 *
 * <p>A trait contributes up to four things while it is present on a mob: attribute modifiers,
 * AI goals, scan aspects and event hooks. The trait engine owns the lifecycle. It adds the
 * declared attribute modifiers as permanent modifiers under engine-assigned ids, injects goals
 * into the mob's goal selectors, and removes both again when the trait goes away. Goals are not
 * saved with the entity, so the engine re-injects them every time the mob joins a level.
 *
 * <p>Traits live in the {@link #REGISTRY_KEY} registry, so addons may contribute their own.
 * Every hook runs on the logical server unless stated otherwise.
 *
 * @apiNote Implementations should be stateless. Per-mob state belongs in an attachment.
 * @since 1.0.0
 */
public interface MobTrait {
    /** The registry key for mob traits. */
    ResourceKey<Registry<MobTrait>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("thaumaturge", "mob_trait"));

    /**
     * Returns whether this trait is a champion modifier. Champion traits make the carrier count
     * as a champion, take part in the random champion roll and award champion loot.
     *
     * @return {@code true} for champion modifiers
     */
    default boolean isChampion() {
        return false;
    }

    /**
     * Returns whether this trait makes the carrier part of the taint. Taint carriers are immune
     * to flux taint and taint hazards, inflict flux taint when they hit, and render with the taint
     * overlay.
     *
     * @return {@code true} for taint traits
     */
    default boolean isTaint() {
        return false;
    }

    /**
     * Returns whether this trait replaces the mob's own AI while present. When any trait on a
     * mob returns {@code true}, the engine stashes the mob's existing goals, stops its brain, and
     * runs only trait-injected goals until no such trait remains.
     *
     * @param mob the carrier
     * @return {@code true} to take over the mob's AI
     */
    default boolean replacesNativeAi(LivingEntity mob) {
        return false;
    }

    /**
     * Declares the attribute modifiers this trait applies to the carrier. Called whenever the
     * engine reconciles the carrier's modifiers: when the trait is added or removed and when the
     * mob joins a level.
     *
     * @param mob       the carrier
     * @param modifiers the sink that receives the modifiers
     */
    default void modifiers(LivingEntity mob, MobTraitModifiers modifiers) {}

    /**
     * Declares the AI goals this trait injects. Called whenever the engine rebuilds the
     * carrier's goals. Only {@link PathfinderMob}s receive goals.
     *
     * @param mob   the carrier
     * @param goals the sink that receives the goals
     */
    default void goals(PathfinderMob mob, MobTraitGoals goals) {}

    /**
     * Returns the aspects this trait adds to the carrier when it is scanned.
     *
     * @param mob the carrier
     * @return the extra aspects, never null
     */
    default AspectList aspects(LivingEntity mob) {
        return AspectList.EMPTY;
    }

    /**
     * Runs once when the trait is added to a mob, after its modifiers are applied.
     *
     * @param mob the carrier
     */
    default void onAdded(LivingEntity mob) {}

    /**
     * Runs once when the trait is removed from a mob, after its modifiers and goals are gone.
     *
     * @param mob the former carrier
     */
    default void onRemoved(LivingEntity mob) {}

    /**
     * Runs once every server tick for each carrier.
     *
     * @param mob the carrier
     */
    default void tick(LivingEntity mob) {}

    /**
     * Runs when the carrier deals damage, before the damage is applied.
     *
     * @param mob    the attacking carrier
     * @param target the entity being hurt
     * @param source the damage source
     * @param amount the incoming amount
     * @return the amount to apply
     */
    default float onAttack(LivingEntity mob, LivingEntity target, DamageSource source, float amount) {
        return amount;
    }

    /**
     * Runs when the carrier takes damage, before the damage is applied.
     *
     * @param mob      the hurt carrier
     * @param attacker the living entity responsible, or null
     * @param source   the damage source
     * @param amount   the incoming amount
     * @return the amount to apply
     */
    default float onHurt(LivingEntity mob, @Nullable LivingEntity attacker, DamageSource source, float amount) {
        return amount;
    }
}
