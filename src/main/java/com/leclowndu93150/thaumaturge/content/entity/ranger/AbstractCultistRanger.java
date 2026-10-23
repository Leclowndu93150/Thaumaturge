package com.leclowndu93150.thaumaturge.content.entity.ranger;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public abstract class AbstractCultistRanger extends EntityCultist {
    private static final String PATROL_TARGET_KEY = "patrol_target";
    private static final String PATROLLING_KEY = "patrolling";
    private static final int PATROL_RANGE = 500;
    private static final double FAR_DESPAWN_DISTANCE_SQR = 16384.0;
    private static final int PATROL_PRIORITY = 4;
    private static final double PATROL_SPEED = 0.7;
    private static final double LEADER_PATROL_SPEED = 0.595;

    private @Nullable BlockPos patrolTarget;
    private boolean patrolling;

    protected AbstractCultistRanger(EntityType<? extends AbstractCultistRanger> type, Level level) {
        super(type, level);
    }

    public abstract boolean isPatrolLeader();

    protected abstract void registerCombatGoals();

    @Override
    protected final void registerGoals() {
        registerCombatGoals();
        this.goalSelector.addGoal(PATROL_PRIORITY, new RangerPatrolGoal(this, PATROL_SPEED, LEADER_PATROL_SPEED));
        this.addIdleGoals();
        this.addTargetGoals();
    }

    public @Nullable BlockPos patrolTarget() {
        return patrolTarget;
    }

    public boolean isPatrolling() {
        return patrolling;
    }

    public void setPatrolling(boolean patrolling) {
        this.patrolling = patrolling;
    }

    public void setPatrolTarget(BlockPos target) {
        this.patrolTarget = target;
        this.patrolling = true;
    }

    public void findPatrolTarget() {
        setPatrolTarget(this.blockPosition()
                .offset(
                        -PATROL_RANGE + this.random.nextInt(2 * PATROL_RANGE),
                        0,
                        -PATROL_RANGE + this.random.nextInt(2 * PATROL_RANGE)));
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return !patrolling || distanceSqr > FAR_DESPAWN_DISTANCE_SQR;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(PATROLLING_KEY, patrolling);
        if (patrolTarget != null) {
            output.putLong(PATROL_TARGET_KEY, patrolTarget.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        super.readAdditionalSaveData(input);
        patrolTarget = input.contains(PATROL_TARGET_KEY) ? BlockPos.of(input.getLong(PATROL_TARGET_KEY)) : null;
        patrolling = input.getBoolean(PATROLLING_KEY);
    }
}
