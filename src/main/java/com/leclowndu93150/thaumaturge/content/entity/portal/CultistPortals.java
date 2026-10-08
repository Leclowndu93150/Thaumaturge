package com.leclowndu93150.thaumaturge.content.entity.portal;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class CultistPortals {
    public static final byte PULSE_EVENT = 16;
    public static final int PULSE_TICKS = 10;
    public static final float SOUND_VOLUME = 0.75F;
    public static final int AMBIENT_INTERVAL = 540;

    private static final float KNIGHT_CHANCE = 0.67F;
    private static final double TOUCH_RANGE_SQ = 3.0;
    private static final double ARRIVAL_LIFT = 0.25;
    private static final float ZAP_PITCH_SPREAD = 0.1F;

    private CultistPortals() {}

    public static @Nullable EntityCultist rollMinion(ServerLevel level, RandomSource random) {
        return random.nextFloat() < KNIGHT_CHANCE
                ? TTEntities.CULTIST_KNIGHT.get().create(level)
                : TTEntities.CULTIST_CLERIC.get().create(level);
    }

    public static int cultistsNear(Mob portal, double range) {
        return portal.level()
                .getEntitiesOfClass(EntityCultist.class, portal.getBoundingBox().inflate(range))
                .size();
    }

    public static void summon(Mob portal, ServerLevel level, Mob arrival) {
        RandomSource random = portal.getRandom();
        arrival.setPos(
                portal.getX() + random.nextFloat() - random.nextFloat(),
                portal.getY() + ARRIVAL_LIFT,
                portal.getZ() + random.nextFloat() - random.nextFloat());
        arrival.finalizeSpawn(
                level, level.getCurrentDifficultyAt(arrival.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(arrival);
        if (arrival instanceof EntityCultist cultist) {
            cultist.spawnCultistArrivalParticles();
        }
        arrival.playSound(TTSounds.WANDFAIL.get(), 1.0F, 1.0F);
    }

    public static void touch(Mob portal, Player player, float damage) {
        if (!(portal.level() instanceof ServerLevel level) || portal.distanceToSqr(player) >= TOUCH_RANGE_SQ) {
            return;
        }
        if (player.hurt(portal.damageSources().indirectMagic(portal, portal), damage)) {
            RandomSource random = portal.getRandom();
            portal.playSound(
                    TTSounds.ZAP.get(), 1.0F, (random.nextFloat() - random.nextFloat()) * ZAP_PITCH_SPREAD + 1.0F);
        }
    }

    public static void collapse(Mob portal, float power) {
        if (portal.level() instanceof ServerLevel level) {
            level.explode(portal, portal.getX(), portal.getY(), portal.getZ(), power, Level.ExplosionInteraction.NONE);
        }
    }
}
