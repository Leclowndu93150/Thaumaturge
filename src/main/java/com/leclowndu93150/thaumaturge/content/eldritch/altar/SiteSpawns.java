package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.content.eldritch.EldritchSpawns;
import com.leclowndu93150.thaumaturge.content.eldritch.portal.SafeSpot;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

final class SiteSpawns {
    private static final int ATTEMPTS = 16;
    private static final int MAX_RISE = 6;
    private static final int HOME_RADIUS = 16;

    private SiteSpawns() {}

    static Optional<Mob> spawn(
            ServerLevel level, BlockPos altar, EntityType<?> type, int minRadius, int maxRadius, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return Optional.empty();
        }
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            int radius = minRadius + random.nextInt(Math.max(1, maxRadius - minRadius + 1));
            int x = altar.getX() + Math.round(Mth.cos(angle) * radius);
            int z = altar.getZ() + Math.round(Mth.sin(angle) * radius);
            Optional<BlockPos> spot =
                    SafeSpot.surface(level, x, z).filter(pos -> Math.abs(pos.getY() - altar.getY()) <= MAX_RISE);
            if (spot.isEmpty()) {
                continue;
            }
            Optional<Mob> prepared = EldritchSpawns.prepare(level, type, spot.get(), altar, HOME_RADIUS, random);
            if (prepared.isEmpty()) {
                return Optional.empty();
            }
            Mob mob = prepared.get();
            mob.setData(TTAttachments.OBELISK_SITE_MEMBER, altar.immutable());
            return level.addFreshEntity(mob) ? Optional.of(mob) : Optional.empty();
        }
        return Optional.empty();
    }

    static List<Mob> members(ServerLevel level, BlockPos altar, double radius) {
        return level.getEntitiesOfClass(
                Mob.class,
                new AABB(altar).inflate(radius),
                mob -> mob.isAlive()
                        && mob.hasData(TTAttachments.OBELISK_SITE_MEMBER)
                        && mob.getData(TTAttachments.OBELISK_SITE_MEMBER).equals(altar));
    }
}
