package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class EntityInhabitedZombie extends Zombie implements IEldritchMob {
    private static final float GEAR_CHANCE_HARD = 0.9F;
    private static final float GEAR_CHANCE = 0.6F;
    private static final int BURST_PARTICLES = 20;

    public EntityInhabitedZombie(EntityType<? extends EntityInhabitedZombie> type, Level level) {
        super(type, level);
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    public static boolean checkInhabitedSpawnRules(EntityType<EntityInhabitedZombie> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        boolean alone = level.getEntitiesOfClass(EntityInhabitedZombie.class, new AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty();
        return alone && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity killed, DamageSource source) {
        return false;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        float gearChance = level.getDifficulty() == Difficulty.HARD ? GEAR_CHANCE_HARD : GEAR_CHANCE;
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CRIMSON_PLATE_HELM.get()));
        if (this.random.nextFloat() <= gearChance) {
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.CRIMSON_PLATE_CHEST.get()));
        }
        if (this.random.nextFloat() <= gearChance) {
            this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.CRIMSON_PLATE_LEGS.get()));
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void tickDeath() {
        if (this.level() instanceof ServerLevel server) {
            EntityEldritchCrab crab = TCEntities.ELDRITCH_CRAB.get().create(server, EntitySpawnReason.CONVERSION);
            if (crab != null) {
                crab.snapTo(this.getX(), this.getY() + this.getEyeHeight(), this.getZ(), this.getYRot(), this.getXRot());
                crab.setHelm(true);
                server.addFreshEntity(crab);
            }
            if (server.getGameRules().get(GameRules.MOB_DROPS) && this.shouldDropExperience()) {
                ExperienceOrb.award(server, this.position(), this.getExperienceReward(server, this.getLastHurtByPlayer()));
            }
            server.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), BURST_PARTICLES, this.getBbWidth() / 2.0, this.getBbHeight() / 4.0,
                    this.getBbWidth() / 2.0, 0.02);
        }
        this.discard();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.CRABTALK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }
}
