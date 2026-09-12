package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.particle.TaintSwarmParticleOptions;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EntityTaintSwarm extends Monster implements ITaintedMob {
    private static final EntityDataAccessor<Boolean> SUMMONED =
            SynchedEntityData.defineId(EntityTaintSwarm.class, EntityDataSerializers.BOOLEAN);

    private static final int ATTACK_RANGE = 3;
    private static final int ATTACK_COOLDOWN = 25;
    private static final int WEAKNESS_DURATION = 100;
    private static final float SELF_DAMAGE_SUMMONED = 5.0F;
    // TC4/TC5 rendered no swarm model: its body was a cloud of roughly thirty attached particles.
    private static final int SWARM_PARTICLES_PER_TICK = 1;

    private int damBonus;
    private int attackTicks;
    private BlockPos flightTarget;

    public EntityTaintSwarm(EntityType<? extends EntityTaintSwarm> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, false);
        this.xpReward = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                // Original swarms only acquired a nearby victim (12 blocks), rather than hunting
                // across the whole geyser activation radius.
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder data) {
        super.defineSynchedData(data);
        data.define(SUMMONED, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isSummoned() {
        return this.entityData.get(SUMMONED);
    }

    public void setSummoned(boolean summoned) {
        this.entityData.set(SUMMONED, summoned);
    }

    public void setDamBonus(int bonus) {
        this.damBonus = bonus;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setNoGravity(true);
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.6, motion.z);

        if (!(this.level() instanceof ServerLevel server)) {
            AABB box = this.getBoundingBox();
            for (int n = 0; n < SWARM_PARTICLES_PER_TICK; n++) {
                double x = box.minX + this.random.nextDouble() * (box.maxX - box.minX);
                double y = box.minY + this.random.nextDouble() * (box.maxY - box.minY);
                double z = box.minZ + this.random.nextDouble() * (box.maxZ - box.minZ);
                this.level().addParticle(new TaintSwarmParticleOptions(this.getId()), x, y, z, 0.0, 0.0, 0.0);
            }
            return;
        }
        if (attackTicks > 0) {
            attackTicks--;
        }
        LivingEntity target = this.getTarget();
        if (target == null) {
            if (isSummoned()) {
                this.hurt(server.damageSources().generic(), SELF_DAMAGE_SUMMONED);
                return;
            }
            updateTaintedFreeFlight(server);
            return;
        }

        // TC4 swarms actively flew toward their victim; merely assigning a target is insufficient
        // with a FlyingMoveControl and no pathing attack goal.
        this.moveControl.setWantedPosition(
                target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ(), 0.65);
        if (attackTicks > 0) {
            return;
        }
        double distSq = this.distanceToSqr(target);
        if (distSq < ATTACK_RANGE * ATTACK_RANGE) {
            attackTicks = ATTACK_COOLDOWN + this.random.nextInt(10);
            this.doHurtTarget(target);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_DURATION, 0, true, false, false));
        }
    }

    private void updateTaintedFreeFlight(ServerLevel server) {
        if (!isValidFlightTarget(server, flightTarget)
                || random.nextInt(30) == 0
                || flightTarget.distSqr(blockPosition()) < 4.0) {
            flightTarget = null;
            for (int attempt = 0; attempt < 12; attempt++) {
                BlockPos candidate = blockPosition()
                        .offset(
                                random.nextInt(7) - random.nextInt(7),
                                random.nextInt(6) - 2,
                                random.nextInt(7) - random.nextInt(7));
                if (isValidFlightTarget(server, candidate)) {
                    flightTarget = candidate;
                    break;
                }
            }
        }
        if (flightTarget != null) {
            moveControl.setWantedPosition(
                    flightTarget.getX() + 0.5, flightTarget.getY() + 0.1, flightTarget.getZ() + 0.5, 0.55);
        }
    }

    private static boolean isValidFlightTarget(ServerLevel level, BlockPos pos) {
        if (pos == null || !level.hasChunkAt(pos) || !level.getBlockState(pos).isAir()) {
            return false;
        }
        if (pos.getY() < level.getMinBuildHeight() + 1
                || pos.getY()
                        > level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) + 8) {
            return false;
        }
        return TaintBiomeManager.isTainted(level, pos);
    }

    public float effectiveAttackDamage() {
        return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + damBonus;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Summoned", isSummoned());
        output.putInt("DamBonus", damBonus);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        super.readAdditionalSaveData(input);
        setSummoned(input.getBoolean("Summoned"));
        damBonus = input.getInt("DamBonus");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.SWARM.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.SWARMATTACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.SWARMATTACK.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }
}
