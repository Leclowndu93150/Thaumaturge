package com.leclowndu93150.thaumaturge.content.entity.projectile;

import com.leclowndu93150.thaumaturge.mixin.server.network.ServerGamePacketListenerImplAccessor;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class EntityGrapple extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> PULLING = SynchedEntityData.defineId(EntityGrapple.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MAIN_HAND = SynchedEntityData.defineId(EntityGrapple.class, EntityDataSerializers.BOOLEAN);

    private static final int FLIGHT_TIMEOUT = 30;
    private static final double PULL_DIVISOR = 5.0;
    private static final double PULL_CAP = 0.25;
    private static final double LIFT_BONUS = 0.033;
    private static final float LAUNCH_BOOST = 0.4F;
    private static final float FALLING_GRAVITY = 0.03F;

    private boolean boost;
    private boolean added;
    public float ampl;

    public EntityGrapple(EntityType<? extends EntityGrapple> type, Level level) {
        super(type, level);
    }

    public EntityGrapple(EntityType<? extends EntityGrapple> type, Level level, LivingEntity thrower, InteractionHand hand) {
        super(type, thrower.getX(), thrower.getEyeY() - 0.1, thrower.getZ(), level);
        setOwner(thrower);
        entityData.set(MAIN_HAND, hand == InteractionHand.MAIN_HAND);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(PULLING, false);
        entityData.define(MAIN_HAND, true);
    }

    @Override
    protected double getDefaultGravity() {
        return isPulling() ? 0.0 : FALLING_GRAVITY;
    }

    public boolean isPulling() {
        return entityData.get(PULLING);
    }

    private void setPulling() {
        entityData.set(PULLING, true);
    }

    public InteractionHand getHand() {
        return entityData.get(MAIN_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public void tick() {
        super.tick();
        Entity thrower = getOwner();
        if (!isPulling() && !isRemoved() && (tickCount > FLIGHT_TIMEOUT || thrower == null)) {
            releaseTracker(thrower);
            discard();
            return;
        }
        if (thrower == null) {
            return;
        }
        if (!level().isClientSide()) {
            if (!added) {
                int tracked = thrower.getData(TCAttachments.GRAPPLE_ID.get());
                if (tracked >= 0 && tracked != getId() && level().getEntity(tracked) instanceof EntityGrapple previous) {
                    previous.discard();
                }
                thrower.setData(TCAttachments.GRAPPLE_ID.get(), getId());
                added = true;
            } else if (thrower.getData(TCAttachments.GRAPPLE_ID.get()) != getId()) {
                discard();
                return;
            }
        }
        if (thrower instanceof LivingEntity living && isPulling() && !isRemoved()) {
            if (living.isShiftKeyDown()) {
                if (!level().isClientSide()) {
                    releaseTracker(living);
                    discard();
                }
                return;
            }
            if (!level().isClientSide() && living instanceof ServerPlayer serverPlayer) {
                ((ServerGamePacketListenerImplAccessor) serverPlayer.connection).setAboveGroundTickCount(0);
            }
            living.resetFallDistance();
            double dis = living.distanceTo(this);
            double mx = getX() - living.getX();
            double my = getY() - living.getY();
            double mz = getZ() - living.getZ();
            double dd = dis;
            if (dis < 8.0) {
                dd = dis * (8.0 - dis);
            }
            dd = Math.max(1.0E-9, dd);
            mx /= dd * PULL_DIVISOR;
            my /= dd * PULL_DIVISOR;
            mz /= dd * PULL_DIVISOR;
            Vec3 pull = new Vec3(mx, my, mz);
            if (pull.length() > PULL_CAP) {
                pull = pull.normalize().scale(0.25);
            }
            living.setDeltaMovement(living.getDeltaMovement().add(pull.x, pull.y + LIFT_BONUS, pull.z));
            if (!boost) {
                living.setDeltaMovement(living.getDeltaMovement().add(0.0, LAUNCH_BOOST, 0.0));
                boost = true;
            }
        }
        if (level().isClientSide()) {
            if (!isPulling()) {
                ampl += 0.02F;
            } else {
                ampl *= 0.66F;
            }
        }
    }

    private void releaseTracker(Entity thrower) {
        if (!level().isClientSide() && thrower != null && thrower.getData(TCAttachments.GRAPPLE_ID.get()) == getId()) {
            thrower.setData(TCAttachments.GRAPPLE_ID.get(), -1);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide()) {
            releaseTracker(getOwner());
        }
        super.remove(reason);
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (!level().isClientSide()) {
            setPulling();
            setDeltaMovement(Vec3.ZERO);
            setPos(hit.getLocation());
        }
    }
}
