package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BlockEntityEldritchObelisk extends BlockEntity {
    private static final int PULSE_INTERVAL = 20;
    private static final double BUFF_RANGE = 6.0;
    private static final int BUFF_DURATION = 40;

    private int counter;

    public BlockEntityEldritchObelisk(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ELDRITCH_OBELISK.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos) {
        if (counter++ % PULSE_INTERVAL != 0) {
            return;
        }
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(BUFF_RANGE));
        for (LivingEntity entity : nearby) {
            if (entity instanceof IEldritchMob && !entity.hasEffect(MobEffects.REGENERATION)) {
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFF_DURATION, 0, true, true));
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BUFF_DURATION, 0, true, true));
            }
        }
    }
}
