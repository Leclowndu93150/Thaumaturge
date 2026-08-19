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
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectFlux implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("flux");

    private static final int BASE_DAMAGE = 3;
    private static final int POWER_COMPLEXITY_FACTOR = 3;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_flux"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.VITIUM;
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
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            struck.hurtServer(level, level.damageSources().indirectMagic(struck, ctx.caster()), damageForDisplay(settings, ctx.power()));
        }
        return false;
    }

    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("power", "focus.common.power", new SettingDefinition.IntRange(1, 5)));
    }

    @Override
    public void onCast(LivingEntity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.CHORUS_FLOWER_GROW, SoundSource.PLAYERS, 2.0F, 2.0F + (float) (caster.level().getRandom().nextGaussian() * 0.1F));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        float purple = 0.25F + level.getRandom().nextFloat() * 0.25F;
        FluxSwirlParticleOptions data = new FluxSwirlParticleOptions(ARGB.colorFromFloat(1.0F, purple, 0.0F, purple), 2.0F + level.getRandom().nextFloat(),
                0.25F + level.getRandom().nextFloat() * 0.25F);
        level.addParticle(data, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }
}
