package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** TC4 Swarmer variant: a permanent mature spore that periodically emits Taint Swarms. */
public final class EntityTaintSporeSwarmer extends EntityTaintSpore {
    private static final int EMIT_INTERVAL = 500;

    public EntityTaintSporeSwarmer(EntityType<? extends EntityTaintSpore> type, Level level) {
        super(type, level);
        setSporeSize(10);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 75.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected boolean requiresStalkSupport() {
        // TC4 Swarmers were free-standing mature spores; unlike ordinary spores they did not
        // depend on a fibrous spore-stalk block beneath them.
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server)
                || isRemoved()
                || server.getDifficulty() == Difficulty.PEACEFUL
                || tickCount % EMIT_INTERVAL != 0
                || !TaintBiomeManager.isTainted(server, blockPosition())
                || server.getNearestPlayer(this, 16.0) == null
                || !server.getEntitiesOfClass(EntityTaintSwarm.class, new AABB(blockPosition()).inflate(16.0))
                        .isEmpty()) {
            return;
        }
        EntityTaintSwarm swarm = TCEntities.TAINT_SWARM.get().create(server);
        if (swarm != null) {
            swarm.moveTo(getX(), getY() + 0.5, getZ(), random.nextFloat() * 360.0F, 0.0F);
            server.addFreshEntity(swarm);
        }
    }

    @Override
    protected void burst(ServerLevel level) {
        if (level.getDifficulty() != Difficulty.PEACEFUL) {
            EntityTaintSwarm swarm = TCEntities.TAINT_SWARM.get().create(level);
            if (swarm != null) {
                swarm.moveTo(getX(), getY(), getZ(), random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(swarm);
            }
        }
        demoteSupport(level);
        discard();
    }
}
