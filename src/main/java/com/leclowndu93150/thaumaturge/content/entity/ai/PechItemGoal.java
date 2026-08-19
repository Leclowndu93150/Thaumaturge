package com.leclowndu93150.thaumaturge.content.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class PechItemGoal extends Goal {
    public static final String PECH_DROP_TAG = "PechDrop";
    private static final float MAX_TARGET_DISTANCE = 16.0F;
    private static final double PICKUP_RANGE_SQ = 1.5;
    private static final double SPEED_FACTOR = 1.5;

    private final EntityPech pech;
    private @Nullable ItemEntity targetItem;
    private int cooldown;

    public PechItemGoal(EntityPech pech) {
        this.pech = pech;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--this.cooldown > 0) {
            return false;
        }
        double best = Double.MAX_VALUE;
        this.targetItem = null;
        for (ItemEntity item : this.pech.level().getEntitiesOfClass(ItemEntity.class, this.pech.getBoundingBox().inflate(MAX_TARGET_DISTANCE))) {
            if (!this.pech.canPickup(item.getItem()) || item.entityTags().contains(PECH_DROP_TAG)) {
                continue;
            }
            double distance = item.distanceToSqr(this.pech);
            if (distance < best && distance <= MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE) {
                best = distance;
                this.targetItem = item;
            }
        }
        return this.targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null && this.targetItem.isAlive() && !this.pech.getNavigation().isDone() && this.targetItem.distanceToSqr(this.pech) < MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE;
    }

    @Override
    public void stop() {
        this.targetItem = null;
    }

    @Override
    public void start() {
        this.moveToTarget();
        this.cooldown = 0;
    }

    private void moveToTarget() {
        if (this.targetItem != null) {
            this.pech.getNavigation().moveTo(this.targetItem, this.pech.getAttributeValue(Attributes.MOVEMENT_SPEED) * SPEED_FACTOR);
        }
    }

    @Override
    public void tick() {
        if (this.targetItem == null) {
            return;
        }
        this.pech.getLookControl().setLookAt(this.targetItem, 30.0F, 30.0F);
        if (this.pech.getSensing().hasLineOfSight(this.targetItem) && --this.cooldown <= 0) {
            this.cooldown = 4 + this.pech.getRandom().nextInt(4);
            this.moveToTarget();
        }
        double distance = this.pech.distanceToSqr(this.targetItem.getX(), this.targetItem.getBoundingBox().minY, this.targetItem.getZ());
        if (distance <= PICKUP_RANGE_SQ) {
            this.cooldown = 0;
            int before = this.targetItem.getItem().getCount();
            ItemStack remainder = this.pech.pickupItem(this.targetItem.getItem().copy());
            if (!remainder.isEmpty()) {
                this.targetItem.setItem(remainder);
            } else {
                this.targetItem.discard();
            }
            if (remainder.isEmpty() || remainder.getCount() != before) {
                this.pech.level().playSound(null, this.targetItem.getX(), this.targetItem.getY(), this.targetItem.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.2F,
                        ((this.pech.getRandom().nextFloat() - this.pech.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
        }
    }
}
