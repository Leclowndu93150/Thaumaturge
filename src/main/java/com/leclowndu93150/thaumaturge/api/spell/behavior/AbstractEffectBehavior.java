package com.leclowndu93150.thaumaturge.api.spell.behavior;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellEffectEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Base for effect behaviours. Runs {@link #apply} once per target the touch budget admits and no
 * {@link SpellEffectEvent} listener cancels, then hands the incoming targets to the children.
 *
 * <p>When the {@link SpellStats#RADIUS} stat is above zero and {@link #widens()} is true, every
 * living entity within that radius of a target is affected as well, the caster excepted.
 *
 * @since 1.0.0
 */
public abstract class AbstractEffectBehavior implements SpellBehavior {
    @Override
    public final void execute(CastContext ctx, List<SpellTarget> incoming) {
        int index = 0;
        for (SpellTarget target : widen(ctx, incoming)) {
            if (!admit(ctx, target)) {
                continue;
            }
            SpellEffectEvent event = NeoForge.EVENT_BUS.post(new SpellEffectEvent(ctx, target, ctx.power()));
            if (!event.isCanceled()) {
                apply(ctx, target, event.power(), index++);
            }
        }
        ctx.proceed(incoming);
    }

    /**
     * Acts on one target.
     *
     * @param ctx    the node's context
     * @param target the target
     * @param power  the power to use, after event listeners
     * @param index  the target's position among the targets this node affected so far, for staggering
     */
    protected abstract void apply(CastContext ctx, SpellTarget target, float power, int index);

    /**
     * Whether the radius stat spreads this effect to entities around each target. Effects with an
     * area of their own return false and read the stat themselves.
     *
     * @return true by default
     */
    protected boolean widens() {
        return true;
    }

    private List<SpellTarget> widen(CastContext ctx, List<SpellTarget> incoming) {
        float radius = ctx.state().get(SpellStats.RADIUS);
        if (radius <= 0.0F || !widens()) {
            return incoming;
        }
        List<SpellTarget> out = new ArrayList<>(incoming);
        Set<Integer> seen = new HashSet<>();
        for (SpellTarget target : incoming) {
            target.entity().ifPresent(entity -> seen.add(entity.getId()));
        }
        LivingEntity caster = ctx.caster();
        for (SpellTarget target : incoming) {
            AABB area = new AABB(target.position(), target.position()).inflate(radius);
            for (LivingEntity near : ctx.level().getEntitiesOfClass(LivingEntity.class, area, Entity::isAlive)) {
                if (near != caster && near.distanceToSqr(target.position()) <= radius * radius && seen.add(near.getId())) {
                    out.add(SpellTarget.entity(near, near.getBoundingBox().getCenter().subtract(target.position())));
                }
            }
        }
        return out;
    }

    private static boolean admit(CastContext ctx, SpellTarget target) {
        if (target.entity().isPresent()) {
            return ctx.budget().claim(target.entity().get());
        }
        if (target.block().isPresent()) {
            return ctx.budget().claim(target.block().get().getBlockPos());
        }
        return true;
    }
}
