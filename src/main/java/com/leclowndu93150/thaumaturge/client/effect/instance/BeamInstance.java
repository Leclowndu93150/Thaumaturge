package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker.WandSource;
import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellRayTrace;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BeamInstance implements IFXInstance {
    private static final int HIT_DURATION = 6;
    private static final float GROW_TICKS = 4.0F;
    private static final int FADE_TICKS = 4;
    private static final float BASE_OPACITY = 0.4F;
    private static final float FADE_STEP = 0.1F;
    private static final double SAME_SOURCE_DISTANCE_SQR = 0.01;
    private static final double SAME_TARGET_DISTANCE_SQR = 0.01;

    private final int sourceEntityId;
    private final boolean entityAnchored;
    private final @Nullable WandSource wandSource;
    private final boolean withSource;
    private final int beamType;
    private final Vec3 requestedSource;
    private double sourceX;
    private double sourceY;
    private double sourceZ;
    private double prevSourceX;
    private double prevSourceY;
    private double prevSourceZ;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double prevTargetX;
    private double prevTargetY;
    private double prevTargetZ;
    private final float colorR;
    private final float colorG;
    private final float colorB;
    private final float endMod;
    private final boolean reverse;
    private final int rotationSpeed;
    private float length;
    private float rotYaw;
    private float rotPitch;
    private float prevYaw;
    private float prevPitch;
    private int age;
    private int hitTicks;
    private int maxAge;
    private float prevSize;
    private boolean expired;
    private int channelBranch;
    private double channelRange;
    private float channelYawOffset;
    private float channelPitchOffset;
    private boolean localChannelHit;
    private boolean channelStopped;
    private @Nullable InteractionHand channelHand;
    private @Nullable Vec3 lastChannelTarget;
    private @Nullable Vec3 frozenSource;
    private @Nullable Vec3 ribbonSide;

    public BeamInstance(
            double sx,
            double sy,
            double sz,
            double tx,
            double ty,
            double tz,
            int color,
            int age,
            int beamType,
            float endMod,
            boolean reverse,
            int sourceEntityId,
            boolean withSource) {
        this(sx, sy, sz, tx, ty, tz, color, age, beamType, endMod, reverse, sourceEntityId, withSource, false);
    }

    public BeamInstance(
            double sx,
            double sy,
            double sz,
            double tx,
            double ty,
            double tz,
            int color,
            int age,
            int beamType,
            float endMod,
            boolean reverse,
            int sourceEntityId,
            boolean withSource,
            boolean hit) {
        this(
                sx,
                sy,
                sz,
                tx,
                ty,
                tz,
                color,
                age,
                beamType,
                endMod,
                reverse,
                sourceEntityId,
                withSource,
                hit,
                sourceEntityId != BeamPayloadIds.NO_ENTITY);
    }

    public BeamInstance(
            double sx,
            double sy,
            double sz,
            double tx,
            double ty,
            double tz,
            int color,
            int age,
            int beamType,
            float endMod,
            boolean reverse,
            int sourceEntityId,
            boolean withSource,
            boolean hit,
            boolean anchorToEntity) {
        this(
                sx,
                sy,
                sz,
                tx,
                ty,
                tz,
                color,
                age,
                beamType,
                endMod,
                reverse,
                sourceEntityId,
                withSource,
                hit,
                anchorToEntity,
                null);
    }

    public BeamInstance(
            double sx,
            double sy,
            double sz,
            double tx,
            double ty,
            double tz,
            int color,
            int age,
            int beamType,
            float endMod,
            boolean reverse,
            int sourceEntityId,
            boolean withSource,
            boolean hit,
            boolean anchorToEntity,
            @Nullable WandSource wandSource) {
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        this.maxAge = age;
        this.beamType = beamType;
        this.endMod = endMod;
        this.reverse = reverse;
        this.rotationSpeed = 5;
        this.sourceEntityId = sourceEntityId;
        this.entityAnchored = anchorToEntity && sourceEntityId != BeamPayloadIds.NO_ENTITY;
        this.wandSource = wandSource;
        this.channelHand = wandSource == null ? null : wandSource.hand();
        this.withSource = withSource;
        this.requestedSource = new Vec3(sx, sy, sz);
        this.sourceX = sx;
        this.sourceY = sy;
        this.sourceZ = sz;
        this.prevSourceX = sx;
        this.prevSourceY = sy;
        this.prevSourceZ = sz;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        this.prevTargetX = tx;
        this.prevTargetY = ty;
        this.prevTargetZ = tz;
        refreshGeometry();
        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;
        this.hitTicks = hit ? HIT_DURATION : 0;
    }

    @Override
    public void tick() {
        this.prevSize = computeSize(0.0F);
        this.prevSourceX = this.sourceX;
        this.prevSourceY = this.sourceY;
        this.prevSourceZ = this.sourceZ;
        this.prevTargetX = this.targetX;
        this.prevTargetY = this.targetY;
        this.prevTargetZ = this.targetZ;
        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;
        LivingEntity anchor = this.entityAnchored ? BeamAnchor.find(this.sourceEntityId) : null;
        if (anchor != null) {
            Vec3 castPoint = BeamAnchor.castPoint(anchor, 1.0F);
            this.sourceX = castPoint.x;
            this.sourceY = castPoint.y;
            this.sourceZ = castPoint.z;
        }
        if (isChannel() && this.wandSource != null && !this.channelStopped) {
            LocalPlayer player = this.wandSource.player();
            if (!player.isUsingItem() || player.getUsedItemHand() != this.channelHand) {
                stopChannel();
            }
        }
        refreshGeometry();
        unwindAngles();
        if (this.hitTicks > 0) {
            this.hitTicks--;
        }
        if (this.age >= this.maxAge && this.hitTicks <= 0) {
            this.expired = true;
        }
        if (this.age < this.maxAge) this.age++;
    }

    private void refreshGeometry() {
        Vec3 span = new Vec3(this.sourceX - this.targetX, this.sourceY - this.targetY, this.sourceZ - this.targetZ);
        this.length = (float) span.length();
        this.rotYaw = (float) Math.toDegrees(Math.atan2(span.x, span.z));
        this.rotPitch = (float) Math.toDegrees(Math.atan2(span.y, span.horizontalDistance()));
    }

    private void unwindAngles() {
        this.prevYaw = this.rotYaw - Mth.wrapDegrees(this.rotYaw - this.prevYaw);
        this.prevPitch = this.rotPitch - Mth.wrapDegrees(this.rotPitch - this.prevPitch);
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }

    public boolean sameSourceAs(BeamInstance other) {
        if (this.beamType != other.beamType || this.withSource != other.withSource) {
            return false;
        }
        if (this.sourceEntityId != BeamPayloadIds.NO_ENTITY || other.sourceEntityId != BeamPayloadIds.NO_ENTITY) {
            return this.sourceEntityId == other.sourceEntityId
                    && this.entityAnchored == other.entityAnchored
                    && Objects.equals(this.wandSource, other.wandSource);
        }
        return this.requestedSource.distanceToSqr(other.requestedSource) < SAME_SOURCE_DISTANCE_SQR;
    }

    public boolean sameBeamAs(BeamInstance other) {
        double dx = this.targetX - other.targetX;
        double dy = this.targetY - other.targetY;
        double dz = this.targetZ - other.targetZ;
        return sameSourceAs(other)
                && this.colorR == other.colorR
                && this.colorG == other.colorG
                && this.colorB == other.colorB
                && this.endMod == other.endMod
                && this.reverse == other.reverse
                && (isChannel() || other.isChannel()
                        ? isChannel() && other.isChannel() && this.channelBranch == other.channelBranch
                        : dx * dx + dy * dy + dz * dz < SAME_TARGET_DISTANCE_SQR);
    }

    public BeamInstance withChannel(int branch, double range) {
        this.channelBranch = branch;
        this.channelRange = range;
        this.localChannelHit = this.hitTicks > 0;
        // Only the initial packet determines relative branch spread. Recomputing these
        // offsets on every server pulse mixes an old server aim with the current camera.
        if (isChannel() && this.wandSource != null) {
            LocalPlayer player = this.wandSource.player();
            Vec3 aim = new Vec3(this.targetX, this.targetY, this.targetZ).subtract(player.getEyePosition());
            if (aim.lengthSqr() > 1.0E-6) {
                float yaw = Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(-aim.x, aim.z)) - player.getYRot());
                float pitch = (float) -Math.toDegrees(Math.atan2(aim.y, aim.horizontalDistance())) - player.getXRot();
                // A single channel ray should track the live camera, not delayed server aim.
                // Preserve intentional spread on additional channel branches.
                this.channelYawOffset = (branch - 1) % 8 == 0 ? 0.0F : Mth.clamp(yaw, -15.0F, 15.0F);
                this.channelPitchOffset = (branch - 1) % 8 == 0 ? 0.0F : Mth.clamp(pitch, -15.0F, 15.0F);
            }
        }
        return this;
    }

    public BeamInstance withChannelHand(@Nullable InteractionHand hand) {
        this.channelHand = hand;
        return this;
    }

    public boolean isStopped() {
        return this.channelStopped;
    }

    public boolean isChannelFrom(int entityId, @Nullable InteractionHand hand) {
        return isChannel() && this.sourceEntityId == entityId && (hand == null || this.channelHand == hand);
    }

    public void stopChannel() {
        if (!isChannel() || this.channelStopped) return;
        this.frozenSource = this.wandSource == null ? sourcePos(1.0F) : this.wandSource.tip();
        if (this.frozenSource == null) this.frozenSource = sourcePos(1.0F);
        this.channelStopped = true;
        this.hitTicks = 0;
        this.localChannelHit = false;
        // Stop tracking instantly; retain only a very short, stationary afterglow.
        this.maxAge = Math.min(this.maxAge, this.age + 2);
    }

    public boolean isChannel() {
        return this.channelBranch > 0 && this.channelRange > 0.0 && Double.isFinite(this.channelRange);
    }

    public void refresh(BeamInstance newer) {
        if (!this.entityAnchored) {
            this.prevSourceX = this.sourceX;
            this.prevSourceY = this.sourceY;
            this.prevSourceZ = this.sourceZ;
            this.sourceX = newer.sourceX;
            this.sourceY = newer.sourceY;
            this.sourceZ = newer.sourceZ;
        }
        this.prevTargetX = this.targetX;
        this.prevTargetY = this.targetY;
        this.prevTargetZ = this.targetZ;
        this.targetX = newer.targetX;
        this.targetY = newer.targetY;
        this.targetZ = newer.targetZ;
        if (!this.channelStopped) {
            this.maxAge = Math.max(this.maxAge, this.age + newer.maxAge);
            this.hitTicks = isChannel() ? newer.hitTicks : Math.max(this.hitTicks, newer.hitTicks);
            this.channelRange = newer.channelRange;
            // Keep the initial channel aim offset stable across asynchronous server pulses.
            this.expired = false;
        }
        refreshGeometry();
    }

    public int beamType() {
        return this.beamType;
    }

    public float length() {
        return this.length;
    }

    public float endMod() {
        return this.endMod;
    }

    public boolean reverse() {
        return this.reverse;
    }

    public int rotationSpeed() {
        return this.rotationSpeed;
    }

    public int age() {
        return this.age;
    }

    public int maxAge() {
        return this.maxAge;
    }

    public float colorR() {
        return this.colorR;
    }

    public float colorG() {
        return this.colorG;
    }

    public float colorB() {
        return this.colorB;
    }

    public boolean entityAnchored() {
        return this.entityAnchored;
    }

    public boolean withSource() {
        return this.withSource;
    }

    public boolean hit() {
        return isChannel() && this.wandSource != null ? this.localChannelHit : this.hitTicks > 0;
    }

    public float hitFactor(float partial) {
        if (isChannel()) {
            return Mth.clamp(computeOpacity(partial) / BASE_OPACITY, 0.0F, 1.0F);
        }
        return Mth.clamp((this.hitTicks - partial) / (float) HIT_DURATION, 0.0F, 1.0F);
    }

    public Vec3 sourcePos(float partial) {
        if (this.channelStopped && this.frozenSource != null) return this.frozenSource;
        LivingEntity anchor = this.wandSource != null
                ? this.wandSource.player()
                : this.entityAnchored ? BeamAnchor.find(this.sourceEntityId) : null;
        if (anchor != null) {
            return BeamAnchor.castPoint(anchor, partial);
        }
        return new Vec3(
                Mth.lerp(partial, this.prevSourceX, this.sourceX),
                Mth.lerp(partial, this.prevSourceY, this.sourceY),
                Mth.lerp(partial, this.prevSourceZ, this.sourceZ));
    }

    public @Nullable Vec3 wandTip() {
        if (this.channelStopped) return this.wandSource == null ? null : this.frozenSource;
        return this.wandSource == null ? null : this.wandSource.tip();
    }

    public Vec3 targetPos(float partial) {
        if (isChannel()) {
            if (this.channelStopped && this.lastChannelTarget != null) return this.lastChannelTarget;
            LivingEntity anchor = this.wandSource != null
                    ? this.wandSource.player()
                    : this.entityAnchored ? BeamAnchor.find(this.sourceEntityId) : null;
            if (anchor != null && !this.channelStopped) {
                Vec3 direction = channelForward(partial);
                HitResult hit = SpellRayTrace.trace(
                        anchor.level(), anchor, anchor.getEyePosition(partial), direction, this.channelRange);
                this.localChannelHit = hit.getType() != HitResult.Type.MISS;
                // Sable block clips return plot coordinates; entities and misses are world-space.
                // Use the interpolated render pose so moving decks do not wobble each tick.
                Vec3 result = hit.getType() == HitResult.Type.BLOCK
                        ? ClientSpaces.renderPosition(anchor.level(), hit.getLocation())
                        : hit.getLocation();
                this.lastChannelTarget = result;
                return result;
            }
        }
        return new Vec3(
                Mth.lerp(partial, this.prevTargetX, this.targetX),
                Mth.lerp(partial, this.prevTargetY, this.targetY),
                Mth.lerp(partial, this.prevTargetZ, this.targetZ));
    }

    public Vec3 channelForward(float partial) {
        LivingEntity anchor = this.wandSource != null
                ? this.wandSource.player()
                : this.entityAnchored ? BeamAnchor.find(this.sourceEntityId) : null;
        if (this.wandSource != null) {
            LocalPlayer player = this.wandSource.player();
            return Vec3.directionFromRotation(
                    player.getViewXRot(partial) + this.channelPitchOffset,
                    player.getViewYRot(partial) + this.channelYawOffset);
        }
        return anchor == null
                ? new Vec3(this.targetX - this.sourceX, this.targetY - this.sourceY, this.targetZ - this.sourceZ)
                        .normalize()
                : anchor.getViewVector(partial);
    }

    /** Parallel-transport a ribbon axis between render frames instead of flipping at vertical aim. */
    public Vec3 ribbonSide(Vec3 axis) {
        Vec3 candidate = this.ribbonSide == null
                ? (Math.abs(axis.y) < 0.9 ? axis.cross(new Vec3(0.0, 1.0, 0.0)) : axis.cross(new Vec3(1.0, 0.0, 0.0)))
                : this.ribbonSide;
        candidate = candidate.subtract(axis.scale(candidate.dot(axis)));
        if (candidate.lengthSqr() < 1.0E-8) {
            candidate = axis.cross(Math.abs(axis.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0));
        }
        this.ribbonSide = candidate.normalize();
        return this.ribbonSide;
    }

    public float yawAt(float partial) {
        return Mth.lerp(partial, this.prevYaw, this.rotYaw);
    }

    public float pitchAt(float partial) {
        return Mth.lerp(partial, this.prevPitch, this.rotPitch);
    }

    public float computeSize(float partial) {
        float size = Math.min((this.age + partial) / GROW_TICKS, 1.0F);
        return Mth.lerp(partial, this.prevSize, size);
    }

    public float computeOpacity(float partial) {
        int remaining = this.maxAge - this.age;
        if (remaining <= FADE_TICKS) {
            return BASE_OPACITY - (FADE_TICKS - remaining) * FADE_STEP;
        }
        return BASE_OPACITY;
    }

    public float texScroll(float partial) {
        LocalPlayer player = Minecraft.getInstance().player;
        float slide = player != null ? player.tickCount : 0;
        float v = slide + partial;
        if (this.reverse) {
            v = -v;
        }
        return -v * 0.2F - Mth.floor(-v * 0.1F);
    }

    public float worldRotation(float partial) {
        long worldTime = this.levelTime();
        return worldTime % (360 / this.rotationSpeed) * this.rotationSpeed + this.rotationSpeed * partial;
    }

    private long levelTime() {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null ? level.getGameTime() : 0L;
    }
}
