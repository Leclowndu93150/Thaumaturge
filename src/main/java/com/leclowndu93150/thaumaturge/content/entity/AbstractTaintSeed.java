package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.api.taint.ITaintOrigin;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintFeeding;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintOriginRegistry;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTaintSeed extends AbstractRootedTaint implements ITaintOrigin {
    private static final int SPREAD_INTERVAL = 20;
    private static final int AMBIENT_FUMES = 3;
    private static final double AMBIENT_FUME_RISE = 0.015;
    private static final float AMBIENT_FUME_SCALE = 1.5F;
    private static final float TICKS_PER_SECOND = 20.0F;
    private static final float FLUX_TAINT_RADIUS_MULT = 4.0F;
    private static final int FLUX_TAINT_TICKS = 100;
    private static final float ATTACK_ANIM_START = 0.5F;
    private static final float ATTACK_ANIM_DECAY = 0.75F;
    private static final float ATTACK_ANIM_EPSILON = 0.001F;
    private static final double FOLLOW_RANGE = 16.0;
    private static final String RESERVE_KEY = "taint_reserve";
    private static final String STARVE_KEY = "starve_ticks";

    public float attackAnim;

    private @Nullable BlockPos registeredPosition;
    private float reserve;
    private int starveTicks;

    protected AbstractTaintSeed(EntityType<? extends AbstractTaintSeed> type, Level level) {
        super(type, level);
    }

    public abstract int getArea();

    public static AttributeSupplier.Builder createSeedAttributes(double maxHealth, double attackDamage) {
        return createRootedAttributes(maxHealth, attackDamage, FOLLOW_RANGE);
    }

    @Override
    protected void onStrike(ServerLevel level) {
        this.playSound(TTSounds.TENTACLE.get(), this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    protected void startStrikeAnimation() {
        this.attackAnim = ATTACK_ANIM_START;
    }

    @Override
    protected void tickStrikeAnimation() {
        if (this.attackAnim > 0.0F) {
            this.attackAnim *= ATTACK_ANIM_DECAY;
            if (this.attackAnim < ATTACK_ANIM_EPSILON) {
                this.attackAnim = 0.0F;
            }
        }
    }

    @Override
    protected void rootedServerStep(ServerLevel server) {
        BlockPos position = this.blockPosition();
        if (!position.equals(registeredPosition)) {
            TaintOriginRegistry.get(server).move(registeredPosition, position);
            registeredPosition = position.immutable();
        }
        if (this.tickCount % SPREAD_INTERVAL != 0) {
            return;
        }
        for (int i = 0; i < AMBIENT_FUMES; i++) {
            Effects.taint(
                            server,
                            this.position()
                                    .add(
                                            (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                                            this.random.nextDouble() * this.getBbHeight(),
                                            (this.random.nextDouble() - 0.5) * this.getBbWidth()))
                    .motion(0.0, AMBIENT_FUME_RISE + this.random.nextDouble() * AMBIENT_FUME_RISE, 0.0)
                    .scale(AMBIENT_FUME_SCALE)
                    .send();
        }
        BlockPos pos = this.blockPosition();
        TaintEcology.touchActiveSeed(server, pos);
        TaintBiomeManager.taintColumn(server, pos);
        TaintFeeding.feed(server, pos, this, SPREAD_INTERVAL / TICKS_PER_SECOND);
        if (TaintFeeding.isDormant(this)) {
            starve(server);
        } else {
            this.starveTicks = 0;
            spreadFibres(server, pos);
        }
        if (!this.isAlive()) {
            return;
        }
        applyAuraTouch(server);
    }

    private void spreadFibres(ServerLevel server, BlockPos pos) {
        int area = getArea();
        int attempts = TaintFeeding.growthAttempts(this);
        for (int attempt = 0; attempt < attempts; attempt++) {
            int dx = Mth.nextInt(server.getRandom(), -area * 3, area * 3);
            int dy = Mth.nextInt(server.getRandom(), -area, area);
            int dz = Mth.nextInt(server.getRandom(), -area * 3, area * 3);
            TaintHelper.growFromOrigin(server, pos.offset(dx, dy, dz), this);
        }
    }

    private void starve(ServerLevel server) {
        int limit = ThaumaturgeCommonConfig.TAINT_STARVE_TICKS.get();
        this.starveTicks += SPREAD_INTERVAL;
        float damage = this.getMaxHealth() * SPREAD_INTERVAL / limit;
        this.hurt(server.damageSources().starve(), this.starveTicks >= limit ? this.getMaxHealth() : damage);
    }

    @Override
    public boolean isLiveTaintOrigin() {
        return this.isAlive();
    }

    @Override
    public float getTaintReserve() {
        return this.reserve;
    }

    @Override
    public void setTaintReserve(float reserve) {
        this.reserve = Math.max(0.0F, reserve);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(RESERVE_KEY, this.reserve);
        tag.putInt(STARVE_KEY, this.starveTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.reserve = Math.max(0.0F, tag.getFloat(RESERVE_KEY));
        this.starveTicks = tag.getInt(STARVE_KEY);
    }

    private void applyAuraTouch(ServerLevel server) {
        double radius = getArea() * FLUX_TAINT_RADIUS_MULT;
        for (LivingEntity target : server.getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox().inflate(radius), e -> canAfflict(this, e))) {
            target.addEffect(new MobEffectInstance(
                    TTMobEffects.FLUX_TAINT, FLUX_TAINT_TICKS, Math.max(0, getArea() - 1), true, false, false));
        }
    }

    static boolean canAfflict(LivingEntity seed, LivingEntity target) {
        return target != seed && target.isAlive() && !target.isSpectator() && !MobTraits.isTainted(target);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel server && registeredPosition != null) {
            TaintOriginRegistry.get(server).remove(registeredPosition);
            registeredPosition = null;
        }
        super.remove(reason);
    }
}
