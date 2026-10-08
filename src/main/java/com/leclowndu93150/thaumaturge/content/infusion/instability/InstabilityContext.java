package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record InstabilityContext(ServerLevel level, BlockPos matrix, List<BlockPos> pedestals) {
    private static final double EVENT_RANGE = 10.0;
    private static final int ARC_COLOR = 0x4C004C;
    private static final float ZAP_VOLUME = 0.1F;
    private static final float ZAP_PITCH_VARIATION = 0.2F;

    public RandomSource random() {
        return level.getRandom();
    }

    public <T extends Entity> List<T> nearby(Class<T> type) {
        return level.getEntitiesOfClass(type, new AABB(matrix).inflate(EVENT_RANGE));
    }

    public void arcTo(Vec3 target) {
        EffectDispatch.spawnArc(level, Vec3.atCenterOf(matrix), target, ARC_COLOR, 0.0F);
    }

    public void zapSound() {
        level.playSound(
                null,
                matrix,
                TTSounds.ZAP.get(),
                SoundSource.BLOCKS,
                ZAP_VOLUME,
                1.0F + random().nextFloat() * ZAP_PITCH_VARIATION);
    }
}
