package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

/**
 * Stable modifier
 */
public class StableModifier implements ModifierEffect {

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
    }
}