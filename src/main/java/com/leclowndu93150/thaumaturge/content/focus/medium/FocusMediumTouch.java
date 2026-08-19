package com.leclowndu93150.thaumaturge.content.focus.medium;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusMedium;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.focus.FocusFX;
import com.leclowndu93150.thaumaturge.content.focus.FocusRayTrace;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FocusMediumTouch implements FocusMedium {
    private static final Identifier KEY = TCIds.rl("touch");

    static final double RAY_MIN_RANGE = 0.25;
    static final float RAY_PADDING = 0.25F;

    private static final int COMPLEXITY = 2;
    private static final double MOTION_HALF = 2.0;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("base_auromancy"), Optional.empty(), false);
    }

    @Override
    public int complexity(FocusSettings settings) {
        return COMPLEXITY;
    }

    @Override
    public Set<SupplyType> supplies() {
        return SUPPLIES_BOTH;
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.AVERSIO;
    }

    protected double range(CastContext ctx) {
        return ctx.caster() instanceof Player player ? player.blockInteractionRange() : 0.0;
    }

    protected void onTrajectory(CastContext ctx, Trajectory trajectory) {
        if (ctx.level() instanceof ServerLevel level) {
            FocusFX.burst(level, trajectory.source(), trajectory.direction().scale(1.0 / MOTION_HALF), ctx.effects(), ctx.caster());
        }
    }

    @Override
    public CastStreams cast(CastContext ctx, FocusSettings settings, CastStreams incoming) {
        Trajectory[] supplied = incoming.trajectories();
        List<Trajectory> trajectories = new ArrayList<>();
        List<HitResult> targets = new ArrayList<>();
        double range = range(ctx);
        LivingEntity caster = ctx.caster();
        boolean playerCaster = caster instanceof Player;
        if (supplied != null) {
            for (Trajectory sT : supplied) {
                onTrajectory(ctx, sT);
                trajectories.add(traceTrajectory(ctx, sT, range));
                if (playerCaster) {
                    HitResult ray = FocusRayTrace.pointedOrBlock(ctx.level(), caster, sT.source(), sT.direction().normalize(), RAY_MIN_RANGE, range, RAY_PADDING);
                    if (ray != null) {
                        targets.add(ray);
                    }
                }
            }
        }
        return new CastStreams(trajectories.toArray(new Trajectory[0]), targets.toArray(new HitResult[0]));
    }

    private Trajectory traceTrajectory(CastContext ctx, Trajectory sT, double range) {
        Vec3 direction = sT.direction().normalize();
        Vec3 end = direction;
        HitResult ray = FocusRayTrace.pointedEntity(ctx.level(), ctx.caster(), sT.source(), end, RAY_MIN_RANGE, range, RAY_PADDING, false);
        if (ray == null) {
            end = end.scale(range).add(sT.source());
            HitResult blockRay = FocusRayTrace.clipBlocks(ctx.level(), ctx.caster(), sT.source(), end);
            if (blockRay.getType() != HitResult.Type.MISS) {
                end = blockRay.getLocation();
            }
        } else if (ray instanceof EntityHitResult entityHit) {
            end = end.scale(sT.source().distanceTo(entityHit.getEntity().position()));
            end = end.add(sT.source());
        }
        return new Trajectory(end, direction);
    }
}
