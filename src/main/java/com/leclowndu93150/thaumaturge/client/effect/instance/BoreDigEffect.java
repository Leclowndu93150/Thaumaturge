package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.manager.IFXInstance;
import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BoreDigEffect implements IFXInstance {
    private static final float TOTAL_PARTICLES = 50.0F;
    private static final int SPARKLE_CHANCE = 4;
    private static final float SPARKLE_RED = 0.2F;
    private static final float SPARKLE_GREEN_BASE = 0.6F;
    private static final float SPARKLE_GREEN_RANGE = 0.3F;
    private static final float SPARKLE_BLUE = 0.2F;

    private final ClientLevel level;
    private final BlockPos target;
    private final int boreEntityId;
    private final BlockState state;
    private final int particlesPerTick;
    private int remainingTicks;

    public BoreDigEffect(ClientLevel level, BlockPos target, int boreEntityId, BlockState state, int delay) {
        this.level = level;
        this.target = target;
        this.boreEntityId = boreEntityId;
        this.state = state;
        this.remainingTicks = Math.max(1, delay);
        this.particlesPerTick = Mth.ceil(TOTAL_PARTICLES / this.remainingTicks);
    }

    @Override
    public void tick() {
        Entity bore = this.level.getEntity(this.boreEntityId);
        if (bore == null) {
            this.remainingTicks = 0;
            return;
        }
        RandomSource random = this.level.getRandom();
        Vec3 destination = bore.getEyePosition();
        for (int index = 0; index < this.particlesPerTick; index++) {
            double x = this.target.getX() + random.nextFloat();
            double y = this.target.getY() + random.nextFloat();
            double z = this.target.getZ() + random.nextFloat();
            if (random.nextInt(SPARKLE_CHANCE) == 0) {
                BoreSparkleParticleOptions options = new BoreSparkleParticleOptions(this.boreEntityId, destination.x, destination.y, destination.z, SPARKLE_RED,
                        SPARKLE_GREEN_BASE + random.nextFloat() * SPARKLE_GREEN_RANGE, SPARKLE_BLUE);
                this.level.addParticle(options, x, y, z, 0.0, 0.0, 0.0);
            } else {
                BoreDebrisParticleOptions options = new BoreDebrisParticleOptions(this.state, this.boreEntityId, destination.x, destination.y, destination.z, 0.0, 0.0, 0.0);
                this.level.addParticle(options, x, y, z, 0.0, 0.0, 0.0);
            }
        }
        this.remainingTicks--;
    }

    @Override
    public boolean isExpired() {
        return this.remainingTicks <= 0;
    }
}
