package com.leclowndu93150.thaumaturge.api.golems.accessory;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import java.util.Optional;
import java.util.UUID;

/** Public callback context for one accessory namespace on one golem. */
public record GolemAccessoryContext(IGolemAPI golem, GolemAccessory accessory, GolemAccessoryState state) {
    public Optional<UUID> ownerIdentity() {
        return golem.ownerIdentity();
    }

    public boolean isInactive() {
        return golem.isInactive();
    }
}
