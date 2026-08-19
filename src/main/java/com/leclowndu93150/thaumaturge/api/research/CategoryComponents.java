package com.leclowndu93150.thaumaturge.api.research;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;

/**
 * Helpers for resolving display text for {@link IResearchCategory research categories}.
 *
 * <p>Translation keys follow the {@code research_category.<namespace>.<path>} convention used by
 * the lang provider.
 *
 * @since 1.0.0
 */
public final class CategoryComponents {
    private CategoryComponents() {}

    /**
     * Plain display name of the given category.
     *
     * @param key the category key
     * @return the translated name
     */
    public static MutableComponent name(ResourceKey<IResearchCategory> key) {
        return Component.translatable(translationKey(key));
    }

    /**
     * Display name styled for callout in card text (bold dark-blue).
     *
     * @param key the category key
     * @return the styled name
     */
    public static MutableComponent emphasised(ResourceKey<IResearchCategory> key) {
        return name(key).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_BLUE);
    }

    /**
     * Translation key for the given category, e.g. {@code research_category.thaumaturge.basics}.
     *
     * @param key the category key
     * @return the translation key
     */
    public static String translationKey(ResourceKey<IResearchCategory> key) {
        return "research_category." + key.identifier().getNamespace() + "." + key.identifier().getPath();
    }
}
