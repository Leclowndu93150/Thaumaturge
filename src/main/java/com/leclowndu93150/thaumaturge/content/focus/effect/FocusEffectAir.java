package com.leclowndu93150.thaumaturge.content.focus.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.focus.FocusFX;
import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectAir implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("air");

    private static final int BASE_DAMAGE = 1;
    private static final int POWER_COMPLEXITY_FACTOR = 2;
    private static final float KNOCKBACK_FACTOR = 0.25F;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_elemental"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.AER;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return settings.value("power") * POWER_COMPLEXITY_FACTOR;
    }

    @Override
    public float damageForDisplay(FocusSettings settings, float power) {
        return (BASE_DAMAGE + settings.value("power")) * power;
    }

    @Override
    public boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(ctx.level() instanceof ServerLevel level)) {
            return false;
        }
        FocusFX.impact(level, target.getLocation(), id());
        level.playSound(null, target.getLocation().x, target.getLocation().y, target.getLocation().z, SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.5F, 0.66F);
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            float damage = damageForDisplay(settings, ctx.power());
            struck.hurtServer(level, level.damageSources().thrown(struck, ctx.caster()), damage);
            if (struck instanceof LivingEntity living) {
                if (trajectory != null) {
                    living.knockback(damage * KNOCKBACK_FACTOR, -trajectory.direction().x, -trajectory.direction().z);
                } else {
                    living.knockback(damage * KNOCKBACK_FACTOR, -Mth.sin(struck.getYRot() * DEG_TO_RAD), Mth.cos(struck.getYRot() * DEG_TO_RAD));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("power", "focus.common.power", new SettingDefinition.IntRange(1, 5)));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        float s = (float) (2.0 + level.getRandom().nextGaussian() * 0.5);
        level.addParticle(new AirGustParticleOptions(s), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(LivingEntity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), TCSounds.WIND.get(), SoundSource.PLAYERS, 0.125F, 2.0F);
    }
}
