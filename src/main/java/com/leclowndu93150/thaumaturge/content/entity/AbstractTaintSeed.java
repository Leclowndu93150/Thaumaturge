package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.particle.LightningFlashParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.TaintFumeParticleOptions;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public abstract class AbstractTaintSeed extends AbstractRootedTaint {
    private static final double FOLLOW_RANGE = 16.0;
    private static final int PULSE_INTERVAL = 20;
    private static final int FUMES_PER_PULSE = 3;
    private static final int FUME_COLOR = 0xFF7B2A9E;
    private static final float FUME_SCALE = 0.9F;
    private static final double FUME_RISE = 0.04;
    private static final double BODY_HALF = 0.5;
    private static final float STARVATION_DAMAGE = 0.5F;
    private static final float STARVATION_FLUX = 0.1F;
    private static final int MAX_SPREAD_ATTEMPTS = 4;
    private static final float ATTEMPTS_PER_SATURATION = 2.0F;
    private static final int SPREAD_HORIZONTAL_PER_AREA = 3;
    private static final double AFFLICTION_REACH_PER_AREA = 4.0;
    private static final int AFFLICTION_TICKS = 100;
    private static final double AMBUSH_REACH_PER_ROOT_AREA = 16.0;
    private static final double AMBUSH_MIN_REACH = 0.0;
    private static final float STRIKE_AGITATION = 1.0F;
    private static final float AGITATION_EASE = 0.82F;
    private static final float AGITATION_REST = 0.01F;
    private static final float HURT_TICKS = 10.0F;
    private static final double CROWN_LIFT = 0.15;
    private static final double CROWN_WANDER_RADIUS = 0.12;
    private static final double CROWN_STIR_PUSH = 0.3;
    private static final float CROWN_WANDER_SPEED = 0.06F;
    private static final int CROWN_MOTE_COLOR = 0xFF8E35B5;
    private static final float CROWN_MOTE_SCALE = 0.45F;
    private static final double CROWN_MOTE_RISE = 0.035;
    private static final double CROWN_MOTE_DRIFT = 0.006;
    private static final int CROWN_FLASH_ONE_IN = 30;
    private static final int CROWN_FLASH_COLOR = 0xFFB46CFF;
    private static final float CROWN_FLASH_ALPHA = 0.85F;
    private static final float CROWN_FLASH_SCALE_PER_AREA = 2.5F;

    public float attackAnim;
    private int headStartTicks;
    private @Nullable BlockPos anchor;

    protected AbstractTaintSeed(EntityType<? extends AbstractTaintSeed> type, Level level) {
        super(type, level);
    }

    public abstract int getArea();

    public static AttributeSupplier.Builder createSeedAttributes(double maxHealth, double attackDamage) {
        return createRootedAttributes(maxHealth, attackDamage, FOLLOW_RANGE);
    }

    public void grantHeadStart(int ticks) {
        this.headStartTicks = Math.max(this.headStartTicks, ticks);
    }

    @Override
    protected void rootedServerStep(ServerLevel level) {
        trackAnchor(level);
        if (!this.isAlive()) {
            return;
        }
        if (this.headStartTicks > 0) {
            this.headStartTicks--;
            spreadFibres(level, MAX_SPREAD_ATTEMPTS);
        }
        if (this.tickCount % PULSE_INTERVAL == 0) {
            pulse(level);
        }
    }

    private void trackAnchor(ServerLevel level) {
        BlockPos here = this.blockPosition();
        if (here.equals(this.anchor)) {
            return;
        }
        if (this.anchor != null) {
            TaintHelper.unregisterSeedAnchor(level, this.anchor);
        }
        this.anchor = here;
        TaintHelper.registerSeedAnchor(level, here);
    }

    private void pulse(ServerLevel level) {
        BlockPos here = this.blockPosition();
        releaseFumes(level);
        TaintEcology.touchActiveSeed(level, here);
        TaintBiomeManager.taintColumn(level, here);
        if (this.headStartTicks <= 0) {
            feedOrStarve(level, here);
        }
        if (this.isAlive()) {
            afflictSurroundings(level);
            TaintacleAmbush.tryAmbush(level, this, AMBUSH_MIN_REACH, AMBUSH_REACH_PER_ROOT_AREA * Math.sqrt(getArea()));
        }
    }

    private void releaseFumes(ServerLevel level) {
        TaintFumeParticleOptions fume = new TaintFumeParticleOptions(FUME_COLOR, FUME_SCALE);
        for (int i = 0; i < FUMES_PER_PULSE; i++) {
            level.sendParticles(fume, this.getRandomX(BODY_HALF), this.getRandomY(), this.getRandomZ(BODY_HALF), 0, 0.0, FUME_RISE, 0.0, 1.0);
        }
    }

    private void feedOrStarve(ServerLevel level, BlockPos here) {
        float saturation = AuraHelper.getFluxSaturation(level, here);
        if (saturation <= 0.0F) {
            this.hurtServer(level, level.damageSources().starve(), STARVATION_DAMAGE);
            AuraHelper.polluteAura(level, here, STARVATION_FLUX, false);
            return;
        }
        spreadFibres(level, Math.min(MAX_SPREAD_ATTEMPTS, 1 + Mth.floor(saturation * ATTEMPTS_PER_SATURATION)));
    }

    private void spreadFibres(ServerLevel level, int attempts) {
        int vertical = getArea();
        int horizontal = SPREAD_HORIZONTAL_PER_AREA * vertical;
        BlockPos centre = this.blockPosition();
        RandomSource random = this.getRandom();
        for (int i = 0; i < attempts; i++) {
            BlockPos spot = centre.offset(evenOffset(random, horizontal), evenOffset(random, vertical), evenOffset(random, horizontal));
            TaintHelper.attemptFibreGrowth(level, spot, true);
        }
    }

    private void afflictSurroundings(ServerLevel level) {
        int area = getArea();
        AABB reach = this.getBoundingBox().inflate(AFFLICTION_REACH_PER_AREA * area);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, reach, this::canAfflict)) {
            victim.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, AFFLICTION_TICKS, area - 1, true, false, false), this);
        }
    }

    private boolean canAfflict(LivingEntity victim) {
        return victim != this && victim.isAlive() && !victim.isSpectator() && !MobTraits.isTainted(victim);
    }

    private static int evenOffset(RandomSource random, int reach) {
        return random.nextInt(reach * 2 + 1) - reach;
    }

    @Override
    protected void onStrike(ServerLevel level) {
        this.playSound(TTSounds.TENTACLE.get(), this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    protected void startStrikeAnimation() {
        this.attackAnim = STRIKE_AGITATION;
    }

    @Override
    protected void tickStrikeAnimation() {
        this.attackAnim = this.attackAnim > AGITATION_REST ? this.attackAnim * AGITATION_EASE : 0.0F;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.isAlive()) {
            stirCrown();
        }
    }

    private void stirCrown() {
        float stir = Math.max(this.attackAnim, Mth.clamp(this.hurtTime / HURT_TICKS, 0.0F, 1.0F));
        double radius = (CROWN_WANDER_RADIUS + CROWN_STIR_PUSH * stir) * getArea();
        float angle = this.tickCount * CROWN_WANDER_SPEED;
        double x = this.getX() + Mth.cos(angle) * radius;
        double y = this.getY() + this.getBbHeight() + CROWN_LIFT;
        double z = this.getZ() + Mth.sin(angle) * radius;
        RandomSource random = this.getRandom();
        Level level = this.level();
        if (random.nextInt(CROWN_FLASH_ONE_IN) == 0) {
            level.addParticle(new LightningFlashParticleOptions(CROWN_FLASH_COLOR, CROWN_FLASH_ALPHA, CROWN_FLASH_SCALE_PER_AREA * getArea()), x, y, z, 0.0, 0.0, 0.0);
            return;
        }
        level.addParticle(new TaintFumeParticleOptions(CROWN_MOTE_COLOR, CROWN_MOTE_SCALE), x, y, z, random.nextGaussian() * CROWN_MOTE_DRIFT, CROWN_MOTE_RISE,
                random.nextGaussian() * CROWN_MOTE_DRIFT);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    public void onRemoval(RemovalReason reason) {
        super.onRemoval(reason);
        if (this.level() instanceof ServerLevel server && this.anchor != null) {
            TaintHelper.unregisterSeedAnchor(server, this.anchor);
            this.anchor = null;
        }
    }
}
