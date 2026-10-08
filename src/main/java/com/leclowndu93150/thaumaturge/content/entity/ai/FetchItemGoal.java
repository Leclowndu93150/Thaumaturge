package com.leclowndu93150.thaumaturge.content.entity.ai;

import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class FetchItemGoal<T extends PathfinderMob & ItemCollector> extends Goal {
    private static final double SEARCH_RADIUS = 16.0;
    private static final double GRAB_REACH_SQ = 1.5;
    private static final double PACE = 1.5;
    private static final int REPATH_DELAY = 4;
    private static final int REPATH_JITTER = 4;
    private static final float LOOK_TURN = 30.0F;
    private static final float GRAB_VOLUME = 0.2F;

    private final T collector;
    private @Nullable ItemEntity quarry;
    private int patience;

    public FetchItemGoal(T collector) {
        this.collector = collector;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (--patience > 0) {
            return false;
        }
        quarry = collector
                .level()
                .getEntitiesOfClass(
                        ItemEntity.class, collector.getBoundingBox().inflate(SEARCH_RADIUS), collector::wantsToCollect)
                .stream()
                .filter(this::withinReach)
                .min(Comparator.comparingDouble(item -> item.distanceToSqr(collector)))
                .orElse(null);
        return quarry != null;
    }

    @Override
    public boolean canContinueToUse() {
        return quarry != null && quarry.isAlive() && !collector.getNavigation().isDone() && withinReach(quarry);
    }

    @Override
    public void start() {
        patience = 0;
        chase();
    }

    @Override
    public void stop() {
        quarry = null;
    }

    @Override
    public void tick() {
        if (quarry == null) {
            return;
        }
        collector.getLookControl().setLookAt(quarry, LOOK_TURN, LOOK_TURN);
        if (collector.getSensing().hasLineOfSight(quarry) && --patience <= 0) {
            patience = REPATH_DELAY + collector.getRandom().nextInt(REPATH_JITTER);
            chase();
        }
        if (collector.distanceToSqr(quarry.getX(), quarry.getBoundingBox().minY, quarry.getZ()) <= GRAB_REACH_SQ) {
            grab(quarry);
        }
    }

    private boolean withinReach(ItemEntity item) {
        return item.distanceToSqr(collector) <= SEARCH_RADIUS * SEARCH_RADIUS;
    }

    private void chase() {
        if (quarry != null) {
            collector.getNavigation().moveTo(quarry, collector.getAttributeValue(Attributes.MOVEMENT_SPEED) * PACE);
        }
    }

    private void grab(ItemEntity item) {
        patience = 0;
        int offered = item.getItem().getCount();
        ItemStack leftover = collector.collect(item.getItem().copy());
        if (leftover.isEmpty()) {
            item.discard();
        } else {
            item.setItem(leftover);
        }
        if (leftover.getCount() != offered) {
            RandomSource random = collector.getRandom();
            collector
                    .level()
                    .playSound(
                            null,
                            item.getX(),
                            item.getY(),
                            item.getZ(),
                            SoundEvents.ITEM_PICKUP,
                            SoundSource.NEUTRAL,
                            GRAB_VOLUME,
                            ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }
    }
}
