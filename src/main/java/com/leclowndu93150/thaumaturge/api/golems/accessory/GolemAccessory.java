package com.leclowndu93150.thaumaturge.api.golems.accessory;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

/** A wearable golem accessory with optional server-authoritative behavior. */
public final class GolemAccessory {
    private final ResourceLocation id;
    private final Group group;
    private final int healthBonus;
    private final float rangeFactor;
    private final float speedFactor;
    private final float regenFactor;
    private final int armorBonus;
    private final boolean killCredit;
    private final @Nullable GolemAccessoryBehavior behavior;

    /** Binary-compatible constructor for existing stat-only accessories. */
    public GolemAccessory(
            ResourceLocation id,
            Group group,
            int healthBonus,
            float rangeFactor,
            float speedFactor,
            float regenFactor,
            int armorBonus,
            boolean killCredit) {
        this(id, group, healthBonus, rangeFactor, speedFactor, regenFactor, armorBonus, killCredit, null);
    }

    public GolemAccessory(
            ResourceLocation id,
            Group group,
            int healthBonus,
            float rangeFactor,
            float speedFactor,
            float regenFactor,
            int armorBonus,
            boolean killCredit,
            @Nullable GolemAccessoryBehavior behavior) {
        this.id = Objects.requireNonNull(id);
        this.group = Objects.requireNonNull(group);
        this.healthBonus = healthBonus;
        this.rangeFactor = rangeFactor;
        this.speedFactor = speedFactor;
        this.regenFactor = regenFactor;
        this.armorBonus = armorBonus;
        this.killCredit = killCredit;
        this.behavior = behavior;
    }

    public ResourceLocation id() {
        return id;
    }

    public Group group() {
        return group;
    }

    public int healthBonus() {
        return healthBonus;
    }

    public float rangeFactor() {
        return rangeFactor;
    }

    public float speedFactor() {
        return speedFactor;
    }

    public float regenFactor() {
        return regenFactor;
    }

    public int armorBonus() {
        return armorBonus;
    }

    public boolean killCredit() {
        return killCredit;
    }

    public @Nullable GolemAccessoryBehavior behavior() {
        return behavior;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof GolemAccessory other)) return false;
        return healthBonus == other.healthBonus
                && Float.compare(rangeFactor, other.rangeFactor) == 0
                && Float.compare(speedFactor, other.speedFactor) == 0
                && Float.compare(regenFactor, other.regenFactor) == 0
                && armorBonus == other.armorBonus
                && killCredit == other.killCredit
                && id.equals(other.id)
                && group == other.group
                && Objects.equals(behavior, other.behavior);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, group, healthBonus, rangeFactor, speedFactor, regenFactor, armorBonus, killCredit, behavior);
    }

    public enum Group {
        NONE,
        HAT,
        EYES
    }
}
