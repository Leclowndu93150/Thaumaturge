package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.AltarFocusGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.CultistHurtByTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class EntityCultistCleric extends EntityCultist implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_RITUALIST = SynchedEntityData.defineId(EntityCultistCleric.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> DATA_RITUAL_ANCHOR = SynchedEntityData.defineId(EntityCultistCleric.class, EntityDataSerializers.BLOCK_POS);

    private static final float RITUAL_PITCH_STEP = 10.0F;
    private static final double RITUAL_ANCHOR_HEIGHT = 1.5;

    private static final float BOOTS_CHANCE_HARD = 0.3F;
    private static final float BOOTS_CHANCE = 0.1F;
    private static final float ORB_CHANCE = 0.34F;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_SPREAD = 3.0F;
    private static final int FIREBALL_COUNT = 3;
    private static final int FIREBALL_LEVEL_EVENT = 1009;

    public int rage;

    public EntityCultistCleric(EntityType<? extends EntityCultistCleric> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createCultistAttributes().add(Attributes.MAX_HEALTH, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AltarFocusGoal(this));
        this.goalSelector.addGoal(2, new LongRangeAttackGoal(this, 2.0, 1.0, 20, 40, 24.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new CultistHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityEldritchGuardian.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_RITUALIST, false);
        entityData.define(DATA_RITUAL_ANCHOR, BlockPos.ZERO);
    }

    public boolean isRitualist() {
        return this.entityData.get(DATA_RITUALIST);
    }

    public void setRitualist(boolean ritualist) {
        this.entityData.set(DATA_RITUALIST, ritualist);
        if (ritualist && this.hasHome()) {
            this.entityData.set(DATA_RITUAL_ANCHOR, this.getHomePosition());
        }
    }

    public BlockPos ritualAnchor() {
        return this.entityData.get(DATA_RITUAL_ANCHOR);
    }

    @Override
    protected void setLoot(DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CRIMSON_ROBE_HELM.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.CRIMSON_ROBE_CHEST.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.CRIMSON_ROBE_LEGS.get()));
        float bootsChance = this.level().getDifficulty() == Difficulty.HARD ? BOOTS_CHANCE_HARD : BOOTS_CHANCE;
        if (this.random.nextFloat() < bootsChance) {
            this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.CRIMSON_BOOTS.get()));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        double dx = target.getX() - this.getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
        double dz = target.getZ() - this.getZ();
        this.swing(this.getUsedItemHand());
        if (this.random.nextFloat() > 1.0F - ORB_CHANCE) {
            EntityGolemOrb blast = new EntityGolemOrb(this.level(), this, target, true);
            Vec3 v = target.position().add(target.getDeltaMovement().scale(10.0)).subtract(this.position()).normalize();
            blast.setPos(blast.getX() + v.x, blast.getY() + v.y, blast.getZ() + v.z);
            blast.shoot(v.x, v.y, v.z, ORB_SPEED, ORB_SPREAD);
            this.playSound(TCSounds.EGATTACK.get(), 1.0F, 1.0F + this.random.nextFloat() * 0.1F);
            this.level().addFreshEntity(blast);
        } else {
            float spread = Mth.sqrt(velocity) * 0.5F;
            this.level().levelEvent(null, FIREBALL_LEVEL_EVENT, this.blockPosition(), 0);
            for (int i = 0; i < FIREBALL_COUNT; i++) {
                Vec3 shot = new Vec3(dx + this.random.nextGaussian() * spread, dy, dz + this.random.nextGaussian() * spread);
                SmallFireball fireball = new SmallFireball(this.level(), this, shot.normalize());
                fireball.setPos(fireball.getX(), this.getY() + this.getBbHeight() / 2.0F + 0.5, fireball.getZ());
                this.level().addFreshEntity(fireball);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.isRitualist();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
        this.setRitualist(false);
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.isRitualist()) {
            faceRitualAnchor();
        }
        if (!this.level().isClientSide() && this.isRitualist() && this.rage >= 5) {
            this.setRitualist(false);
        }
    }

    private void faceRitualAnchor() {
        BlockPos anchor = ritualAnchor();
        double dx = anchor.getX() + 0.5 - this.getX();
        double dy = anchor.getY() + RITUAL_ANCHOR_HEIGHT - (this.getY() + this.getEyeHeight());
        double dz = anchor.getZ() + 0.5 - this.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(dy, horizontal) * 180.0 / Math.PI));
        this.setXRot(rotateTowards(this.getXRot(), pitch, RITUAL_PITCH_STEP));
        this.yHeadRot = rotateTowards(this.yHeadRot, yaw, this.getMaxHeadXRot());
    }

    private static float rotateTowards(float current, float target, float step) {
        float delta = Mth.wrapDegrees(target - current);
        return current + Mth.clamp(delta, -step, step);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setRitualist(input.getBooleanOr("ritualist", false));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("ritualist", this.isRitualist());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.CHANT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }
}
