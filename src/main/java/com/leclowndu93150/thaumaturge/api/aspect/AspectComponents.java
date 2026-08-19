package com.leclowndu93150.thaumaturge.api.aspect;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Localized component factories for aspects. Used by tooltips, GUI labels, and chat output.
 *
 * <p>Translation keys follow the pattern {@code aspect.<namespace>.<tag>} for the display name,
 * {@code aspect.<namespace>.<tag>.desc} for the English description, and
 * {@code aspect.<namespace>.<tag>.help} for the study hint. The namespace is
 * derived from the aspect's registry id, so addon aspects resolve against the addon's lang
 * files without further configuration.
 *
 * @since 1.0.0
 */
public final class AspectComponents {
    private static final String UNKNOWN_NAME_KEY = "tc.aspect.unknown";
    private static final String UNKNOWN_SHORT_KEY = "tc.aspect.unknown.short";
    private static final String UNKNOWN_DESCRIPTION_KEY = "tc.aspect.unknown.desc";
    private static final String COMPOSITION_KEY = "tc.aspect.composition";
    private static final int COMPOUND_COMPONENTS = 2;

    private AspectComponents() {}

    /**
     * Returns the display name of the given aspect as the viewing player may see it.
     *
     * <p>Aspects the player has not discovered resolve to a generic placeholder rather than their
     * real name, so display surfaces mask by default. Use {@link #trueName} only where the reveal
     * is the point, such as the discovery message itself.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return the real name when the aspect is {@link AspectKnowledge#KNOWN}, otherwise a
     * placeholder
     * @see #trueName
     */
    public static MutableComponent name(Holder<IAspect> aspect) {
        if (AspectKnowledgeAccess.isKnown(aspect)) {
            return trueName(aspect);
        }
        return Component.translatable(UNKNOWN_NAME_KEY);
    }

    /**
     * Returns the display name of the given aspect in a form short enough for a chip-sized label.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return the real name when the aspect is {@link AspectKnowledge#KNOWN}, otherwise a short
     * placeholder
     */
    public static MutableComponent shortName(Holder<IAspect> aspect) {
        if (AspectKnowledgeAccess.isKnown(aspect)) {
            return trueName(aspect);
        }
        return Component.translatable(UNKNOWN_SHORT_KEY);
    }

    /**
     * Returns the localized display name of the given aspect regardless of whether the viewing
     * player has discovered it.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return a translatable component bound to {@code aspect.<namespace>.<tag>}
     * @apiNote this bypasses discovery masking. Prefer {@link #name} unless the call site exists
     * specifically to reveal the aspect.
     */
    public static MutableComponent trueName(Holder<IAspect> aspect) {
        return Component.translatable(translationKey(aspect, ""));
    }

    /**
     * Returns the description of the given aspect as the viewing player may see it.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return a translatable component bound to {@code aspect.<namespace>.<tag>.desc}, or a
     * placeholder when the aspect is not {@link AspectKnowledge#KNOWN}
     */
    public static MutableComponent description(Holder<IAspect> aspect) {
        if (AspectKnowledgeAccess.isKnown(aspect)) {
            return Component.translatable(translationKey(aspect, ".desc"));
        }
        return Component.translatable(UNKNOWN_DESCRIPTION_KEY);
    }

    /**
     * Returns the localized study hint of the given aspect, a short phrase naming the kind of
     * thing a player should examine to discover it.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return a translatable component bound to {@code aspect.<namespace>.<tag>.help}
     */
    public static MutableComponent help(Holder<IAspect> aspect) {
        return Component.translatable(translationKey(aspect, ".help"));
    }

    /**
     * Returns a phrase naming the two aspects that combine into the given aspect, for use as a
     * lead toward an aspect the player can derive but has not discovered.
     *
     * <p>The component names are themselves masked, so a component the player does not know reads
     * as a placeholder rather than leaking.
     *
     * @param aspect the aspect, referenced by holder so the registry id is available
     * @return a phrase such as {@code Terra + Aqua}, or an empty component when the aspect is not
     * a two-component compound
     */
    public static MutableComponent composition(Holder<IAspect> aspect) {
        List<Holder<IAspect>> parts = aspect.value().components();
        if (parts.size() != COMPOUND_COMPONENTS) {
            return Component.empty();
        }
        return Component.translatable(COMPOSITION_KEY, name(parts.get(0)), name(parts.get(1)));
    }

    private static String translationKey(Holder<IAspect> holder, String suffix) {
        String namespace = holder.unwrapKey().map(key -> key.identifier().getNamespace()).orElse("thaumaturge");
        return "aspect." + namespace + "." + holder.value().tag() + suffix;
    }
}
