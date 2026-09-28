package com.leclowndu93150.thaumaturge.api.golems.accessory;

import java.util.Optional;

/**
 * Read access to the behaviour states of the accessories one golem wears, as a client sees them.
 * Only states whose behaviour has a {@link GolemAccessoryBehavior#syncCodec()} are present.
 *
 * @since 1.0.0
 */
public interface GolemAccessoryStateView {
    /**
     * The synced state of a worn accessory's behaviour.
     *
     * @param behavior the behaviour of the accessory, used as the key
     * @param <S>      the state type
     * @return the state, or empty when no worn accessory has this behaviour or its state is not
     *         synced
     */
    <S> Optional<S> state(GolemAccessoryBehavior<S> behavior);
}
