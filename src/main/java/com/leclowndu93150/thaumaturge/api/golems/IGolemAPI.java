package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * The surface a golem exposes to seal behaviours, tasks, part abilities and accessories.
 *
 * <p>Unless stated otherwise, mutating methods are server-side only and must be called on the server thread.
 *
 * @since 1.0.0
 */
public interface IGolemAPI {
    /** The active exclusive duty id, or empty. Only this presentation value is synced to clients. */
    default Optional<Identifier> duty() {
        return Optional.empty();
    }

    /**
     * Enters a registered duty for an equipped accessory. Only a nearby owner may enter;
     * an existing duty wins conflicts. State is copied. Server thread only.
     */
    default boolean tryEnterDuty(ServerPlayer actor, Identifier duty, CompoundTag state) {
        return false;
    }

    /** Leaves the current duty and restores the preceding home and follow mode. Server thread only. */
    default boolean exitDuty(ServerPlayer actor) {
        return false;
    }

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
     * @param <T>      the state type
     * @return the state, or empty when the golem does not wear an accessory with this behaviour
     * @since 1.0.0
     */
    <T> Optional<T> accessoryState(GolemAccessoryBehavior<T> behavior);

    /**
     * Replaces a worn accessory's state from outside its callbacks, for example from an addon's
     * interaction handler. Server side only; the new state is saved and, when the behaviour
     * syncs, sent to clients.
     *
     * @param behavior the behaviour of the accessory, used as the key
     * @param transform maps the current state to the new one; must not return null
     * @param <T>      the state type
     * @return true when the golem wears an accessory with this behaviour and the update ran
     * @throws IllegalStateException when called on the client
     * @since 1.0.0
     */
    <T> boolean updateAccessoryState(GolemAccessoryBehavior<T> behavior, UnaryOperator<T> transform);

    /**
     * @return the level the golem lives in
     */
    Level level();

    /**
     * @return the golem as a living entity
     */
    LivingEntity asEntity();

    /**
     * @return whether the golem currently has an attack target
     */
    boolean isInCombat();

    /**
     * Plays the main-hand swing animation for every watcher.
     */
    void swingArm();

    /**
     * @return the golem's carry slots
     */
    IGolemHands hands();

    /**
     * @return the golem's dye colour index, from 1 to 16, or 0 when it has none
     */
    byte color();

    /**
     * Awards rank experience. Only golems with the smart trait accumulate it; ranking up plays a sound and an emote.
     *
     * @param amount the experience amount
     */
    void addRankXp(int amount);

    /**
     * Replaces the golem's build, syncs it to clients and refreshes attributes and goals derived from it.
     *
     * @param build the new build
     */
    void setProperties(IGolemProperties build);

    /**
     * @return the golem's current build; an immutable value
     */
    IGolemProperties properties();
}
