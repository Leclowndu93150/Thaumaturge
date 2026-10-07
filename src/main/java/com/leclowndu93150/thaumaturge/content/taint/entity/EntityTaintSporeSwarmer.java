package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSwarm;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class EntityTaintSporeSwarmer extends AbstractTaintSpore {
    private static final double MAX_HEALTH = 75.0;
    private static final int EMIT_INTERVAL = 500;
    private static final double EMIT_RANGE = 16.0;
    private static final double EMIT_HEIGHT = 0.5;
    private static final float FULL_TURN = 360.0F;

    public EntityTaintSporeSwarmer(EntityType<? extends EntityTaintSporeSwarmer> type, Level level) {
        super(type, level);
        setSporeSize(MAX_SIZE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected boolean requiresStalkSupport() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server) || isRemoved() || server.getDifficulty() == Difficulty.PEACEFUL || tickCount % EMIT_INTERVAL != 0
                || !TaintBiomeManager.isTainted(server, blockPosition()) || server.getNearestPlayer(this, EMIT_RANGE) == null
                || !server.getEntitiesOfClass(EntityTaintSwarm.class, new AABB(blockPosition()).inflate(EMIT_RANGE)).isEmpty()) {
            return;
        }
        signalRelease(server);
        releaseSwarm(server, EMIT_HEIGHT);
    }

    @Override
    protected void onBurst(ServerLevel level) {
        if (level.getDifficulty() != Difficulty.PEACEFUL) {
            releaseSwarm(level, 0.0);
        }
    }

    private void releaseSwarm(ServerLevel level, double height) {
        EntityTaintSwarm swarm = TTEntities.TAINT_SWARM.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (swarm != null) {
            swarm.snapTo(getX(), getY() + height, getZ(), random.nextFloat() * FULL_TURN, 0.0F);
            level.addFreshEntity(swarm);
        }
    }
}
