package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.entity.ai.ThaumicSlimeSpitGoal;
import com.leclowndu93150.thaumaturge.content.particle.FluxGooDropletParticleOptions;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.mixin.world.entity.monster.SlimeAccessor;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class ThaumicSlime extends Slime implements ITaintedMob {
    public static final int PARTICLE_COLOR = 0xB200FF;
    public static final float PARTICLE_ALPHA = 0.4F;

    private static final EntityDataAccessor<Integer> DATA_LAUNCHED = SynchedEntityData.defineId(ThaumicSlime.class, EntityDataSerializers.INT);
    private static final int LAUNCH_TICKS = 10;
    private static final int LAUNCHED_BONUS_REACH = 2;
    private static final float ATTACK_REACH_FACTOR = 0.6F;

    public ThaumicSlime(EntityType<? extends ThaumicSlime> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_LAUNCHED, LAUNCH_TICKS);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new ThaumicSlimeSpitGoal(this));
    }

    @Override
    public void setSize(int size, boolean updateHealth) {
        super.setSize(size, updateHealth);
        this.xpReward = size + 2;
    }

    @Override
    protected ParticleOptions getParticleType() {
        return new FluxGooDropletParticleOptions(PARTICLE_COLOR, PARTICLE_ALPHA, 0);
    }

    @Override
    protected float getAttackDamage() {
        return this.getSize() + 1.0F;
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        if (this.level() instanceof ServerLevel server && this.isAlive()) {
            int reach = this.getSize() + (isLaunched() ? LAUNCHED_BONUS_REACH : 0);
            double reachSq = ATTACK_REACH_FACTOR * reach * ATTACK_REACH_FACTOR * reach;
            if (this.distanceToSqr(target) < reachSq && this.hasLineOfSight(target)) {
                DamageSource source = this.damageSources().mobAttack(this);
                if (target.hurtServer(server, source, this.getAttackDamage())) {
                    this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                    EnchantmentHelper.doPostAttackEffects(server, target, source);
                }
            }
        }
    }

    @Override
    public void tick() {
        int size = getSize();
        SlimeAccessor accessor = (SlimeAccessor) (Object) this;
        boolean landing = this.onGround() && !accessor.thaumaturge$getWasOnGround();
        if (landing) {
            accessor.thaumaturge$setWasOnGround(true);
        }
        super.tick();
        if (landing) {
            if (this.level().isClientSide()) {
                spawnGooParticles(size, size * 2);
            }
            this.playSound(getJumpSound(), getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
            this.targetSquish = -0.5F;
        }
        int launched = this.entityData.get(DATA_LAUNCHED);
        if (launched > 0) {
            if (this.level().isClientSide()) {
                spawnGooParticles(size, size * (launched + 1));
            } else {
                this.entityData.set(DATA_LAUNCHED, launched - 1);
            }
        }
    }

    private void spawnGooParticles(int size, int count) {
        AABB box = this.getBoundingBox();
        double midY = (box.minY + box.maxY) / 2.0;
        for (int n = 0; n < count; n++) {
            float angle = this.random.nextFloat() * Mth.TWO_PI;
            float radius = this.random.nextFloat() * 0.5F + 0.5F;
            float ox = Mth.sin(angle) * size * 0.5F * radius;
            float oz = Mth.cos(angle) * size * 0.5F * radius;
            int lifetime = (int) (66.0F / (this.random.nextFloat() * 0.9F + 0.1F));
            this.level().addParticle(new FluxGooDropletParticleOptions(PARTICLE_COLOR, PARTICLE_ALPHA, lifetime), this.getX() + ox, midY, this.getZ() + oz, 0.0, 0.0, 0.0);
        }
    }

    public void markLaunched() {
        this.entityData.set(DATA_LAUNCHED, LAUNCH_TICKS);
    }

    public boolean isLaunched() {
        return this.entityData.get(DATA_LAUNCHED) > 0;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        int size = this.getSize();
        if (this.level() instanceof ServerLevel server && size > 1 && this.isDeadOrDying()) {
            for (int k = 0; k < size; k++) {
                float xd = (k % 2 - 0.5F) * size / 4.0F;
                float zd = (k / 2 - 0.5F) * size / 4.0F;
                ThaumicSlime child = TCEntities.THAUMIC_SLIME.get().create(server, EntitySpawnReason.TRIGGERED);
                if (child != null) {
                    child.setSize(1, true);
                    child.snapTo(this.getX() + xd, this.getY() + 0.5, this.getZ() + zd, this.random.nextFloat() * 360.0F, 0.0F);
                    server.addFreshEntity(child);
                }
            }
            this.setSize(1, false);
        }
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (this.getSize() > 1) {
            this.spawnAtLocation(level, EssentiaCrystalFactory.of(level.registryAccess(), TCAspects.VITIUM));
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        int sizeBits = 1 + level.getRandom().nextInt(3);
        int size = 1 << sizeBits;
        this.setSize(size, true);
        return groupData;
    }

    public static boolean checkSpawnRules(EntityType<ThaumicSlime> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return false;
    }

    public boolean canSpitAt(Player target) {
        return this.getSize() > 2 && this.distanceToSqr(target) > 4.0 * 4.0;
    }
}
