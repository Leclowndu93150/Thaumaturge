package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The data flowing between spell nodes during execution: rays still in flight and resolved
 * hits. Either array may be null, meaning nothing of that kind is currently supplied.
 *
 * @param trajectories the rays handed to the next node, or null
 * @param targets      the hits handed to the next node, or null
 * @since 1.0.0
 */
public record CastStreams(Trajectory @Nullable [] trajectories, HitResult @Nullable [] targets) {
    /** Streams supplying nothing. */
    public static final CastStreams EMPTY = new CastStreams(null, null);

    private static final float SOURCE_EYE_OFFSET = 0.1F;

    /**
     * The standard cast origin: one trajectory from just below the caster's eyes along the
     * look vector, and the caster itself as the initial target.
     *
     * @param caster the casting entity
     * @return the origin streams
     */
    public static CastStreams fromCaster(LivingEntity caster) {
        return new CastStreams(new Trajectory[]{new Trajectory(sourceVector(caster), caster.getLookAngle())}, new HitResult[]{new EntityHitResult(caster)});
    }

    /**
     * A cast origin aimed at the vertical center of a target entity, adjusted by a vertical
     * offset.
     *
     * @param caster the casting entity
     * @param target the entity to aim at
     * @param offset the vertical aim adjustment in blocks
     * @return the origin streams
     */
    public static CastStreams fromCasterToTarget(LivingEntity caster, Entity target, double offset) {
        Vec3 source = sourceVector(caster);
        double dx = target.getX() - source.x;
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - source.y;
        double dz = target.getZ() - source.z;
        return new CastStreams(new Trajectory[]{new Trajectory(source, new Vec3(dx, dy + offset, dz).normalize())}, new HitResult[]{new EntityHitResult(caster)});
    }

    /**
     * A cast origin aimed at a world position.
     *
     * @param caster the casting entity
     * @param loc    the position to aim at
     * @return the origin streams
     */
    public static CastStreams fromCasterToPoint(LivingEntity caster, Vec3 loc) {
        Vec3 source = sourceVector(caster);
        return new CastStreams(new Trajectory[]{new Trajectory(source, loc.subtract(source).normalize())}, new HitResult[]{new EntityHitResult(caster)});
    }

    private static Vec3 sourceVector(LivingEntity entity) {
        return entity.position().add(0.0, entity.getEyeHeight() - SOURCE_EYE_OFFSET, 0.0);
    }
}
