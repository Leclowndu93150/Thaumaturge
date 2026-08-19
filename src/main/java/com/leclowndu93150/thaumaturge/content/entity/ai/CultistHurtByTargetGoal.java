package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.phys.AABB;

public final class CultistHurtByTargetGoal extends HurtByTargetGoal {
    private static final double ALERT_RANGE_Y = 10.0;

    public CultistHurtByTargetGoal(PathfinderMob cultist) {
        super(cultist);
        this.setAlertOthers();
    }

    @Override
    protected void alertOthers() {
        double range = this.getFollowDistance();
        AABB searchBox = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(range, ALERT_RANGE_Y, range);
        for (EntityCultist other : this.mob.level().getEntitiesOfClass(EntityCultist.class, searchBox, EntitySelector.NO_SPECTATORS)) {
            if (this.mob != other && other.getTarget() == null && this.mob.getLastHurtByMob() != null && !other.isAlliedTo(this.mob.getLastHurtByMob())) {
                this.alertOther(other, this.mob.getLastHurtByMob());
            }
        }
    }
}
