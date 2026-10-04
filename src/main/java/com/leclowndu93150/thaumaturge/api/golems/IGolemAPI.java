package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The surface a golem entity exposes to seals, tasks and part functions.
 *
 * @since 1.0.0
 */
public interface IGolemAPI {
    /**
     * @return the golem as a living entity
     */
    LivingEntity getGolemEntity();

    /**
     * @return the golem's current composition
     */
    IGolemProperties getProperties();

    /**
     * Replaces the golem's composition and refreshes derived attributes.
     *
     * @param properties the new composition
     */
    void setProperties(IGolemProperties properties);

    /**
     * @return the level the golem lives in
     */
    Level getGolemWorld();

    /**
     * Attempts to store a stack in the golem's carry slots.
     *
     * @param stack the stack to store; may be partially consumed
     * @return the remainder that did not fit
     */
    ItemStack holdItem(ItemStack stack);

    /**
     * Removes carried items.
     *
     * @param stack the stack to match and count against, or an empty stack to remove any
     * @return the removed items
     */
    ItemStack dropItem(ItemStack stack);

    /**
     * @param stack   the stack to test
     * @param partial whether carrying only part of the stack counts
     * @return whether the golem has room for the stack
     */
    boolean canCarry(ItemStack stack, boolean partial);

    /**
     * @param stack the stack to test
     * @return how many items of the stack the golem could still carry
     */
    int canCarryAmount(ItemStack stack);

    /**
     * @param stack the stack to match
     * @return whether the golem currently carries a matching stack
     */
    boolean isCarrying(ItemStack stack);

    /**
     * @return the golem's carry slots
     */
    List<ItemStack> getCarrying();

    /**
     * Awards rank experience. Only golems with the {@link GolemTrait#SMART} trait accumulate it.
     *
     * @param xp the experience amount
     */
    void addRankXp(int xp);

    /**
     * @return the golem's assigned dye color index, or 0 when uncolored
     */
    byte getGolemColor();

    /**
     * Plays the golem's arm swing animation and syncs it to watchers.
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
