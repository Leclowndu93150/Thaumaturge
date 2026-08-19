package com.leclowndu93150.thaumaturge.api.casters;

import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * An element that applies the spell's payload to a resolved hit, such as damage, a potion
 * effect, or block interaction.
 *
 * @since 1.0.0
 */
public non-sealed interface FocusEffect extends FocusElement {
    @Override
    default Set<SupplyType> requires() {
        return SUPPLIES_TARGETS;
    }

    @Override
    default Set<SupplyType> supplies() {
        return SUPPLIES_NOTHING;
    }

    /**
     * Applies this effect to one target. Called once per supplied target; the spell's
     * current power is {@link CastContext#power()}.
     *
     * @param ctx        the execution scope
     * @param settings   the configured settings
     * @param target     the resolved hit to affect
     * @param trajectory the trajectory that produced the hit, or null when unknown
     * @param index      the index of this target within the supplied batch
     * @return true when the effect did something
     */
    boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index);

    /**
     * The damage figure shown for this effect in the focal manipulator.
     *
     * @param settings the configured settings
     * @param power    the power the display assumes
     * @return the displayed damage, or 0 when the effect deals none
     */
    default float damageForDisplay(FocusSettings settings, float power) {
        return 0.0F;
    }

    /**
     * Called once on the server when a spell containing this effect is cast, before any node
     * executes.
     *
     * @param caster the casting entity
     */
    default void onCast(LivingEntity caster) {}

    /**
     * Spawns this effect's travel or impact particles without carrier drift. Called on the
     * client only.
     *
     * @param level  the client level
     * @param pos    the particle position
     * @param motion the particle motion
     */
    default void impactParticles(Level level, Vec3 pos, Vec3 motion) {
        impactParticles(level, pos, motion, Vec3.ZERO);
    }

    /**
     * Spawns this effect's travel or impact particles. Called on the client only.
     *
     * <p>The drift is a carrier velocity the particle applies every tick of its life,
     * unaffected by its own friction or gravity. Cast bursts pass the caster's velocity here
     * so the flash keeps pace with a moving caster.
     *
     * @param level  the client level
     * @param pos    the particle position
     * @param motion the particle motion
     * @param drift  the per-tick carrier drift
     */
    default void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {}
}
