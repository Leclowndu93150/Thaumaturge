package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.cast.TouchBudget;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

final class CastRun {
    private final ServerLevel level;
    private final UUID castId;
    private final Optional<UUID> casterId;
    private final TouchBudget budget;
    private final SpellFxBatch fx;
    private final int nodeLimit;
    private @Nullable LivingEntity caster;
    private int nodes;

    CastRun(ServerLevel level, UUID castId, Optional<UUID> casterId, @Nullable LivingEntity caster) {
        this.level = level;
        this.castId = castId;
        this.casterId = casterId;
        this.caster = caster;
        this.budget = CastBudgets.of(level, castId);
        this.fx = new SpellFxBatch(level);
        this.nodeLimit = ThaumaturgeServerConfig.SPELL_MAX_NODES_PER_RUN.get();
    }

    ServerLevel level() {
        return level;
    }

    UUID castId() {
        return castId;
    }

    Optional<UUID> casterId() {
        return casterId;
    }

    @Nullable
    LivingEntity caster() {
        if (caster == null && casterId.isPresent() && level.getEntity(casterId.get()) instanceof LivingEntity living) {
            caster = living;
        }
        return caster;
    }

    TouchBudget budget() {
        return budget;
    }

    SpellFxBatch fx() {
        return fx;
    }

    boolean admitNode() {
        return ++nodes <= nodeLimit;
    }

    void finish() {
        fx.flush();
    }
}
