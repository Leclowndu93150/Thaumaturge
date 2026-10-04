package com.leclowndu93150.thaumaturge.api.golems.seals;

/**
 * A page of the seal configuration screen. The ordinal is the page id used by the menu and by the
 * {@code button.category.<ordinal>.desc} translation keys.
 *
 * @since 1.0.0
 */
public enum SealPanel {
    /** Priority, golem colour, lock and redstone control. Every seal has it. */
    PRIORITY,
    /** The item filter. Present when the type has a filter. */
    FILTER,
    /** The work area. Present when the type has a configurable area. */
    AREA,
    /** The type's settings. Present when the type shows them. */
    TOGGLES,
    /** The required and forbidden golem traits. Every seal has it. */
    TAGS
}
