package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.particle.GolemEmoteParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public enum GolemEvent {
    TASK_CLAIMED(GolemEvent.HANDOFF_TASK_ID, GolemEvent.TASK_LOOK), TASK_FAILED(GolemEvent.FAILED_ID, GolemEvent.FAILED_LOOK), CONFUSED(GolemEvent.CONFUSED_ID,
            GolemEvent.CONFUSED_LOOK), STAY(GolemEvent.STAY_ID, GolemEvent.STAY_LOOK), FOLLOW(GolemEvent.FOLLOW_ID, GolemEvent.TASK_LOOK), RANK_UP(GolemEvent.RANK_UP_ID, GolemEvent.HEARTS_LOOK);

    public static final byte HANDOFF_TASK_ID = 5;

    private static final byte FAILED_ID = 101;
    private static final byte CONFUSED_ID = 102;
    private static final byte STAY_ID = 103;
    private static final byte FOLLOW_ID = 104;
    private static final byte RANK_UP_ID = 105;

    private static final int TASK_LOOK = 0;
    private static final int FAILED_LOOK = 1;
    private static final int CONFUSED_LOOK = 2;
    private static final int STAY_LOOK = 3;
    private static final int HEARTS_LOOK = 4;

    private static final int WHITE = 0xFFFFFF;
    private static final int CYAN = 0x38E8F5;
    private static final int YELLOW = 0xFFF21A;
    private static final float ICON_SCALE = 2.0F;
    private static final double ICON_LIFT = 0.15D;
    private static final int TASK_TICKS = 7;
    private static final int FAILED_TICKS = 10;
    private static final double FAILED_RISE = 0.008D;
    private static final int CONFUSED_TICKS = 10;
    private static final double CONFUSED_RISE = 0.02D;
    private static final int STAY_TICKS = 20;
    private static final double STAY_RISE = 0.006D;
    private static final int HEART_MIN_COUNT = 4;
    private static final int HEART_EXTRA_COUNT = 3;
    private static final int HEART_MIN_TICKS = 20;
    private static final int HEART_EXTRA_TICKS = 20;
    private static final float HEART_MIN_SCALE = 0.35F;
    private static final float HEART_EXTRA_SCALE = 0.4F;
    private static final double HEART_SCATTER = 0.012D;
    private static final double HEART_MAX_RISE = 0.025D;
    private static final double HEART_LIFT = 0.05D;

    private final byte id;
    private final int look;

    GolemEvent(byte id, int look) {
        this.id = id;
        this.look = look;
    }

    public byte id() {
        return id;
    }

    public static @Nullable GolemEvent byId(byte id) {
        for (GolemEvent event : values()) {
            if (event.id == id) {
                return event;
            }
        }
        return null;
    }

    public static boolean emotesEnabled() {
        return ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get();
    }

    public void broadcast(Entity entity) {
        Level level = entity.level();
        if (level.isClientSide() || !emotesEnabled()) {
            return;
        }
        level.broadcastEntityEvent(entity, id);
    }

    public void play(Entity entity) {
        Level level = entity.level();
        double top = entity.getBoundingBox().maxY;
        switch (look) {
            case TASK_LOOK -> icon(level, entity, top, GolemEmoteParticleOptions.ICON_TASK, WHITE, TASK_TICKS, 0.0D);
            case FAILED_LOOK -> icon(level, entity, top, GolemEmoteParticleOptions.ICON_FAIL, CYAN, FAILED_TICKS, FAILED_RISE);
            case CONFUSED_LOOK -> icon(level, entity, top, GolemEmoteParticleOptions.ICON_CONFUSED, WHITE, CONFUSED_TICKS, CONFUSED_RISE);
            case STAY_LOOK -> icon(level, entity, top, GolemEmoteParticleOptions.ICON_STAY, YELLOW, STAY_TICKS, STAY_RISE);
            default -> hearts(level, entity, top);
        }
    }

    private static void icon(Level level, Entity entity, double top, int icon, int color, int ticks, double rise) {
        GolemEmoteParticleOptions options = new GolemEmoteParticleOptions(color, icon, ticks, ICON_SCALE);
        level.addParticle(options, entity.getX(), top + ICON_LIFT, entity.getZ(), 0.0D, rise, 0.0D);
    }

    private static void hearts(Level level, Entity entity, double top) {
        RandomSource random = level.getRandom();
        int count = HEART_MIN_COUNT + random.nextInt(HEART_EXTRA_COUNT);
        for (int i = 0; i < count; i++) {
            int ticks = HEART_MIN_TICKS + random.nextInt(HEART_EXTRA_TICKS);
            float scale = HEART_MIN_SCALE + random.nextFloat() * HEART_EXTRA_SCALE;
            GolemEmoteParticleOptions options = new GolemEmoteParticleOptions(WHITE, GolemEmoteParticleOptions.ICON_HEART, ticks, scale);
            double vx = random.nextGaussian() * HEART_SCATTER;
            double vz = random.nextGaussian() * HEART_SCATTER;
            double vy = random.nextDouble() * HEART_MAX_RISE;
            level.addParticle(options, entity.getX(), top + HEART_LIFT, entity.getZ(), vx, vy, vz);
        }
    }
}
