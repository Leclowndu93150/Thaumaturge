package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.content.entity.ISidedHurt;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionModifier;
import com.leclowndu93150.thaumaturge.content.world.mound.BlockLoot;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityEldritchGolem extends EntityThaumaturgeBoss implements IEldritchMob, RangedAttackMob, ISidedHurt {
    private static final EntityDataAccessor<Boolean> DATA_HEADLESS =
            SynchedEntityData.defineId(EntityEldritchGolem.class, EntityDataSerializers.BOOLEAN);

    private static final byte ATTACK_EVENT = 4;
    private static final byte ARC_EVENT = 19;
    private static final int SPAWN_INVULN_TICKS = 100;
    private static final int BONUS_ARMOR = 6;
    private static final float MELEE_FACTOR = 0.75F;
    private static final double MELEE_KNOCKUP = 0.2;
    private static final float HEADLESS_FLING = 1.5F;
    private static final int ATTACK_COOLDOWN = 10;
    private static final int BEAM_CHARGE_MAX = 150;
    private static final float SOFT_BLOCK_HARDNESS = 0.15F;
    private static final float HEAD_EXPLOSION_POWER = 2.0F;
    private static final int ARC_DURATION_BASE = 8;
    private static final int ARC_COLOR = 0xA6FFFF;

    private int beamCharge;
    private boolean chargingBeam;
    private int arcing;
    private BlockPos arcTarget = BlockPos.ZERO;
    private int attackTimer;

    public EntityEldritchGolem(EntityType<? extends EntityEldritchGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ARMOR, BONUS_ARMOR);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_HEADLESS, false);
    }

    public boolean isHeadless() {
        return this.entityData.get(DATA_HEADLESS);
    }

    public void setHeadless(boolean headless) {
        this.entityData.set(DATA_HEADLESS, headless);
    }

    @Override
    public void generateName() {
        int mod = ChampionHelper.championType(this);
        if (mod >= 0) {
            this.setCustomName(Component.translatable(
                    "entity.thaumaturge.eldritch_golem.name.custom",
                    Component.translatable(
                            "champion.mod." + ChampionModifier.MODS.get(mod).name())));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("headless", this.isHeadless());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        super.readAdditionalSaveData(input);
        this.setHeadless(input.getBoolean("headless"));
        if (this.isHeadless()) {
            this.makeHeadless();
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData data) {
        this.spawnTimer = SPAWN_INVULN_TICKS;
        ChampionHelper.makeChampion(this, true);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackTimer > 0) {
            this.attackTimer--;
        }
        Vec3 movement = this.getDeltaMovement();
        if (movement.x * movement.x + movement.z * movement.z > 2.5000003E-7F && this.random.nextInt(5) == 0) {
            BlockState state = this.level().getBlockState(this.blockPosition());
            if (!state.isAir()) {
                this.level()
                        .addParticle(
                                new BlockParticleOption(ParticleTypes.BLOCK, state),
                                this.getX() + (this.random.nextFloat() - 0.5) * this.getBbWidth(),
                                this.getBoundingBox().minY + 0.1,
                                this.getZ() + (this.random.nextFloat() - 0.5) * this.getBbWidth(),
                                4.0 * (this.random.nextFloat() - 0.5),
                                0.5,
                                (this.random.nextFloat() - 0.5) * 4.0);
            }
            if (!this.level().isClientSide() && state.getBlock() instanceof BlockLoot) {
                this.level().destroyBlock(this.blockPosition(), true);
            }
        }
        if (!this.level().isClientSide()) {
            BlockState state = this.level().getBlockState(this.blockPosition());
            float hardness = state.getDestroySpeed(this.level(), this.blockPosition());
            if (!state.isAir() && hardness >= 0.0F && hardness <= SOFT_BLOCK_HARDNESS) {
                this.level().destroyBlock(this.blockPosition(), true);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return hurtSided(level(), source, damage);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (damage > this.getHealth() && !this.isHeadless() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.setHeadless(true);
            this.spawnTimer = SPAWN_INVULN_TICKS;
            double xx = Mth.cos(this.getYRot() % 360.0F / 180.0F * Mth.PI) * 0.75F;
            double zz = Mth.sin(this.getYRot() % 360.0F / 180.0F * Mth.PI) * 0.75F;
            level.explode(
                    this,
                    this.getX() + xx,
                    this.getY() + this.getEyeHeight(),
                    this.getZ() + zz,
                    HEAD_EXPLOSION_POWER,
                    false,
                    Level.ExplosionInteraction.NONE);
            this.makeHeadless();
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public boolean hurtClient(DamageSource source, float damage) {
        return super.hurtClient(source, damage);
    }

    public int getAttackTimer() {
        return this.attackTimer;
    }

    private void makeHeadless() {
        this.goalSelector.addGoal(2, new LongRangeAttackGoal(this, 3.0, 1.0, 5, 5, 24.0F));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        ServerLevel level = (ServerLevel) level();
        if (this.attackTimer > 0) {
            return false;
        }
        this.attackTimer = ATTACK_COOLDOWN;
        this.level().broadcastEntityEvent(this, ATTACK_EVENT);
        boolean hit = target.hurt(
                this.damageSources().mobAttack(this),
                (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * MELEE_FACTOR);
        if (hit) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, MELEE_KNOCKUP, 0.0));
            if (this.isHeadless()) {
                target.push(
                        -Mth.sin(this.getYRot() * Mth.PI / 180.0F) * HEADLESS_FLING,
                        0.1,
                        Mth.cos(this.getYRot() * Mth.PI / 180.0F) * HEADLESS_FLING);
            }
        }
        return hit;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (!this.hasLineOfSight(target) || this.chargingBeam || this.beamCharge <= 0) {
            return;
        }
        this.beamCharge -= 15 + this.random.nextInt(5);
        this.getLookControl()
                .setLookAt(
                        target.getX(),
                        target.getBoundingBox().minY + target.getBbHeight() / 2.0F,
                        target.getZ(),
                        30.0F,
                        30.0F);
        Vec3 look = this.getLookAngle();
        EntityGolemOrb blast = new EntityGolemOrb(this.level(), this, target, false);
        blast.setPos(blast.getX() + look.x, blast.getY(), blast.getZ() + look.z);
        double dx = target.getX() + target.getDeltaMovement().x - this.getX();
        double dy = target.getY() - this.getY() - target.getBbHeight() / 2.0F;
        double dz = target.getZ() + target.getDeltaMovement().z - this.getZ();
        blast.shoot(dx, dy, dz, 0.66F, 5.0F);
        this.playSound(TCSounds.EGATTACK.get(), 1.0F, 1.0F + this.random.nextFloat() * 0.1F);
        this.level().addFreshEntity(blast);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == ATTACK_EVENT) {
            this.attackTimer = ATTACK_COOLDOWN;
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.getSpawnTimer() > 0) {
                this.heal(2.0F);
            }
            if (this.isHeadless() && this.beamCharge <= 0) {
                this.chargingBeam = true;
            }
            if (this.isHeadless() && this.chargingBeam) {
                this.beamCharge++;
                if (this.tickCount % 5 == 0) {
                    this.sendJacobsArc((ServerLevel) this.level());
                }
                if (this.beamCharge == BEAM_CHARGE_MAX) {
                    this.chargingBeam = false;
                }
            }
        }
    }

    private void sendJacobsArc(ServerLevel level) {
        if (this.arcing > 0) {
            this.arcing--;
            Effects.arcLightning(level, this.position().add(0.0, this.getBbHeight() / 2.0, 0.0))
                    .to(Vec3.atBottomCenterOf(this.arcTarget.above()))
                    .color(ARC_COLOR)
                    .send();
            return;
        }
        float radius = 2.0F + this.random.nextFloat() * 2.0F;
        double radians = Math.toRadians(this.random.nextInt(360));
        int bx = Mth.floor(this.getX() + radius * Math.cos(radians));
        int by = Mth.floor(this.getY());
        int bz = Mth.floor(this.getZ() + radius * Math.sin(radians));
        BlockPos pos = new BlockPos(bx, by, bz);
        for (int step = 0; step < 5 && this.level().isEmptyBlock(pos); step++) {
            pos = pos.below();
        }
        if (this.level().isEmptyBlock(pos.above()) && !this.level().isEmptyBlock(pos)) {
            this.arcTarget = pos;
            this.arcing = ARC_DURATION_BASE + this.random.nextInt(5);
            this.playSound(
                    TCSounds.JACOBS.get(), 0.8F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        }
    }
}
