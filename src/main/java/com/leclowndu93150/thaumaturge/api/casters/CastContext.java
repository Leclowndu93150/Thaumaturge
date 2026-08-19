package com.leclowndu93150.thaumaturge.api.casters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * The execution scope a focus element runs in: the level, the caster, the accumulated power,
 * and the continuation of the spell after the current node. Elements read from it; only the
 * engine advances it.
 *
 * @since 1.0.0
 */
public final class CastContext {
    private final Level level;
    private final UUID castId;
    private final @Nullable UUID casterId;
    private final List<FocusUnit> program;
    private @Nullable LivingEntity caster;
    private float power;
    private int cursor;

    CastContext(Level level, UUID castId, @Nullable UUID casterId, @Nullable LivingEntity caster, float power, List<FocusUnit> program) {
        this.level = level;
        this.castId = castId;
        this.casterId = casterId;
        this.caster = caster;
        this.power = power;
        this.program = program;
    }

    /**
     * The level the spell executes in.
     *
     * @return the level, never null
     */
    public Level level() {
        return level;
    }

    /**
     * The id of this cast, shared by every branch spawned from the same cast.
     *
     * @return the cast id, never null
     */
    public UUID castId() {
        return castId;
    }

    /**
     * The id of the casting entity.
     *
     * @return the caster id, or null when the spell has no caster
     */
    public @Nullable UUID casterId() {
        return casterId;
    }

    /**
     * The casting entity, resolved from the caster id through the level when not already
     * known.
     *
     * @return the caster, or null when it cannot be resolved
     */
    public @Nullable LivingEntity caster() {
        if (caster == null && casterId != null && level.getEntity(casterId) instanceof LivingEntity living) {
            caster = living;
        }
        return caster;
    }

    /**
     * The accumulated power at the current node, after every preceding node's multiplier.
     *
     * @return the current power
     */
    public float power() {
        return power;
    }

    /**
     * The remainder of the spell after the current node, for mediums that resolve their
     * targets later through an intermediary. The continuation carries the current power and
     * the caster id.
     *
     * @return the remaining package, or null when the current node is last
     */
    public @Nullable FocusPackage continuation() {
        if (cursor + 1 >= program.size()) {
            return null;
        }
        return new FocusPackage(power, 0, Optional.ofNullable(casterId), program.subList(cursor + 1, program.size()));
    }

    /**
     * The element ids of every effect in the executing spell, descending into split
     * branches. Used by mediums that render from the effects they deliver.
     *
     * @return the effect ids in encounter order; empty when none
     */
    public List<Identifier> effects() {
        return FocusEngine.effectIds(program);
    }

    List<FocusUnit> program() {
        return program;
    }

    void foldPower(float multiplier) {
        power *= multiplier;
    }

    void moveTo(int cursor) {
        this.cursor = cursor;
    }

    CastContext branch(float branchPower, List<FocusUnit> branchProgram) {
        return new CastContext(level, castId, casterId, caster, branchPower, branchProgram);
    }
}
