package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * The surface a golem exposes to seal behaviours, tasks, part abilities and accessories.
 *
 * <p>Unless stated otherwise, mutating methods are server-side only and must be called on the server thread.
 *
 * @since 1.0.0
 */
public interface IGolemAPI {
    /**
     * @return the golem as a living entity
     */
    LivingEntity asEntity();

    /**
     * @return the level the golem lives in
     */
    Level level();

    /**
     * @return the golem's current build; an immutable value
     */
    IGolemProperties properties();

    /**
     * Replaces the golem's build, syncs it to clients and refreshes attributes and goals derived from it.
     *
     * @param properties the new build
     */
    void setProperties(IGolemProperties properties);

    /**
     * @return the golem's carry slots
     */
    IGolemHands hands();

    /**
     * Awards rank experience. Only golems with the smart trait accumulate it; ranking up plays a sound and an emote.
     *
     * @param xp the experience amount
     */
    void addRankXp(int xp);

    /**
     * @return the golem's dye colour index, from 1 to 16, or 0 when it has none
     */
    byte color();

    /**
     * Plays the main-hand swing animation for every watcher.
     */
    void swingArm();

    /**
     * @return whether the golem currently has an attack target
     */
    boolean isInCombat();

    /**
     * The UUID of the golem's owner, available while the owner is offline.
     *
     * @return the owner's UUID, or empty when the golem has no owner
     * @since 1.0.0
     */
    Optional<UUID> ownerIdentity();

    /**
     * The state a worn accessory's behaviour currently holds on this golem. On the server this is
     * the authoritative state; on the client it is the last synced state and is only present for
     * behaviours with a {@link GolemAccessoryBehavior#syncCodec()}.
     *
     * @param behavior the behaviour of the accessory, used as the key
     * @param <S>      the state type
     * @return the state, or empty when the golem does not wear an accessory with this behaviour
     * @since 1.0.0
     */
    <S> Optional<S> accessoryState(GolemAccessoryBehavior<S> behavior);

    /**
     * Replaces a worn accessory's state from outside its callbacks, for example from an addon's
     * interaction handler. Server side only; the new state is saved and, when the behaviour
     * syncs, sent to clients.
     *
     * @param behavior the behaviour of the accessory, used as the key
     * @param update   maps the current state to the new one; must not return null
     * @param <S>      the state type
     * @return true when the golem wears an accessory with this behaviour and the update ran
     * @throws IllegalStateException when called on the client
     * @since 1.0.0
     */
    <S> boolean updateAccessoryState(GolemAccessoryBehavior<S> behavior, UnaryOperator<S> update);
}
