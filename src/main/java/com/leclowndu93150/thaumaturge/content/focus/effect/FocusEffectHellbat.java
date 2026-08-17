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
import com.leclowndu93150.thaumaturge.content.entity.EntityFireBat;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class FocusEffectHellbat implements FocusEffect {
    private static final Identifier KEY = TCIds.rl("hellbat");

    private static final int BAT_COMPLEXITY_FACTOR = 8;
    private static final int SPAWN_LEVEL_EVENT = 2004;
    private static final float SPAWN_SPREAD = 0.5F;
    private static final int MAX_ACTIVE_BATS = 16;
    private static final double ACTIVE_BAT_RANGE = 32.0;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_hellbat"), Optional.empty(), false);
    }

    @Override
    public ResourceKey<IAspect> aspect() {
        return TCAspects.BESTIA;
    }

    @Override
    public int complexity(FocusSettings settings) {
        return settings.value("bats") * BAT_COMPLEXITY_FACTOR;
    }

    @Override
    public float damageForDisplay(FocusSettings settings, float power) {
        return settings.value("bats") * power;
    }

    @Override
    public boolean apply(
            CastContext ctx, FocusSettings settings, HitResult target, @Nullable Trajectory trajectory, int index) {
        if (!(ctx.level() instanceof ServerLevel level)) {
            return false;
        }
        LivingEntity struck =
                target instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living
                        ? living
                        : null;
        LivingEntity caster = ctx.caster();
        if (struck == caster) {
            struck = null;
        }
        Vec3 origin = target.getLocation();
        int bats = Math.min(settings.value("bats"), remainingBatBudget(level, caster, origin));
        if (bats <= 0) {
            return false;
        }
        int bonus = Math.round(ctx.power()) - 1;
        boolean spawned = false;
        for (int i = 0; i < bats; i++) {
            EntityFireBat bat = TCEntities.FIRE_BAT.get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (bat == null) {
                continue;
            }
            bat.snapTo(
                    origin.x
                            + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * SPAWN_SPREAD,
                    origin.y + 1.0 + level.getRandom().nextFloat() * SPAWN_SPREAD,
                    origin.z
                            + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * SPAWN_SPREAD,
                    level.getRandom().nextFloat() * 360.0F,
                    0.0F);
            bat.summon(caster, struck, bonus);
            if (level.addFreshEntity(bat)) {
                spawned = true;
            }
        }
        if (spawned) {
            level.levelEvent(SPAWN_LEVEL_EVENT, BlockPos.containing(origin), 0);
            level.playSound(
                    null,
                    origin.x,
                    origin.y,
                    origin.z,
                    TCSounds.ICE.get(),
                    SoundSource.PLAYERS,
                    0.2F,
                    0.95F + level.getRandom().nextFloat() * 0.1F);
        }
        return spawned;
    }

    private static int remainingBatBudget(ServerLevel level, @Nullable LivingEntity caster, Vec3 origin) {
        AABB area = new AABB(origin, origin).inflate(ACTIVE_BAT_RANGE);
        int active = level.getEntitiesOfClass(EntityFireBat.class, area, bat -> bat.owner == caster)
                .size();
        return MAX_ACTIVE_BATS - active;
    }

    @Override
    public List<SettingDefinition> settings() {
        return List.of(new SettingDefinition("bats", "focus.hellbat.bats", new SettingDefinition.IntRange(1, 3)));
    }

    @Override
    public void impactParticles(Level level, Vec3 pos, Vec3 motion, Vec3 drift) {
        FlameFanParticleOptions data =
                new FlameFanParticleOptions((float) (1.0 + level.getRandom().nextGaussian() * 0.2F), -0.1F, 0.7F);
        level.addParticle(data, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }
}
