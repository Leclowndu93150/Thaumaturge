package com.leclowndu93150.thaumaturge.content.focus.medium;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.focus.FocusRayTrace;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FocusMediumBolt extends FocusMediumTouch {
    private static final Identifier KEY = TCIds.rl("bolt");

    static final double BOLT_RANGE = 16.0;

    private static final int COMPLEXITY = 5;
    private static final float BOLT_WIDTH_FACTOR = 0.66F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_bolt"), Optional.empty(), false);
    }

    @Override
    public int complexity(FocusSettings settings) {
        return COMPLEXITY;
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.POTENTIA;
    }

    @Override
    protected double range(CastContext ctx) {
        return BOLT_RANGE;
    }

    @Override
    protected void onTrajectory(CastContext ctx, Trajectory trajectory) {
        Vec3 end = trajectory.direction().normalize();
        HitResult ray = FocusRayTrace.pointedEntity(ctx.level(), ctx.caster(), trajectory.source(), end, RAY_MIN_RANGE, BOLT_RANGE, RAY_PADDING, false);
        if (ray == null) {
            end = end.scale(BOLT_RANGE).add(trajectory.source());
            HitResult blockRay = FocusRayTrace.clipBlocks(ctx.level(), ctx.caster(), trajectory.source(), end);
            if (blockRay.getType() != HitResult.Type.MISS) {
                end = blockRay.getLocation();
            }
        } else if (ray instanceof EntityHitResult entityHit) {
            end = end.scale(trajectory.source().distanceTo(entityHit.getEntity().position()));
            end = end.add(trajectory.source());
        }
        List<Identifier> effects = ctx.effects();
        if (!effects.isEmpty() && ctx.level() instanceof ServerLevel level) {
            int r = 0;
            int g = 0;
            int b = 0;
            for (Identifier effectId : effects) {
                int color = FocusEngine.color(effectId);
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
            }
            r /= effects.size();
            g /= effects.size();
            b /= effects.size();
            Effects.arcBolt(level, trajectory.source()).to(end).color((r << 16) | (g << 8) | b).width(ctx.power() * BOLT_WIDTH_FACTOR).send();
        }
    }
}
