package com.leclowndu93150.thaumaturge.content.entity.projectile;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.mixin.server.network.ServerGamePacketListenerImplAccessor;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EntityGrapple extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> PULLING =
            SynchedEntityData.defineId(EntityGrapple.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> MAIN_HAND =
            SynchedEntityData.defineId(EntityGrapple.class, EntityDataSerializers.BOOLEAN);

    private static final int MAX_FLIGHT_TICKS = 30;
    private static final double RENDER_DISTANCE_SQR = 64.0 * 64.0;
    private static final double EASE_RANGE = 8.0;
    private static final double NEAR_TUG = 0.02;
    private static final double FAR_TUG = 0.1;
    private static final double MAX_TUG = 0.25;
    private static final double LIFT = 0.06;
    private static final double BOOST = 0.42;
    private static final double FLIGHT_GRAVITY = 0.01;
    private static final double PULLING_GRAVITY = 0.0;
    private static final float AMPLITUDE_GROWTH = 0.02F;
    private static final float AMPLITUDE_DECAY = 0.6F;

    private boolean claimed;
    private boolean boosted;
    private boolean anchored;
    public float ampl;

    public EntityGrapple(EntityType<? extends EntityGrapple> type, Level level) {
        super(type, level);
    }

    public EntityGrapple(
            EntityType<? extends EntityGrapple> type, Level level, LivingEntity thrower, InteractionHand hand) {
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
        return isPulling() ? PULLING_GRAVITY : FLIGHT_GRAVITY;
    }

    public boolean isPulling() {
        return entityData.get(PULLING);
    }

    private boolean isAnchored() {
        return anchored || isPulling();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (PULLING.equals(accessor) && isPulling()) {
            anchored = true;
            setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < RENDER_DISTANCE_SQR;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    public void tick() {
        Entity owner = getOwner();
        if (!level().isClientSide() && !keepAlive(owner)) {
            return;
        }
        if (level().isClientSide()) {
            ampl = isAnchored() ? ampl * AMPLITUDE_DECAY : ampl + AMPLITUDE_GROWTH;
        }
        if (!isAnchored()) {
            super.tick();
            return;
        }
        baseTick();
        setDeltaMovement(Vec3.ZERO);
        if (isPulling() && owner != null && canMoveOwner(owner)) {
            pull(owner);
        }
    }

    private boolean keepAlive(@Nullable Entity owner) {
        if (owner == null || !owner.isAlive() || owner.level() != level()) {
            discard();
            return false;
        }
        if (!claim(owner)) {
            discard();
            return false;
        }
        if (!isPulling() && tickCount > MAX_FLIGHT_TICKS) {
            discard();
            return false;
        }
        return true;
    }

    private boolean claim(Entity owner) {
        if (!claimed) {
            int tracked = owner.getData(TTAttachments.GRAPPLE_ID.get());
            if (tracked >= 0 && tracked != getId() && level().getEntity(tracked) instanceof EntityGrapple previous) {
                previous.discard();
            }
            owner.setData(TTAttachments.GRAPPLE_ID.get(), getId());
            claimed = true;
        } else if (owner.getData(TTAttachments.GRAPPLE_ID.get()) != getId()) {
            return false;
        }
        return true;
    }

    private boolean canMoveOwner(Entity owner) {
        return level().isClientSide()
                ? owner instanceof Player player && player.isLocalPlayer()
                : owner.isAlive() && !owner.isRemoved();
    }

    private void pull(Entity owner) {
        if (owner.isShiftKeyDown()) {
            if (!level().isClientSide()) {
                discard();
            }
            return;
        }
        owner.resetFallDistance();
        if (owner instanceof ServerPlayer serverPlayer) {
            ((ServerGamePacketListenerImplAccessor) serverPlayer.connection).setAboveGroundTickCount(0);
        }
        Vec3 tug = tugVector(position(), owner.getEyePosition());
        double hop = boosted ? 0.0 : BOOST;
        boosted = true;
        owner.setDeltaMovement(owner.getDeltaMovement().add(tug.x, tug.y + LIFT + hop, tug.z));
    }

    static Vec3 tugVector(Vec3 grapplePosition, Vec3 ownerEyePosition) {
        Vec3 offset = grapplePosition.subtract(ownerEyePosition);
        double distance = offset.length();
        if (distance == 0.0) {
            return Vec3.ZERO;
        }
        double strength = Math.min(tugStrength(distance), MAX_TUG);
        return offset.scale(strength / distance);
    }

    private static double tugStrength(double distance) {
        double t = Math.min(distance / EASE_RANGE, 1.0);
        double eased = t * t * (3.0 - 2.0 * t);
        return Mth.lerp(eased, NEAR_TUG, FAR_TUG);
    }

    public InteractionHand getHand() {
        return entityData.get(MAIN_HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private void releaseTracker(Entity thrower) {
        if (!level().isClientSide() && thrower != null && thrower.getData(TTAttachments.GRAPPLE_ID.get()) == getId()) {
            thrower.setData(TTAttachments.GRAPPLE_ID.get(), -1);
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
        super.onHitBlock(hit);
        if (isPulling()) {
            return;
        }
        anchored = true;
        setDeltaMovement(Vec3.ZERO);
        setPos(LevelSpaces.toWorld(level(), hit.getLocation()));
        if (!level().isClientSide()) {
            entityData.set(PULLING, true);
        }
    }
}
