package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.cast.TouchBudget;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class CastBudgets {
    private static final int PRUNE_THRESHOLD = 64;

    private final Map<UUID, Tally> tallies = new HashMap<>();

    static TouchBudget of(ServerLevel level, UUID castId) {
        return level.getData(TTAttachments.SPELL_BUDGETS).budget(level, castId);
    }

    private TouchBudget budget(ServerLevel level, UUID castId) {
        long now = level.getGameTime();
        if (tallies.size() > PRUNE_THRESHOLD) {
            tallies.values().removeIf(tally -> tally.tick < now);
        }
        Tally tally = tallies.computeIfAbsent(castId, id -> new Tally());
        if (tally.tick != now) {
            tally.reset(now);
        }
        return tally;
    }

    private static final class Tally implements TouchBudget {
        private final Set<Integer> entities = new HashSet<>();
        private int blocks;
        private long tick = Long.MIN_VALUE;

        private void reset(long now) {
            tick = now;
            entities.clear();
            blocks = 0;
        }

        @Override
        public boolean claim(Entity entity) {
            if (!entities.add(entity.getId())) {
                entity.invulnerableTime = 0;
                return true;
            }
            if (entities.size() > ThaumaturgeServerConfig.SPELL_MAX_ENTITIES_PER_TICK.get()) {
                entities.remove(entity.getId());
                return false;
            }
            return true;
        }

        @Override
        public boolean claim(BlockPos pos) {
            return ++blocks <= ThaumaturgeServerConfig.SPELL_MAX_BLOCKS_PER_TICK.get();
        }
    }
}
