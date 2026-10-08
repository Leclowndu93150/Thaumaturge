package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.portal.CultistPortals;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityCultistPortalLesser extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE =
            SynchedEntityData.defineId(EntityCultistPortalLesser.class, EntityDataSerializers.BOOLEAN);

    private static final double ACTIVATION_RANGE = 32.0;
    private static final double MINION_SCAN_RANGE = 32.0;
    private static final int STAGE_BASE_TICKS = 50;
    private static final float TOUCH_DAMAGE = 4.0F;
    private static final float DEATH_EXPLOSION_POWER = 1.5F;

    private int stageCounter = 100;
    public int activeCounter;
    public int pulse;

    public EntityCultistPortalLesser(EntityType<? extends EntityCultistPortalLesser> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_ACTIVE, false);
    }

    public boolean isActive() {
        return this.entityData.get(DATA_ACTIVE);
    }

    public void setActive(boolean active) {
        this.entityData.set(DATA_ACTIVE, active);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void move(MoverType type, Vec3 movement) {}

    @Override
    public void aiStep() {
        if (this.pulse > 0) {
            this.pulse--;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isActive()) {
            this.activeCounter++;
        }
        if (this.level().isClientSide()) {
            return;
        }
        if (!this.isActive()) {
            if (this.tickCount % 10 == 0 && this.level().getNearestPlayer(this, ACTIVATION_RANGE) != null) {
                this.setActive(true);
                this.playSound(TTSounds.CRAFTSTART.get(), 1.0F, 1.0F);
            }
        } else if (this.stageCounter-- <= 0) {
            Player player = this.level().getNearestPlayer(this, ACTIVATION_RANGE);
            if (player != null && this.hasLineOfSight(player)) {
                int count =
                        switch (this.level().getDifficulty()) {
                            case HARD -> 6;
                            case NORMAL -> 4;
                            default -> 2;
                        };
                count -= CultistPortals.cultistsNear(this, MINION_SCAN_RANGE);
                if (count > 0) {
                    this.level().broadcastEntityEvent(this, CultistPortals.PULSE_EVENT);
                    this.spawnMinion();
                }
            }
            this.stageCounter = STAGE_BASE_TICKS + this.random.nextInt(STAGE_BASE_TICKS);
        }
    }

    private void spawnMinion() {
        ServerLevel server = (ServerLevel) this.level();
        EntityCultist cultist = CultistPortals.rollMinion(server, this.random);
        if (cultist == null) {
            return;
        }
        CultistPortals.summon(this, server, cultist);
        this.hurt(this.damageSources().fellOutOfWorld(), 5 + this.random.nextInt(5));
    }

    @Override
    public void playerTouch(Player player) {
        CultistPortals.touch(this, player, TOUCH_DAMAGE);
    }

    @Override
    public void die(DamageSource source) {
        CultistPortals.collapse(this, DEATH_EXPLOSION_POWER);
        super.die(source);
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, @Nullable Entity source) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        super.addAdditionalSaveData(output);
        output.putBoolean("active", this.isActive());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        super.readAdditionalSaveData(input);
        this.setActive((input.contains("active") ? input.getBoolean("active") : false));
    }

    @Override
    protected float getSoundVolume() {
        return CultistPortals.SOUND_VOLUME;
    }

    @Override
    public int getAmbientSoundInterval() {
        return CultistPortals.AMBIENT_INTERVAL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.MONOLITH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TTSounds.ZAP.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.SHOCK.get();
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == CultistPortals.PULSE_EVENT) {
            this.pulse = CultistPortals.PULSE_TICKS;
        } else {
            super.handleEntityEvent(event);
        }
    }
}
