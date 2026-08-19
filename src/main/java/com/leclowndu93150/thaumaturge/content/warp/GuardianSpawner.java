package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;

public final class GuardianSpawner {
    private static final int SPAWN_ATTEMPTS = 50;
    private static final int MIN_OFFSET = 7;
    private static final int MAX_OFFSET = 24;

    private GuardianSpawner() {}

    public static void spawn(ServerPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            spawnGuardian(player);
        }
    }

    private static void spawnGuardian(ServerPlayer player) {
        ServerLevel level = player.level();
        RandomSource rand = player.getRandom();
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            BlockPos pos = randomOffset(player, rand);
            if (!level.getBlockState(pos.below()).isCollisionShapeFullBlock(level, pos.below())) {
                continue;
            }
            EntityEldritchGuardian guardian = TCEntities.ELDRITCH_GUARDIAN.get().create(level, EntitySpawnReason.EVENT);
            if (guardian == null) {
                return;
            }
            guardian.snapTo(pos.getX(), pos.getY(), pos.getZ(), rand.nextFloat() * 360.0F, 0.0F);
            if (!canPlace(level, guardian)) {
                guardian.discard();
                continue;
            }
            guardian.setTarget(player);
            level.addFreshEntity(guardian);
            return;
        }
    }

    public static void spawnPortal(ServerPlayer player) {
        ServerLevel level = player.level();
        RandomSource rand = player.getRandom();
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            BlockPos pos = randomOffset(player, rand);
            if (!level.getBlockState(pos.below()).isSolidRender()) {
                continue;
            }
            EntityCultistPortalLesser portal = TCEntities.CULTIST_PORTAL_LESSER.get().create(level, EntitySpawnReason.EVENT);
            if (portal == null) {
                return;
            }
            portal.snapTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
            if (!canPlace(level, portal)) {
                portal.discard();
                continue;
            }
            portal.finalizeSpawn(level, level.getCurrentDifficultyAt(portal.blockPosition()), EntitySpawnReason.EVENT, null);
            level.addFreshEntity(portal);
            WarpManager.sendActionBar(player, "warp.thaumaturge.text.16");
            return;
        }
    }

    private static BlockPos randomOffset(ServerPlayer player, RandomSource rand) {
        int x = Mth.floor(player.getX()) + Mth.nextInt(rand, MIN_OFFSET, MAX_OFFSET) * Mth.nextInt(rand, -1, 1);
        int y = Mth.floor(player.getY()) + Mth.nextInt(rand, MIN_OFFSET, MAX_OFFSET) * Mth.nextInt(rand, -1, 1);
        int z = Mth.floor(player.getZ()) + Mth.nextInt(rand, MIN_OFFSET, MAX_OFFSET) * Mth.nextInt(rand, -1, 1);
        return new BlockPos(x, y, z);
    }

    private static boolean canPlace(ServerLevel level, Mob mob) {
        return level.noCollision(mob) && !level.containsAnyLiquid(mob.getBoundingBox());
    }
}
