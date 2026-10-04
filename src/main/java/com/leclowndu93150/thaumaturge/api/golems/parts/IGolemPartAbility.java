package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;

/**
 * Behaviour a golem part adds to the golem that carries it.
 *
 * @since 1.0.0
 */
public interface IGolemPartAbility {
    /**
     * Runs once per golem tick on both the server and the client, after the golem's own tick.
     *
     * @param golem the golem carrying the part
     */
    void tick(IGolemAPI golem);
}
