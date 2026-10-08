package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.IGolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;

public final class GolemStats {
    private static final double BASE_HEALTH = 10.0;
    private static final double FRAGILE_FACTOR = 0.75;
    private static final double REINFORCED_FACTOR = 1.5;
    private static final double DAMAGE_PER_RANK = 0.25;

    private GolemStats() {}

    public static double health(IGolemProperties props) {
        double health = BASE_HEALTH + props.material().healthMod();
        return props.hasTrait(TTGolemTraits.FRAGILE.get()) ? Math.floor(health * FRAGILE_FACTOR) : health;
    }

    public static double armor(IGolemProperties props) {
        double armor = props.material().armor();
        if (props.hasTrait(TTGolemTraits.ARMORED.get())) {
            armor = Math.floor(Math.max(armor * REINFORCED_FACTOR, armor + 1.0));
        }
        return props.hasTrait(TTGolemTraits.FRAGILE.get()) ? Math.floor(armor * FRAGILE_FACTOR) : armor;
    }

    public static double meleeDamage(IGolemProperties props) {
        if (!props.hasTrait(TTGolemTraits.FIGHTER.get())) {
            return 0.0;
        }
        double damage = props.material().damage();
        if (props.hasTrait(TTGolemTraits.BRUTAL.get())) {
            damage = Math.max(damage * REINFORCED_FACTOR, damage + 1.0);
        }
        return damage + DAMAGE_PER_RANK * props.rank();
    }
}
