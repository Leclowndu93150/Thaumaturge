package com.leclowndu93150.thaumaturge.content.taint.entity.ability;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

public final class TaintLayingTrait implements MobTrait {
    private static final int LAY_THRESHOLD = 1;
    private static final int LAY_INTERVAL_MIN = 6000;
    private static final int LAY_INTERVAL_SPREAD = 6000;
    private static final float LAY_VOLUME = 1.0F;
    private static final float LAY_PITCH_CENTER = 1.0F;
    private static final float LAY_PITCH_SPREAD = 0.2F;

    @Override
    public void tick(LivingEntity mob) {
        if (!(mob instanceof Chicken chicken)
                || !(mob.level() instanceof ServerLevel level)
                || chicken.isBaby()
                || chicken.isChickenJockey()
                || chicken.eggTime > LAY_THRESHOLD) {
            return;
        }
        RandomSource random = chicken.getRandom();
        chicken.eggTime = LAY_INTERVAL_MIN + random.nextInt(LAY_INTERVAL_SPREAD);
        chicken.playSound(
                SoundEvents.CHICKEN_EGG,
                LAY_VOLUME,
                (random.nextFloat() - random.nextFloat()) * LAY_PITCH_SPREAD + LAY_PITCH_CENTER);
        chicken.spawnAtLocation(new ItemStack(TTItems.TAINTED_EGG.get()));
        chicken.gameEvent(GameEvent.ENTITY_PLACE);
    }
}
