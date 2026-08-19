package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class BeamInstance implements IFXInstance {
    private static final double HAND_SIDE_X = 0.066;
    private static final double HAND_DROP = 0.06;
    private static final double HAND_SIDE_Z = 0.04;
    private static final double LOOK_LEAD = 0.3;
    private static final double ANCHOR_LIFT = 0.25;
    private static final float GROW_TICKS = 4.0F;
    private static final int FADE_TICKS = 4;
    private static final float BASE_OPACITY = 0.4F;
    private static final float FADE_STEP = 0.1F;

    private final int sourceEntityId;
    private final boolean entityAnchored;
    private final boolean withSource;
    private final int beamType;
    private double sourceX;
    private double sourceY;
    private double sourceZ;
    private double prevSourceX;
    private double prevSourceY;
    private double prevSourceZ;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final double prevTargetX;
    private final double prevTargetY;
    private final double prevTargetZ;
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
    private final int maxAge;
    private float prevSize;
    private boolean expired;

    public BeamInstance(double sx, double sy, double sz, double tx, double ty, double tz, int color, int age, int beamType, float endMod, boolean reverse, int sourceEntityId, boolean withSource) {
        this.colorR = ((color >> 16) & 0xFF) / 255.0F;
        this.colorG = ((color >> 8) & 0xFF) / 255.0F;
        this.colorB = (color & 0xFF) / 255.0F;
        this.maxAge = age;
        this.beamType = beamType;
        this.endMod = endMod;
        this.reverse = reverse;
        this.rotationSpeed = 5;
        this.sourceEntityId = sourceEntityId;
        this.entityAnchored = sourceEntityId != BeamPayloadIds.NO_ENTITY;
        this.withSource = withSource;
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
    }

    @Override
    public void tick() {
        this.prevSize = computeSize(0.0F);
        this.prevSourceX = this.sourceX;
        this.prevSourceY = this.sourceY;
        this.prevSourceZ = this.sourceZ;
        this.prevYaw = this.rotYaw;
        this.prevPitch = this.rotPitch;
        Entity anchor = resolveAnchor();
        if (anchor != null) {
            this.sourceX = anchor.getX();
            this.sourceY = anchor.getY() + anchorHeight(anchor);
            this.sourceZ = anchor.getZ();
        }
        refreshGeometry();
        unwindAngles();
        if (this.age++ >= this.maxAge) {
            this.expired = true;
        }
    }

    private Entity resolveAnchor() {
        if (!this.entityAnchored) {
            return null;
        }
        ClientLevel level = Minecraft.getInstance().level;
        Entity entity = level != null ? level.getEntity(this.sourceEntityId) : null;
        return entity != null && entity.isAlive() ? entity : null;
    }

    private static double anchorHeight(Entity entity) {
        return entity.getBbHeight() / 2.0 + ANCHOR_LIFT;
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

    public Vec3 sourcePos(float partial) {
        Entity anchor = resolveAnchor();
        if (anchor != null) {
            Vec3 lead = anchor.getViewVector(1.0F).scale(LOOK_LEAD);
            Vec3 before = handAnchor(anchor, anchor.xOld, anchor.yOld, anchor.zOld, anchor.yRotO).add(lead);
            Vec3 now = handAnchor(anchor, anchor.getX(), anchor.getY(), anchor.getZ(), Mth.lerp(partial, anchor.yRotO, anchor.getYRot())).add(lead);
            return before.lerp(now, partial);
        }
        return new Vec3(Mth.lerp(partial, this.prevSourceX, this.sourceX), Mth.lerp(partial, this.prevSourceY, this.sourceY), Mth.lerp(partial, this.prevSourceZ, this.sourceZ));
    }

    private static Vec3 handAnchor(Entity entity, double x, double y, double z, float yawDegrees) {
        double yawRad = Math.toRadians(yawDegrees);
        return new Vec3(x - Math.cos(yawRad) * HAND_SIDE_X, y + anchorHeight(entity) - HAND_DROP, z - Math.sin(yawRad) * HAND_SIDE_Z);
    }

    public Vec3 targetPos(float partial) {
        return new Vec3(Mth.lerp(partial, this.prevTargetX, this.targetX), Mth.lerp(partial, this.prevTargetY, this.targetY), Mth.lerp(partial, this.prevTargetZ, this.targetZ));
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
        long worldTime = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        return worldTime % (360 / this.rotationSpeed) * this.rotationSpeed + this.rotationSpeed * partial;
    }
}
