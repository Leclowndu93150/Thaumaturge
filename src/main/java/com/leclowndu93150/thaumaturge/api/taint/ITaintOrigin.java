package com.leclowndu93150.thaumaturge.api.taint;

/**
 * A source that taint grows from, such as a taint seed or a tainted node. Implement it on the entity or block entity whose position
 * was registered with {@link TaintApi#addTaintSeed}.
 *
 * <p>Every taint block must lie within the configured spread radius of a live origin, or it decays. An origin eats flux from the aura
 * around it into a reserve and spends that reserve to grow taint. An origin whose reserve cannot pay for growth is dormant: it keeps its
 * land but does not spread.
 *
 * <p>All methods are called on the server thread.
 *
 * @since 1.1.0
 */
public interface ITaintOrigin {
    /**
     * Returns whether this origin currently sustains and drives taint. A dead seed or a node that is no longer tainted returns
     * {@code false} and is pruned from the origin index.
     *
     * @return {@code true} while the origin is alive and tainted
     */
    boolean isLiveTaintOrigin();

    /**
     * Returns the flux this origin has stored for growth.
     *
     * @return the stored flux, never negative
     */
    float getTaintReserve();

    /**
     * Replaces the stored flux. Implementations persist the value and clamp it to zero or more.
     *
     * @param reserve the new stored flux
     */
    void setTaintReserve(float reserve);

    /**
     * Spends stored flux when enough is available.
     *
     * @param amount the flux to spend
     * @return {@code true} when the reserve covered the amount and was reduced, {@code false} when it was left untouched
     */
    default boolean spendTaintReserve(float amount) {
        float reserve = getTaintReserve();
        if (reserve < amount) {
            return false;
        }
        setTaintReserve(reserve - amount);
        return true;
    }
}
