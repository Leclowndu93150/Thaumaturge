package com.leclowndu93150.thaumaturge.content.focus.eldritch;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageSources;
import java.util.Set;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectEldritch implements FocusEffect {
    private static final double KNOCKBACK = 0.35;
    private static final int COLOR = 0xAC80D0;
    @Override
    public Identifier id() {
        return TTIds.ELDRITCH_REND;
    }
    @Override
    public int complexity(FocusSettings settings) {
        return 0;
    }
    @Override
    public Set<SupplyType> requires() {
        return SUPPLIES_NOTHING;
    }
    @Override
    public float damageForDisplay(FocusSettings settings, float power) {
        return power;
    }

    @Override
    public boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(ctx.level() instanceof ServerLevel level) || !(target instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity living)) {
            return false;
        }
        final boolean damaged = living.hurtServer(level, TTDamageSources.eldritchSpell(level, ctx.caster()), ctx.power());
        if (damaged) {
            final Vec3 point = hit.getLocation();
            level.sendParticles(new DustParticleOptions(COLOR, 1.3F), point.x, point.y, point.z, 12, 0.25, 0.4, 0.25, 0);
            if (trajectory != null) {
                living.knockback(KNOCKBACK, -trajectory.direction().x, -trajectory.direction().z);
            }
        }
        return damaged;
    }
}
