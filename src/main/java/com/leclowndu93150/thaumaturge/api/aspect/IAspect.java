package com.leclowndu93150.thaumaturge.api.aspect;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * An aspect is one of the fundamental thaumic essences that make up the world.
 *
 * <p>Aspects are loaded from datapack JSON under {@code data/<namespace>/thaumaturge/aspect/}
 * and exposed through the {@link #REGISTRY_KEY} datapack registry. Code typically references
 * aspects by {@link ResourceKey} (see the constants in {@code TCAspects}) and resolves them
 * through a {@link net.minecraft.core.HolderLookup.Provider HolderLookup.Provider} or a
 * {@link Holder}.
 *
 * <p>Aspects come in two kinds. A primal aspect has no components and represents one of the
 * six base essences. A compound aspect declares exactly two component aspects whose
 * combination yields it; this drives discovery and recombination. {@link #isPrimal()} reports
 * the kind.
 *
 * <p>Implementations are immutable value carriers. Equality semantics are defined by the
 * implementation; addons should treat aspects as values and not rely on reference identity.
 *
 * @since 1.0.0
 */
public interface IAspect {
    /** The datapack registry key for aspects. */
    ResourceKey<Registry<IAspect>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "aspect"));

    /**
     * The unique tag of this aspect. Stable identifier and the registry path of the aspect.
     *
     * @return the lowercase Latin tag, e.g. {@code "aer"}
     */
    String tag();

    /**
     * The packed RGB color of this aspect, used for tinting tooltips, particles, and aspect
     * icons.
     *
     * @return the color as {@code 0xRRGGBB}
     */
    int color();

    /**
     * The components that make up this aspect. Empty when this aspect is primal, otherwise
     * contains exactly two component aspects whose combination produces this aspect.
     *
     * @return the component aspect holders, in canonical order
     */
    List<Holder<IAspect>> components();

    /**
     * Optional Minecraft chat color code used by certain UIs for primal aspects.
     *
     * @return the legacy color code character without the {@code §} prefix, or empty
     */
    Optional<String> chatColor();

    /**
     * Identifier of the sprite texture used to render this aspect.
     *
     * @return the texture identifier, typically under {@code textures/aspects/}
     */
    Identifier texture();

    /**
     * The OpenGL blend function used when drawing this aspect's icon. Most aspects use the
     * default additive blend; entropy and its derived aspects use one-minus-source for visual
     * contrast.
     *
     * @return the OpenGL blend function constant
     */
    int blend();

    /**
     * Whether this aspect is one of the six primal essences.
     *
     * @return {@code true} when this aspect has no components
     */
    default boolean isPrimal() {
        return components().isEmpty();
    }
}
