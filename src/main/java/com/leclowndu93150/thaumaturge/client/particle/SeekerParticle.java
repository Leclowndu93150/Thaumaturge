package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class SeekerParticle extends TTParticle {
    private static final double APPROACH_CAP = 0.25;
    private static final double APPROACH_RANGE = 15.0;
    private static final double SHRINK_DISTANCE = 2.0;
    private static final float SHRINK_FACTOR = 0.9F;
    private static final double FRICTION_HORIZONTAL = 0.985;
    private static final double FRICTION_VERTICAL = 0.95;
    private static final double SPAWN_JITTER = 0.01;
    private static final double LIFETIME_PER_BLOCK = 10.0;
    private static final double MIN_DISTANCE = 1.0E-4;

    public static final int NO_ENTITY = -1;

    private final int targetEntityId;
    private final float driftStrength;
    private final Vec3 targetInLevel;
    private @Nullable BlockPos infusionMatrix;
    private Vec3 target;
    private Entity targetEntity;

    protected SeekerParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            ParticleSheet sheet,
            int targetEntityId,
            Vec3 target,
            Vec3 spawnVelocity,
            float driftStrength) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.targetEntityId = targetEntityId;
        this.targetInLevel = this.target = target;
        this.driftStrength = driftStrength;
        initSeeker(x, y, z, spawnVelocity);
    }

    protected SeekerParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            TextureAtlasSprite sprite,
            int targetEntityId,
            Vec3 target,
            Vec3 spawnVelocity,
            float driftStrength) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
        this.targetEntityId = targetEntityId;
        this.targetInLevel = this.target = target;
        this.driftStrength = driftStrength;
        initSeeker(x, y, z, spawnVelocity);
    }

    private void initSeeker(double x, double y, double z, Vec3 spawnVelocity) {
        refreshTarget();
        this.xd = spawnVelocity.x + this.random.nextGaussian() * SPAWN_JITTER;
        this.yd = spawnVelocity.y + this.random.nextGaussian() * SPAWN_JITTER;
        this.zd = spawnVelocity.z + this.random.nextGaussian() * SPAWN_JITTER;
        Vec3 start = LevelSpaces.toWorld(this.level, new Vec3(x, y, z));
        int travel = Math.max(1, (int) (this.target.distanceTo(start) * LIFETIME_PER_BLOCK));
        this.lifetime = travel / 2 + this.random.nextInt(travel);
    }

    protected final void endWhenMatrixMoves() {
        BlockPos matrix = BlockPos.containing(this.targetInLevel).above();
        if (this.level.getBlockEntity(matrix) instanceof BlockEntityInfusionMatrix) {
            this.infusionMatrix = matrix;
        }
    }

    @Override
    public void tick() {
        if (this.infusionMatrix != null
                && !(this.level.getBlockEntity(this.infusionMatrix) instanceof BlockEntityInfusionMatrix)) {
            remove();
            return;
        }
        super.tick();
        if (this.removed || delayed()) {
            return;
        }
        refreshTarget();
        if (arrived()) {
            remove();
            return;
        }
        this.xd *= FRICTION_HORIZONTAL;
        this.yd *= FRICTION_VERTICAL;
        this.zd *= FRICTION_HORIZONTAL;
        steer();
    }

    private void refreshTarget() {
        if (this.targetEntityId == NO_ENTITY) {
            this.target = LevelSpaces.toWorld(this.level, this.targetInLevel);
            return;
        }
        if (this.targetEntity == null) {
            this.targetEntity = this.level.getEntity(this.targetEntityId);
        }
        if (this.targetEntity != null && !this.targetEntity.isRemoved()) {
            this.target = new Vec3(this.targetEntity.getX(), this.targetEntity.getEyeY(), this.targetEntity.getZ());
        }
    }

    private boolean arrived() {
        return BlockPos.containing(this.x, this.y, this.z)
                .equals(BlockPos.containing(this.target.x, this.target.y, this.target.z));
    }

    private void steer() {
        Vec3 toTarget = this.target.subtract(this.x, this.y, this.z);
        double distance = Math.max(MIN_DISTANCE, toTarget.length());
        double pull = Math.min(APPROACH_CAP, distance / APPROACH_RANGE);
        if (distance < SHRINK_DISTANCE) {
            this.quadSize *= SHRINK_FACTOR;
        }
        this.xd = Mth.clamp(this.xd + toTarget.x / distance * pull, -pull, pull);
        this.yd = Mth.clamp(this.yd + toTarget.y / distance * pull, -pull, pull);
        this.zd = Mth.clamp(this.zd + toTarget.z / distance * pull, -pull, pull);
        drift(this.driftStrength, this.driftStrength, this.driftStrength);
    }
}
