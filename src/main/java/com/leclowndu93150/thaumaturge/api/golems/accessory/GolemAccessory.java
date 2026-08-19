package com.leclowndu93150.thaumaturge.api.golems.accessory;

import net.minecraft.resources.Identifier;

/**
 * A wearable golem accessory applied by using its item on a golem. Accessories stack freely
 * except within an exclusion group, of which a golem may wear at most one.
 *
 * <p>Stat fields modify the wearing golem: {@code healthBonus} and {@code armorBonus} add flat
 * points, {@code rangeFactor} and {@code speedFactor} multiply sight range and movement speed,
 * and {@code regenFactor} multiplies the self-repair interval, so values below 1 heal faster.
 * When {@code killCredit} is set, entities slain by the golem count as kills by its owner.
 *
 * @param id the accessory id, unique across all registered accessories
 * @param group the exclusion group, or {@link Group#NONE} for unrestricted accessories
 * @param healthBonus flat max health added while worn
 * @param rangeFactor multiplier on the golem's sight and work range
 * @param speedFactor multiplier on the golem's movement speed
 * @param regenFactor multiplier on the golem's self-repair interval
 * @param armorBonus flat armor added while worn
 * @param killCredit whether the golem's kills are credited to its owner
 * @since 1.0.0
 */
public record GolemAccessory(Identifier id, Group group, int healthBonus, float rangeFactor, float speedFactor, float regenFactor, int armorBonus, boolean killCredit) {
    /**
     * Exclusion groups for accessories occupying the same spot on a golem.
     *
     * @since 1.0.0
     */
    public enum Group {
        NONE, HAT, EYES
    }
}
