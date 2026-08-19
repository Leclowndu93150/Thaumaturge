package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityEldritchOrb extends ThrowableProjectile {
    private static final int MAX_LIFE_TICKS = 100;
    private static final double BLAST_RANGE = 2.0;
    private static final float DAMAGE_FACTOR = 0.666F;
    private static final int WEAKNESS_TICKS = 160;
    private static final float EYE_OFFSET = 0.1F;

    public EntityEldritchOrb(EntityType<? extends EntityEldritchOrb> type, Level level) {
        super(type, level);
    }

    public EntityEldritchOrb(Level level, LivingEntity shooter) {
        super(TCEntities.ELDRITCH_ORB.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - EYE_OFFSET, shooter.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {}

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > MAX_LIFE_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!(this.level() instanceof ServerLevel server) || !(this.getOwner() instanceof LivingEntity owner)) {
            return;
        }
        float damage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * DAMAGE_FACTOR;
        DamageSource source = this.damageSources().indirectMagic(this, owner);
        for (Entity entity : this.level().getEntities(owner, this.getBoundingBox().inflate(BLAST_RANGE))) {
            if (entity instanceof LivingEntity living && !living.isInvertedHealAndHarm()) {
                living.hurtServer(server, source, damage);
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS));
            }
        }
        this.playSound(SoundEvents.LAVA_EXTINGUISH, 0.5F, 2.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.8F);
        this.discard();
    }
}
