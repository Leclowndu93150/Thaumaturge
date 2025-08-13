package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;

public class ScatterModifier implements ModifierEffect {

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        delivery.setScatterSize(1);
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        SpellCastPacket.KeyType tier = delivery.getTier();
        switch (tier) {
            case PRIMARY -> {
                delivery.setProjectileCount(2);
                delivery.setSpread(10.0f);
            }
            case SECONDARY -> {
                delivery.setProjectileCount(3);
                delivery.setSpread(15.0f);
                delivery.setScatterSize(1);
            }
            case TERNARY -> {
                delivery.setProjectileCount(4);
                delivery.setSpread(20.0f);
            }
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        delivery.setScatterSize(1);
    }
}