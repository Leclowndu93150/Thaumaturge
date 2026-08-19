package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityGolemOrb extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> DATA_RED = SynchedEntityData.defineId(EntityGolemOrb.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TARGET = SynchedEntityData.defineId(EntityGolemOrb.class, EntityDataSerializers.INT);
    private static final int RED_LIFE_TICKS = 240;
    private static final int WHITE_LIFE_TICKS = 160;
    private static final float RED_DAMAGE_FACTOR = 1.0F;
    private static final float WHITE_DAMAGE_FACTOR = 0.6F;
    private static final double HOMING_ACCEL = 0.2;
    private static final float HOMING_CLAMP = 0.25F;
    private static final float EYE_OFFSET = 0.1F;
    private static final int NO_TARGET = -1;

    private @Nullable LivingEntity target;

    public EntityGolemOrb(EntityType<? extends EntityGolemOrb> type, Level level) {
        super(type, level);
    }

    public EntityGolemOrb(Level level, LivingEntity shooter, @Nullable LivingEntity target, boolean red) {
        super(TCEntities.GOLEM_ORB.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - EYE_OFFSET, shooter.getZ());
        this.target = target;
        this.entityData.set(DATA_RED, red);
        this.entityData.set(DATA_TARGET, target == null ? NO_TARGET : target.getId());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_RED, false);
        entityData.define(DATA_TARGET, NO_TARGET);
    }

    public boolean isRed() {
        return this.entityData.get(DATA_RED);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > (this.isRed() ? RED_LIFE_TICKS : WHITE_LIFE_TICKS)) {
            this.discard();
        }
        if (this.target == null) {
            int id = this.entityData.get(DATA_TARGET);
            if (id != NO_TARGET && this.level().getEntity(id) instanceof LivingEntity living) {
                this.target = living;
            }
        }
        if (this.target != null && this.target.isAlive()) {
            double distSq = this.distanceToSqr(this.target);
            if (distSq > 0.0) {
                double dx = (this.target.getX() - this.getX()) / distSq;
                double dy = (this.target.getBoundingBox().minY + this.target.getBbHeight() * 0.6 - this.getY()) / distSq;
                double dz = (this.target.getZ() - this.getZ()) / distSq;
                Vec3 movement = this.getDeltaMovement().add(dx * HOMING_ACCEL, dy * HOMING_ACCEL, dz * HOMING_ACCEL);
                this.setDeltaMovement(Mth.clamp((float) movement.x, -HOMING_CLAMP, HOMING_CLAMP), Mth.clamp((float) movement.y, -HOMING_CLAMP, HOMING_CLAMP),
                        Mth.clamp((float) movement.z, -HOMING_CLAMP, HOMING_CLAMP));
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.level() instanceof ServerLevel server) {
            if (this.getOwner() instanceof LivingEntity owner && result instanceof EntityHitResult entityHit) {
                float damage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * (this.isRed() ? RED_DAMAGE_FACTOR : WHITE_DAMAGE_FACTOR);
                entityHit.getEntity().hurtServer(server, this.damageSources().indirectMagic(this, owner), damage);
            }
            this.playSound(TCSounds.SHOCK.get(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() != null) {
            Vec3 look = source.getEntity().getLookAngle();
            this.setDeltaMovement(look.scale(0.9));
            this.playSound(TCSounds.ZAP.get(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
            return true;
        }
        return false;
    }
}
