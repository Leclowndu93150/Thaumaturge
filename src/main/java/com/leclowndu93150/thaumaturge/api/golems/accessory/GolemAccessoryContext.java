package com.leclowndu93150.thaumaturge.api.golems.accessory;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import java.util.Objects;

/**
 * The golem and accessory a {@link GolemAccessoryBehavior} callback runs for.
 *
 * @param golem     the golem wearing the accessory
 * @param accessory the accessory
 * @since 1.0.0
 */
public record GolemAccessoryContext(IGolemAPI golem, GolemAccessory accessory) {
    /**
     * Validates the components.
     *
     * @throws NullPointerException when a component is null
     */
    public GolemAccessoryContext {
        Objects.requireNonNull(golem, "golem");
        Objects.requireNonNull(accessory, "accessory");
    }
}
