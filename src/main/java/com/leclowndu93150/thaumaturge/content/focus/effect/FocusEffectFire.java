package com.leclowndu93150.thaumaturge.content.focus.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.damagesource.TCDamageSources;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.focus.FocusFX;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectFire implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("fire");

    private static final int BASE_DAMAGE = 3;
    private static final int DURATION_COMPLEXITY_FACTOR = 1;
    private static final int POWER_COMPLEXITY_FACTOR = 2;
    private static final int FIRE_PLACE_FLAGS = 11;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("base_auromancy"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.IGNIS;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return settings.value("duration") * DURATION_COMPLEXITY_FACTOR + settings.value("power") * POWER_COMPLEXITY_FACTOR;
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
            if (struck.fireImmune()) {
                return false;
            }
            float fire = 1 + settings.value("duration") * settings.value("duration");
            float damage = damageForDisplay(settings, ctx.power());
            fire *= ctx.power();
            struck.hurtServer(level, TCDamageSources.focusFire(level, struck, ctx.caster()), damage);
            if (fire > 0.0F) {
                struck.igniteForSeconds(Math.round(fire));
            }
            return true;
        }
        if (target instanceof BlockHitResult blockHit && settings.value("duration") > 0) {
            BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
            if (level.getBlockState(pos).isAir() && level.getRandom().nextFloat() < ctx.power()) {
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                level.setBlock(pos, BaseFireBlock.getState(level, pos), FIRE_PLACE_FLAGS);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("power", "focus.common.power", new SettingDefinition.IntRange(1, 5)),
                new SettingDefinition("duration", "focus.fire.burn", new SettingDefinition.IntRange(0, 5)));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        FlameFanParticleOptions data = new FlameFanParticleOptions((float) (1.5 + level.getRandom().nextGaussian() * 0.2F), -0.2F, 0.7F);
        level.addParticle(data, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    @Override
    public void onCast(LivingEntity caster) {
        caster.level().playSound(null, caster.blockPosition().above(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F + (float) (caster.level().getRandom().nextGaussian() * 0.05F));
    }
}
