package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.ai.CultistHurtByTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.CultistSquatGoal;
import com.leclowndu93150.thaumaturge.content.entity.cloak.CloakTracker;
import com.leclowndu93150.thaumaturge.content.entity.cloak.CloakWearer;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public abstract class EntityCultist extends Monster implements CloakWearer {
    public static final byte ARRIVAL_EVENT = 20;
    private static final float GEAR_DROP_CHANCE = 0.05F;
    private static final int ARRIVAL_PARTICLES = 20;
    private static final int DOOR_PRIORITY = 5;
    private static final int RESTRICTION_PRIORITY = 6;
    private static final int STROLL_PRIORITY = 7;
    private static final int LOOK_PRIORITY = 8;
    private static final double WALK_SPEED = 0.8;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int PLAYER_PRIORITY = 2;
    private static final int GUARDIAN_PRIORITY = 3;
    private static final int ILLAGER_PRIORITY = 4;
    private final CloakTracker cloak = new CloakTracker();

    protected EntityCultist(EntityType<? extends EntityCultist> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.setDropChance(EquipmentSlot.CHEST, GEAR_DROP_CHANCE);
        this.setDropChance(EquipmentSlot.FEET, GEAR_DROP_CHANCE);
        this.setDropChance(EquipmentSlot.HEAD, GEAR_DROP_CHANCE);
        this.setDropChance(EquipmentSlot.LEGS, GEAR_DROP_CHANCE);
    }

    public static AttributeSupplier.Builder createCultistAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    protected static AttributeSupplier.Builder createCultistAttributes(double maxHealth) {
        return createCultistAttributes().add(Attributes.MAX_HEALTH, maxHealth);
    }

    protected final void addIdleGoals() {
        this.goalSelector.addGoal(DOOR_PRIORITY, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(RESTRICTION_PRIORITY, new MoveTowardsRestrictionGoal(this, WALK_SPEED));
        this.goalSelector.addGoal(RESTRICTION_PRIORITY, new CultistSquatGoal(this));
        this.goalSelector.addGoal(STROLL_PRIORITY, new RandomStrollGoal(this, WALK_SPEED));
        this.goalSelector.addGoal(LOOK_PRIORITY, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        this.goalSelector.addGoal(LOOK_PRIORITY, new RandomLookAroundGoal(this));
    }

    protected final void addTargetGoals() {
        this.targetSelector.addGoal(RETALIATE_PRIORITY, new CultistHurtByTargetGoal(this));
        this.targetSelector.addGoal(PLAYER_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(
                GUARDIAN_PRIORITY, new NearestAttackableTargetGoal<>(this, EntityEldritchGuardian.class, true));
        this.targetSelector.addGoal(
                ILLAGER_PRIORITY, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public void tick() {
        cloak.tick(this);
        super.tick();
    }

    @Override
    public CloakTracker cloak() {
        return cloak;
    }

    protected abstract ResourceKey<LootTable> equipmentTable();

    @Override
    protected void populateDefaultEquipmentEnchantments(
            ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {}

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData groupData) {
        this.equip(equipmentTable(), Map.of());
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return entity instanceof EntityCultist || super.isAlliedTo(entity);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof EntityCultist) && super.canAttack(target);
    }

    public void spawnCultistArrivalParticles() {
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, ARRIVAL_EVENT);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == ARRIVAL_EVENT) {
            for (int i = 0; i < ARRIVAL_PARTICLES; i++) {
                double vx = this.random.nextGaussian() * 0.05;
                double vy = this.random.nextGaussian() * 0.05;
                double vz = this.random.nextGaussian() * 0.05;
                this.level()
                        .addParticle(
                                ParticleTypes.POOF,
                                this.getX()
                                        + this.random.nextFloat() * this.getBbWidth() * 2.0F
                                        - this.getBbWidth()
                                        + vx * 2.0,
                                this.getY() + this.random.nextFloat() * this.getBbHeight() + vy * 2.0,
                                this.getZ()
                                        + this.random.nextFloat() * this.getBbWidth() * 2.0F
                                        - this.getBbWidth()
                                        + vz * 2.0,
                                vx,
                                vy,
                                vz);
            }
        } else {
            super.handleEntityEvent(event);
        }
    }
}
