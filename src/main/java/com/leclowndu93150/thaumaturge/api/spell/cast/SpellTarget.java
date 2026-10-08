package com.leclowndu93150.thaumaturge.api.spell.cast;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * One point the spell has reached: what is there, where it is and which way the spell heads.
 *
 * <p>Deliveries launch from {@link #position()} along {@link #direction()}; effects act on
 * {@link #hit()}. A miss still carries a position, so area effects can fire in empty air.
 *
 * @param hit       what the spell reached
 * @param position  where the spell is now
 * @param direction the normalized heading
 * @since 1.0.0
 */
public record SpellTarget(HitResult hit, Vec3 position, Vec3 direction) {
    private static final double EYE_OFFSET = 0.1;

    /**
     * A target at a hit's location.
     *
     * @param hit       the hit
     * @param direction the heading, normalized here
     * @return the target
     */
    public static SpellTarget of(HitResult hit, Vec3 direction) {
        return new SpellTarget(hit, hit.getLocation(), direction.normalize());
    }

    /**
     * A target on an entity, positioned at its centre.
     *
     * @param entity    the entity
     * @param direction the heading
     * @return the target
     */
    public static SpellTarget entity(Entity entity, Vec3 direction) {
        Vec3 centre = entity.getBoundingBox().getCenter();
        return new SpellTarget(new EntityHitResult(entity, centre), centre, direction.normalize());
    }

    /**
     * The origin of a cast: the caster as the hit, the eyes as the position, the look as the
     * heading.
     *
     * @param caster the casting entity
     * @return the target
     */
    public static SpellTarget origin(LivingEntity caster) {
        Vec3 eyes = caster.position().add(0.0, caster.getEyeHeight() - EYE_OFFSET, 0.0);
        return new SpellTarget(new EntityHitResult(caster, eyes), eyes, caster.getLookAngle());
    }

    /**
     * The origin of a cast aimed at a point instead of along the look.
     *
     * @param caster the casting entity
     * @param point  the point to aim at
     * @return the target
     */
    public static SpellTarget originTowards(LivingEntity caster, Vec3 point) {
        SpellTarget origin = origin(caster);
        return new SpellTarget(
                origin.hit(),
                origin.position(),
                point.subtract(origin.position()).normalize());
    }

    /**
     * The entity that was hit.
     *
     * @return the entity, empty for block hits and misses
     */
    public Optional<Entity> entity() {
        return hit instanceof EntityHitResult entityHit ? Optional.of(entityHit.getEntity()) : Optional.empty();
    }

    /**
     * The block that was hit.
     *
     * @return the hit, empty for entity hits and misses
     */
    public Optional<BlockHitResult> block() {
        return hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK
                ? Optional.of(blockHit)
                : Optional.empty();
    }

    /**
     * The block position the spell sits in.
     *
     * @return the containing block position
     */
    public BlockPos blockPos() {
        return BlockPos.containing(position);
    }

    /**
     * A copy moved and turned.
     *
     * @param position  the new position
     * @param direction the new heading, normalized here
     * @return the copy, keeping the hit
     */
    public SpellTarget moved(Vec3 position, Vec3 direction) {
        return new SpellTarget(hit, position, direction.normalize());
    }
}
