package com.leclowndu93150.thaumaturge.api.spell.cast;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFx;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * Everything a behaviour sees while its node runs. Spells only execute on the server.
 *
 * <p>A behaviour finishes its node in one of three ways: it calls {@link #proceed} to run the
 * children now, hands {@link #continuation()} to a carrier that resumes later, or does neither to
 * end the branch.
 *
 * @since 1.0.0
 */
public interface CastContext {
    /**
     * The level the spell runs in.
     *
     * @return the server level
     */
    ServerLevel level();

    /**
     * The casting entity, resolved lazily from the caster id.
     *
     * @return the caster, or null when it is gone or the spell has none
     */
    @Nullable
    LivingEntity caster();

    /**
     * The id of the casting entity.
     *
     * @return the id, empty when the spell has no caster
     */
    Optional<UUID> casterId();

    /**
     * The id shared by every node and carrier spawned from one cast.
     *
     * @return the cast id
     */
    UUID castId();

    /**
     * The node being run.
     *
     * @return the node
     */
    SpellNode node();

    /**
     * The key of the node's part.
     *
     * @return the part key
     */
    ResourceKey<SpellPart> partKey();

    /**
     * The node's part.
     *
     * @return the part
     */
    SpellPart part();

    /**
     * A setting of the node, falling back to the spec default and clamped to its range.
     *
     * @param key the setting key
     * @return the value, or 0 when the part declares no such setting
     */
    int setting(String key);

    /**
     * The node's effective aspect.
     *
     * @return the aspect, empty when the part takes none
     */
    Optional<Holder<IAspect>> aspect();

    /**
     * The tint for this node's visuals: the aspect colour when it has one, else the part colour.
     *
     * @return the packed {@code 0xRRGGBB} colour
     */
    int color();

    /**
     * The stats at this node, after the part's power multiplier.
     *
     * @return the state
     */
    SpellState state();

    /**
     * Shorthand for {@code state().power()}.
     *
     * @return the power multiplier
     */
    default float power() {
        return state().power();
    }

    /**
     * The level's random source.
     *
     * @return the random
     */
    RandomSource random();

    /**
     * The touch budget of this cast.
     *
     * @return the budget
     */
    TouchBudget budget();

    /**
     * The effects sink of this cast.
     *
     * @return the sink
     */
    SpellFx fx();

    /**
     * Whether an entity counts as friendly to the caster: the caster itself, its team, its pets,
     * its mount and, without PvP, other players.
     *
     * @param entity the entity
     * @return true when friendly
     */
    boolean isAlly(Entity entity);

    /**
     * The registries of the level.
     *
     * @return the registry access
     */
    RegistryAccess registries();

    /**
     * Whether the node has children to run.
     *
     * @return true when at least one child exists
     */
    boolean hasNext();

    /**
     * Runs every child now with the same targets and the current state.
     *
     * @param targets the targets handed on
     */
    void proceed(List<SpellTarget> targets);

    /**
     * Runs every child now with the same targets and another state.
     *
     * @param targets the targets handed on
     * @param state   the state handed on
     */
    void proceed(List<SpellTarget> targets, SpellState state);

    /**
     * Runs every child after a delay. Pending runs are not saved; they vanish when the level
     * unloads.
     *
     * @param ticks   the delay in ticks, at least 1
     * @param targets the targets handed on
     * @param state   the state handed on
     */
    void proceedLater(int ticks, List<SpellTarget> targets, SpellState state);

    /**
     * The children and current state packed for a carrier.
     *
     * @return the continuation
     */
    SpellContinuation continuation();

    /**
     * The children and another state packed for a carrier.
     *
     * @param state the state to pack
     * @return the continuation
     */
    SpellContinuation continuation(SpellState state);
}
