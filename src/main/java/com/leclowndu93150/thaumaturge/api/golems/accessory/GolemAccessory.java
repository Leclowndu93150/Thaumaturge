package com.leclowndu93150.thaumaturge.api.golems.accessory;

import com.leclowndu93150.thaumaturge.TTIds;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/**
 * A wearable golem accessory, put on by using an item bound to it through
 * {@link GolemAccessoryItem#DATA_MAP} on a golem. Accessories stack freely except within an
 * exclusion {@link Group}, of which a golem may wear at most one.
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
 * @param behavior server-side behaviour with its own saved state, or empty for a stat-only
 *                 accessory
 * @since 1.0.0
 */
public record GolemAccessory(
        ResourceLocation id,
        Group group,
        int healthBonus,
        float rangeFactor,
        float speedFactor,
        float regenFactor,
        int armorBonus,
        boolean killCredit,
        Optional<GolemAccessoryBehavior<?>> behavior) {
    /**
     * Validates the components.
     *
     * @throws NullPointerException when {@code id}, {@code group} or {@code behavior} is null
     */
    public GolemAccessory {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(behavior, "behavior");
    }

    /**
     * Creates a stat-only accessory with no behaviour.
     *
     * @param id the accessory id
     * @param group the exclusion group
     * @param healthBonus flat max health added while worn
     * @param rangeFactor multiplier on sight and work range
     * @param speedFactor multiplier on movement speed
     * @param regenFactor multiplier on the self-repair interval
     * @param armorBonus flat armor added while worn
     * @param killCredit whether kills are credited to the owner
     */
    public GolemAccessory(
            ResourceLocation id,
            Group group,
            int healthBonus,
            float rangeFactor,
            float speedFactor,
            float regenFactor,
            int armorBonus,
            boolean killCredit) {
        this(id, group, healthBonus, rangeFactor, speedFactor, regenFactor, armorBonus, killCredit, Optional.empty());
    }

    /**
     * An exclusion group for accessories that occupy the same spot on a golem. A golem wears at
     * most one accessory from each group, except {@link #NONE}, which never excludes anything.
     * Addons may declare their own groups; groups compare by id.
     *
     * @param id the group id
     * @since 1.0.0
     */
    public record Group(ResourceLocation id) {
        /** The group of accessories that never exclude each other. */
        public static final Group NONE = new Group(TTIds.rl("none"));

        /** Hats, such as the top hat and the fez. */
        public static final Group HAT = new Group(TTIds.rl("hat"));

        /** Eyewear, such as the glasses and the visor. */
        public static final Group EYES = new Group(TTIds.rl("eyes"));

        /**
         * Validates the id.
         *
         * @throws NullPointerException when {@code id} is null
         */
        public Group {
            Objects.requireNonNull(id, "id");
        }

        /**
         * Whether an accessory in this group rules out wearing one in {@code other}.
         *
         * @param other the group of the other accessory
         * @return true when both are the same group and it is not {@link #NONE}
         */
        public boolean excludes(Group other) {
            return !equals(NONE) && equals(other);
        }
    }
}
