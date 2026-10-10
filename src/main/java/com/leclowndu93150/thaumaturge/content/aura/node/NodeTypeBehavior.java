package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityBrainyZombie;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.EventHooks;

final class NodeTypeBehavior {
    private static final int TAINT_SPREAD_RANGE = 4;
    private static final int POLLUTE_INTERVAL = 200;
    private static final float POLLUTE_SATURATION_FACTOR = 0.8F;
    private static final double POLLUTE_BASE_DIVISOR = 3.0;
    private static final float POLLUTE_STRENGTH_STEP = 0.2F;
    private static final float POLLUTE_MINIMUM_STRENGTH = 1.0F;
    private static final float PURE_FLUX_TAKE = 0.25F;
    private static final float PURE_ERODE_CHANCE = 0.025F;
    private static final double GUARD_PLAYER_RANGE = 24.0;
    private static final double GUARD_COUNT_HORIZONTAL = 10.0;
    private static final double GUARD_COUNT_VERTICAL = 6.0;
    private static final int GUARD_LIMIT = 4;
    private static final int SPAWNER_EFFECT_EVENT = 2004;
    private static final double BLOCK_CENTER = 0.5;
    private static final double GUARD_PLACEMENT_REACH = 5.0;
    private static final int GUARD_VERTICAL_REACH = 1;
    private static final float WHOLE_TURN = 360.0F;

    private NodeTypeBehavior() {}

    static boolean tick(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        return switch (node.kind()) {
            case TAINTED -> {
                tainted(node, level, pos, random);
                yield false;
            }
            case PURE -> pure(node, level, pos, random);
            case DARK -> {
                if (random.nextBoolean()) {
                    spawnGuard(level, pos, random);
                }
                yield false;
            }
            case NORMAL, UNSTABLE, HUNGRY -> false;
        };
    }

    private static void tainted(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextBoolean()) {
            TaintHelper.attemptFibreGrowth(level, pos.offset(taintOffset(random), taintOffset(random), taintOffset(random)), true);
        }
        if (node.tickCounter % POLLUTE_INTERVAL != 0) {
            return;
        }
        float saturation = AuraHelper.getFlux(level, pos) / Math.max(1, AuraHelper.getAuraBase(level, pos));
        if (random.nextFloat() <= POLLUTE_SATURATION_FACTOR * saturation) {
            return;
        }
        double root = Math.sqrt(Math.max(1.0, node.aspectsBase.totalAmount() / POLLUTE_BASE_DIVISOR));
        int steps = (int) Math.max(1.0, root);
        AuraHelper.polluteAura(level, pos, Math.max(POLLUTE_MINIMUM_STRENGTH, POLLUTE_STRENGTH_STEP * steps), true);
    }

    private static int taintOffset(RandomSource random) {
        return random.nextInt(TAINT_SPREAD_RANGE * 2 + 1) - TAINT_SPREAD_RANGE;
    }

    private static boolean pure(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        if (AuraHelper.drainFlux(level, pos, PURE_FLUX_TAKE, false) <= 0.0F || node.tickCounter % POLLUTE_INTERVAL != 0 || random.nextFloat() >= PURE_ERODE_CHANCE) {
            return false;
        }
        List<AspectInstance> entries = node.aspectsBase.entries();
        if (!entries.isEmpty()) {
            Holder<IAspect> eroded = entries.get(random.nextInt(entries.size())).aspect();
            node.aspectsBase = node.aspectsBase.remove(eroded, 1);
            int cap = node.aspectsBase.amountOf(eroded);
            int held = node.held.amountOf(eroded);
            if (held > cap) {
                node.held = cap == 0 ? node.held.without(eroded) : node.held.remove(eroded, held - cap);
            }
            node.invalidateRefill();
        }
        if (node.aspectsBase.isEmpty()) {
            node.removeDepleted(level, pos);
            return true;
        }
        return false;
    }

    private static void spawnGuard(ServerLevel level, BlockPos pos, RandomSource random) {
        double centreX = pos.getX() + BLOCK_CENTER;
        double centreY = pos.getY() + BLOCK_CENTER;
        double centreZ = pos.getZ() + BLOCK_CENTER;
        if (!level.hasNearbyAlivePlayer(centreX, centreY, centreZ, GUARD_PLAYER_RANGE) || guardsNear(level, pos) >= GUARD_LIMIT) {
            return;
        }
        EntityBrainyZombie guard = TTEntities.BRAINY_ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
        if (guard == null) {
            return;
        }
        double x = centreX + (random.nextDouble() * 2.0 - 1.0) * GUARD_PLACEMENT_REACH;
        double z = centreZ + (random.nextDouble() * 2.0 - 1.0) * GUARD_PLACEMENT_REACH;
        int y = pos.getY() + random.nextInt(GUARD_VERTICAL_REACH * 2 + 1) - GUARD_VERTICAL_REACH;
        guard.snapTo(x, y, z, random.nextFloat() * WHOLE_TURN, 0.0F);
        if (!guard.checkSpawnRules(level, EntitySpawnReason.EVENT) || !guard.checkSpawnObstruction(level)) {
            guard.discard();
            return;
        }
        EventHooks.finalizeMobSpawn(guard, level, level.getCurrentDifficultyAt(guard.blockPosition()), EntitySpawnReason.EVENT, null);
        level.addFreshEntity(guard);
        level.levelEvent(SPAWNER_EFFECT_EVENT, pos, 0);
    }

    private static int guardsNear(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(GUARD_COUNT_HORIZONTAL, GUARD_COUNT_VERTICAL, GUARD_COUNT_HORIZONTAL);
        return level.getEntities(TTEntities.BRAINY_ZOMBIE.get(), area, EntitySelector.NO_SPECTATORS).size();
    }
}
