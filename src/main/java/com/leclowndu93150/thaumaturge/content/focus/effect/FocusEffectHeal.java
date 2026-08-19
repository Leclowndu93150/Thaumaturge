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
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectHeal implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("heal");

    private static final int POWER_COMPLEXITY_FACTOR = 4;
    private static final float UNDEAD_DAMAGE_FACTOR = 1.5F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_heal"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.VICTUS;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return settings.value("power") * POWER_COMPLEXITY_FACTOR;
    }

    @Override
    public float damageForDisplay(FocusSettings settings, float power) {
        return -settings.value("power") * power;
    }

    @Override
    public boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(ctx.level() instanceof ServerLevel level)) {
            return false;
        }
        FocusFX.impact(level, target.getLocation(), id());
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            if (living.isInvertedHealAndHarm()) {
                living.hurtServer(level, level.damageSources().indirectMagic(ctx.caster(), ctx.caster()), settings.value("power") * ctx.power() * UNDEAD_DAMAGE_FACTOR);
            } else {
                living.heal(settings.value("power") * ctx.power());
            }
        }
        return false;
    }

    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("power", "focus.heal.power", new SettingDefinition.IntRange(1, 5)));
    }

    @Override
    public void onCast(LivingEntity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.CHORUS_FLOWER_GROW, SoundSource.PLAYERS, 2.0F, 2.0F + (float) (caster.level().getRandom().nextGaussian() * 0.1F));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        level.addParticle(TCParticles.HEAL_FLASH.get(), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }
}
