package com.leclowndu93150.thaumaturge.api.golems.accessory;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Server-side behaviour for a {@link GolemAccessory}, with a typed state value the golem keeps for
 * each worn accessory.
 *
 * <p>The state {@code S} is an immutable value, typically a record. Callbacks receive the current
 * state and return the next one; the golem compares them with {@link Object#equals} to decide
 * whether anything changed. The state is saved with the golem through {@link #stateCodec()}, so it
 * survives saving, loading and chunk unloading. When {@link #syncCodec()} returns a codec, the
 * state is also sent to clients tracking the golem whenever it changes, and
 * {@code IGolemAPI#accessoryState} returns it on the client as well.
 *
 * <p>All callbacks run on the server thread. Loading or unloading a golem is not an attachment
 * change and calls neither {@link #onAttach} nor {@link #onRemove}; the saved state is restored
 * instead. Death, pickup and any other removal call {@link #onRemove} once, after which the state
 * is discarded.
 *
 * <p>A behaviour instance belongs to one accessory: state lookups use the instance as the key.
 *
 * @param <S> the state type
 * @since 1.0.0
 */
public interface GolemAccessoryBehavior<S> {
    /**
     * The codec that saves the state with the golem.
     *
     * @return the state codec
     */
    Codec<S> stateCodec();

    /**
     * The state a newly attached accessory starts from, and the state used when a saved golem has
     * none for this accessory.
     *
     * @return the initial state, never null
     */
    S initialState();

    /**
     * The codec that sends the state to clients, or null to keep it on the server. The encoded
     * state of all synced accessories on one golem must stay under 1 KiB; a golem whose synced
     * states grow past that stops sending updates and logs an error.
     *
     * @return the sync codec, or null
     */
    default @Nullable StreamCodec<RegistryFriendlyByteBuf, S> syncCodec() {
        return null;
    }

    /**
     * Called once when the accessory is put on a golem.
     *
     * @param context       the golem and accessory
     * @param state         the initial state
     * @param attachedStack a one-item copy of the accessory item that was used
     * @return the state to keep
     */
    default S onAttach(GolemAccessoryContext context, S state, ItemStack attachedStack) {
        return state;
    }

    /**
     * Called once before the accessory item is dropped from the golem. Changes to
     * {@code returnedStack} are kept, except that its count stays one.
     *
     * @param context       the golem and accessory
     * @param state         the final state
     * @param returnedStack the accessory item about to be dropped
     */
    default void onRemove(GolemAccessoryContext context, S state, ItemStack returnedStack) {}

    /**
     * Called once per server tick while the golem is loaded. Never called on the client.
     *
     * @param context the golem and accessory
     * @param state   the current state
     * @return the state to keep
     */
    default S serverTick(GolemAccessoryContext context, S state) {
        return state;
    }
}
