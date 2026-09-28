package com.leclowndu93150.thaumaturge.api.client.golems;

/**
 * The golem body parts a {@link GolemAccessoryRenderer} can draw on. The pose handed to the renderer
 * follows the chosen part: it moves with the golem's lean and, for {@link #HEAD}, with head yaw and
 * pitch. Units are blocks, {@code +Y} is up and {@code -Z} points out of the golem's front.
 *
 * @since 1.0.0
 */
public enum GolemAccessoryAnchor {
    /**
     * The head. The origin is the head pivot, 0.75 blocks above the golem's feet; the top of the
     * head is 0.1875 above the origin and its front face 0.09375 in front of it.
     */
    HEAD,

    /**
     * The body. The origin is the chest pivot, 0.5 blocks above the golem's feet; the top of the
     * chest is 0.25 above the origin.
     */
    BODY
}
