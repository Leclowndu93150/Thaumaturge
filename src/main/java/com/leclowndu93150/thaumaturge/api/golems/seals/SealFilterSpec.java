package com.leclowndu93150.thaumaturge.api.golems.seals;

/**
 * The shape of a seal type's item filter.
 *
 * @param slots the number of ghost slots, from 1 to 9
 * @param mode  how the slots are interpreted
 * @since 1.0.0
 */
public record SealFilterSpec(int slots, SealFilterMode mode) {
    /**
     * @throws IllegalArgumentException when {@code slots} is outside 1 to 9
     */
    public SealFilterSpec {
        if (slots < 1 || slots > 9) {
            throw new IllegalArgumentException("Seal filters hold 1 to 9 slots, got " + slots);
        }
    }
}
