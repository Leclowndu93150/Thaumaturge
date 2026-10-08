package com.leclowndu93150.thaumaturge.api.golems.seals;

/**
 * How a seal filter treats its slots.
 *
 * @since 1.0.0
 */
public enum SealFilterMode {
    /** A plain whitelist or blacklist of single items. */
    PLAIN,
    /** Like {@link #PLAIN}, but in whitelist mode each slot also carries an amount limit. */
    LIMITS_WHEN_WHITELIST,
    /** Always a whitelist, and every slot carries an amount; the blacklist switch is ignored. */
    WHITELIST_WITH_LIMITS;

    /**
     * @param blacklist the filter's blacklist switch
     * @return whether slot amounts apply
     */
    public boolean usesLimits(boolean blacklist) {
        return switch (this) {
            case PLAIN -> false;
            case LIMITS_WHEN_WHITELIST -> !blacklist;
            case WHITELIST_WITH_LIMITS -> true;
        };
    }
}
