package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.network.ClientboundWarpFXPayload;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public class EntityEldritchGuardian extends Monster implements RangedAttackMob, IEldritchMob {
    private static final byte ARM_LIFT_LEFT_EVENT = 15;
    private static final byte ARM_LIFT_RIGHT_EVENT = 16;
    private static final float ARM_LIFT_BLAST = 0.5F;
    private static final float ARM_LIFT_DECAY = 0.05F;
    private static final float SCREECH_CHANCE = 0.15F;
    private static final int WITHER_TICKS = 400;
    private static final int FOG_INTERVAL_TICKS = 100;
    private static final double FOG_RANGE_HARD_SQ = 576.0;
    private static final double FOG_RANGE_SQ = 256.0;
    private static final float ORB_SPEED = 1.1F;
    private static final float ORB_SPREAD = 2.0F;

    public float armLiftL;
    public float armLiftR;
    private boolean lastBlast;

    public EntityEldritchGuardian(EntityType<? extends EntityEldritchGuardian> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 4.0);
    }

    public static boolean checkGuardianSpawnRules(EntityType<EntityEldritchGuardian> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        boolean alone = level.getEntitiesOfClass(EntityEldritchGuardian.class, new AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty();
        return alone && Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new LongRangeAttackGoal(this, 8.0, 1.0, 20, 40, 24.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
            damage /= 2.0F;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.armLiftL > 0.0F) {
                this.armLiftL -= ARM_LIFT_DECAY;
            }
            if (this.armLiftR > 0.0F) {
                this.armLiftR -= ARM_LIFT_DECAY;
            }
        } else if ((this.tickCount == 0 || this.tickCount % FOG_INTERVAL_TICKS == 0) && this.level().getDifficulty() != Difficulty.EASY) {
            double rangeSq = this.level().getDifficulty() == Difficulty.HARD ? FOG_RANGE_HARD_SQ : FOG_RANGE_SQ;
            for (Player player : this.level().players()) {
                if (player.isAlive() && player instanceof ServerPlayer serverPlayer && player.distanceToSqr(this) < rangeSq) {
                    PacketDistributor.sendToPlayer(serverPlayer, ClientboundWarpFXPayload.mistShort());
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean result = super.doHurtTarget(level, target);
        if (result) {
            int difficultyId = this.level().getDifficulty().getId();
            if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < difficultyId * 0.3F) {
                target.igniteForSeconds(2 * difficultyId);
            }
        }
        return result;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.random.nextFloat() > SCREECH_CHANCE) {
            EntityEldritchOrb blast = new EntityEldritchOrb(this.level(), this);
            this.lastBlast = !this.lastBlast;
            this.level().broadcastEntityEvent(this, this.lastBlast ? ARM_LIFT_RIGHT_EVENT : ARM_LIFT_LEFT_EVENT);
            int rr = this.lastBlast ? 90 : 180;
            double xx = Mth.cos((this.getYRot() + rr) % 360.0F / 180.0F * (float) Math.PI) * 0.5F;
            double zz = Mth.sin((this.getYRot() + rr) % 360.0F / 180.0F * (float) Math.PI) * 0.5F;
            blast.setPos(blast.getX() - xx, blast.getY(), blast.getZ() - zz);
            Vec3 v = target.position().add(target.getDeltaMovement().scale(10.0)).subtract(this.position()).normalize();
            blast.shoot(v.x, v.y, v.z, ORB_SPEED, ORB_SPREAD);
            this.playSound(TCSounds.EGATTACK.get(), 2.0F, 1.0F + this.random.nextFloat() * 0.1F);
            this.level().addFreshEntity(blast);
        } else if (this.hasLineOfSight(target)) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, 0));
            if (target instanceof ServerPlayer player) {
                WarpHelper.addWarp(player, 1 + this.random.nextInt(3), WarpType.TEMPORARY);
            }
            this.playSound(TCSounds.EGSCREECH.get(), 3.0F, 1.0F + this.random.nextFloat() * 0.1F);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == ARM_LIFT_LEFT_EVENT) {
            this.armLiftL = ARM_LIFT_BLAST;
        } else if (event == ARM_LIFT_RIGHT_EVENT) {
            this.armLiftR = ARM_LIFT_BLAST;
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.hasHome();
    }

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        return entity instanceof IEldritchMob || super.considersEntityAsAlly(entity);
    }

    @Override
    protected float getSoundVolume() {
        return 1.5F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return TCSounds.EGIDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.EGDEATH.get();
    }
}
