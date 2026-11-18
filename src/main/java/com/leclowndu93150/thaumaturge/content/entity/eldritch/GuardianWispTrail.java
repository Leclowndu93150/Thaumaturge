package com.leclowndu93150.thaumaturge.content.entity.eldritch;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class GuardianWispTrail {
    private static final int EMIT_INTERVAL = 3;
    private static final double ANCHOR_HEIGHT_FRACTION = 0.2;
    private static final double JITTER = 0.4;
    private static final float RED = 0.18F;
    private static final float GREEN = 0.08F;
    private static final float BLUE = 0.24F;
    private static final double RISE_SPEED = 0.02;
    private static final float DRIFT = -0.02F;

    private GuardianWispTrail() {}

    public static void emit(ServerLevel level, LivingEntity guardian) {
        if (guardian.tickCount % EMIT_INTERVAL != 0) {
            return;
        }
        RandomSource random = guardian.getRandom();
        Vec3 feet = LevelSpaces.trackedFrame(guardian).toLocal(guardian.position());
        Vec3 origin = feet.add(
                random.triangle(0.0, JITTER),
                guardian.getBbHeight() * ANCHOR_HEIGHT_FRACTION + random.triangle(0.0, JITTER),
                random.triangle(0.0, JITTER));
        Effects.wispyMotes(level, origin)
                .color(RED, GREEN, BLUE)
                .motion(0.0, RISE_SPEED, 0.0)
                .gravity(DRIFT)
                .send();
    }
}
