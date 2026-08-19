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
import com.leclowndu93150.thaumaturge.content.particle.FrostFlakeParticleOptions;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public final class FocusEffectFrost implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("frost");

    private static final int BASE_DAMAGE = 3;
    private static final int POWER_COMPLEXITY_FACTOR = 2;
    private static final int SLOW_TICKS_PER_DURATION = 20;
    private static final float POTENCY_DIVISOR = 3.0F;
    private static final float MAX_FREEZE_RADIUS = 16.0F;
    private static final int FREEZE_RADIUS_FACTOR = 2;
    private static final int MELT_DELAY_MIN = 60;
    private static final int MELT_DELAY_MAX = 120;

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
        return TCAspects.GELUM;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return settings.value("duration") + settings.value("power") * POWER_COMPLEXITY_FACTOR;
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
            float damage = damageForDisplay(settings, ctx.power());
            int duration = SLOW_TICKS_PER_DURATION * settings.value("duration");
            int potency = (int) (1.0F + settings.value("power") * ctx.power() / POTENCY_DIVISOR);
            struck.hurtServer(level, level.damageSources().thrown(struck, ctx.caster()), damage);
            if (struck instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, potency));
            }
        } else if (target instanceof BlockHitResult blockHit) {
            float f = Math.min(MAX_FREEZE_RADIUS, FREEZE_RADIUS_FACTOR * settings.value("power") * ctx.power());
            for (BlockPos pos : BlockPos.betweenClosed(blockHit.getBlockPos().offset((int) -f, (int) -f, (int) -f), blockHit.getBlockPos().offset((int) f, (int) f, (int) f))) {
                if (pos.distToCenterSqr(target.getLocation().x, target.getLocation().y, target.getLocation().z) > f * f) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.WATER) && state.getFluidState().isSource() && level.isUnobstructed(Blocks.FROSTED_ICE.defaultBlockState(), pos, CollisionContext.empty())) {
                    level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                    level.scheduleTick(pos.immutable(), Blocks.FROSTED_ICE, Mth.nextInt(level.getRandom(), MELT_DELAY_MIN, MELT_DELAY_MAX));
                }
            }
        }
        return false;
    }

    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("power", "focus.common.power", new SettingDefinition.IntRange(1, 5)),
                new SettingDefinition("duration", "focus.common.duration", new SettingDefinition.IntRange(2, 10)));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        FrostFlakeParticleOptions data = new FrostFlakeParticleOptions((float) (0.7F + level.getRandom().nextGaussian() * 0.3F));
        level.addParticle(data, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(LivingEntity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 0.2F, 1.0F + (float) (caster.level().getRandom().nextGaussian() * 0.05F));
    }
}
