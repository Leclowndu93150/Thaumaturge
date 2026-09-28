package com.leclowndu93150.thaumaturge.api.client.golems;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryStateView;
import java.util.Objects;

/**
 * What a {@link GolemAccessoryRenderer} knows about the golem it draws on.
 *
 * @param lightCoords the packed light at the golem
 * @param ageInTicks  the golem's age in ticks, including the partial tick, for animation
 * @param states      the synced behaviour states of the accessories the golem wears
 * @since 1.0.0
 */
public record GolemAccessoryRenderContext(int lightCoords, float ageInTicks, GolemAccessoryStateView states) {
    /**
     * Validates the components.
     *
     * @throws NullPointerException when {@code states} is null
     */
    public GolemAccessoryRenderContext {
        Objects.requireNonNull(states, "states");
    }
}
