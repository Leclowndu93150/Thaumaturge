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
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectCurse implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("curse");

    private static final int POWER_COMPLEXITY_FACTOR = 3;
    private static final int DURATION_TICKS_FACTOR = 20;
    private static final float POTENCY_DIVISOR = 2.0F;
    private static final float CASCADE_START = 0.85F;
    private static final float CASCADE_STEP = 0.15F;
    private static final int BAMF_COLOR = 6946821;
    private static final double MAX_SAP_RADIUS = 8.0;
    private static final double SAP_RADIUS_FACTOR = 1.5;
    private static final float COLOR_DIVISOR = 255.0F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_curse"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.MORTUUS;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return settings.value("duration") + settings.value("power") * POWER_COMPLEXITY_FACTOR;
    }

    @Override
    public float damageForDisplay(FocusSettings settings, float power) {
        return (1.0F + settings.value("power")) * power;
    }

    @Override
    public boolean apply(CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(ctx.level() instanceof ServerLevel level)) {
            return false;
        }
        Effects.bamf(level, target.getLocation()).color(((BAMF_COLOR >> 16) & 0xFF) / COLOR_DIVISOR, ((BAMF_COLOR >> 8) & 0xFF) / COLOR_DIVISOR, (BAMF_COLOR & 0xFF) / COLOR_DIVISOR).withSound()
                .fancy().send();
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            Entity struck = entityHit.getEntity();
            float damage = damageForDisplay(settings, ctx.power());
            int duration = DURATION_TICKS_FACTOR * settings.value("duration");
            int eff = (int) (settings.value("power") * ctx.power() / POTENCY_DIVISOR);
            if (eff < 0) {
                eff = 0;
            }
            struck.hurtServer(level, level.damageSources().indirectMagic(struck, ctx.caster()), damage);
            if (struck instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, duration, eff));
                float c = CASCADE_START;
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, eff));
                    c -= CASCADE_STEP;
                }
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, eff));
                    c -= CASCADE_STEP;
                }
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, duration * 2, eff));
                    c -= CASCADE_STEP;
                }
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration * 3, eff));
                    c -= CASCADE_STEP;
                }
                if (level.getRandom().nextFloat() < c) {
                    living.addEffect(new MobEffectInstance(MobEffects.UNLUCK, duration * 3, eff));
                }
            }
        } else if (target instanceof BlockHitResult blockHit) {
            float f = (float) Math.min(MAX_SAP_RADIUS, SAP_RADIUS_FACTOR * settings.value("power") * ctx.power());
            for (BlockPos pos : BlockPos.betweenClosed(blockHit.getBlockPos().offset((int) -f, (int) -f, (int) -f), blockHit.getBlockPos().offset((int) f, (int) f, (int) f))) {
                if (pos.distToCenterSqr(target.getLocation().x, target.getLocation().y, target.getLocation().z) <= f * f && level.getBlockState(pos.above()).isAir()
                        && level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
                    level.setBlockAndUpdate(pos.above(), TCBlocks.EFFECT_SAP.get().defaultBlockState());
                }
            }
        }
        return false;
    }

    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("power", "focus.common.power", new SettingDefinition.IntRange(1, 5)),
                new SettingDefinition("duration", "focus.common.duration", new SettingDefinition.IntRange(1, 10)));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        level.addParticle(TCParticles.CURSE_SMOKE.get(), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(LivingEntity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.15F, 1.0F + caster.level().getRandom().nextFloat() / 2.0F);
    }
}
