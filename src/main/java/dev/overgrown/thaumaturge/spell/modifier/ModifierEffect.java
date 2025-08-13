package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public interface ModifierEffect {
    default void applySelf(SelfSpellDelivery delivery) {}
    default void applyTargeted(TargetedSpellDelivery delivery) {}
    default void applyAoe(AoeSpellDelivery delivery) {}
}
