package com.leclowndu93150.thaumaturge.api.aspect;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import net.minecraft.resources.Identifier;

/**
 * Immutable additions to an item's derived aspect composition, keyed by aspect identifier.
 * Keys are resolved against the active registries on each index build; no holders are retained.
 * Unknown aspects are ignored with a warning. Zero amounts are allowed and have no effect.
 *
 * <p>Supplements apply after recipe derivation and equipment bonuses, before the per-aspect cap.
 * Explicit base assignments, including empty assignments, bypass supplements. Stack-specific
 * component variants and essentia capabilities retain their existing precedence over the index.
 *
 * @param amounts aspect identifiers and additions, between zero and {@link #MAX_AMOUNT}
 */
public record AspectSupplement(Map<Identifier, Integer> amounts) {
    public static final int MAX_AMOUNT = 500;
    public static final Codec<AspectSupplement> CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.intRange(0, MAX_AMOUNT)).xmap(AspectSupplement::new, AspectSupplement::amounts);

    public AspectSupplement {
        Map<Identifier, Integer> ordered = new TreeMap<>(Comparator.comparing(Identifier::toString));
        amounts.forEach((aspect, amount) -> {
            Objects.requireNonNull(aspect, "aspect");
            if (amount == null || amount < 0 || amount > MAX_AMOUNT) {
                throw new IllegalArgumentException("Aspect supplement amounts must be between 0 and " + MAX_AMOUNT);
            }
            ordered.put(aspect, amount);
        });
        amounts = Collections.unmodifiableMap(ordered);
    }
}
