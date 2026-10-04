package com.leclowndu93150.thaumaturge.api.client.golems;

/**
 * The golem body parts a {@link GolemAccessoryRenderer} can draw on. The pose handed to the renderer
 * follows the chosen part: it moves with the golem's lean and, for {@link #HEAD}, with head yaw and
 * pitch. Units are unscaled model blocks (16 vanilla model pixels), {@code +Y} is up and
 * {@code -Z} points out of the golem's front. The renderer applies the golem's 0.6 scale.
 *
 * @since 1.0.0
 */
public enum GolemAccessoryAnchor {
    /**
     * The vanilla head pivot. The shell extends 5/16 above the origin, 4/16 to each side,
     * and 5/16 forward and back. The antenna is hidden while a HAT accessory is worn.
     */
    HEAD,

    /**
     * The vanilla body pivot at the bottom of the torso. The chest extends 6/16 upward,
     * 4/16 to each side and 3/16 forward and back.
     */
    BODY
}
