package com.leclowndu93150.thaumaturge.api.golems.seals;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * An on/off option a seal type declares. Placed seals store one value per declared setting.
 *
 * @param key       the save key, unique within the declaring type
 * @param nameKey   the translation key of the toggle label
 * @param fallback  the value a fresh seal starts with, and the permanent value when the type hides its settings
 * @param formerKey an older save key, read only when a saved seal has no value under {@code key}; null when there is none
 * @since 1.0.0
 */
public record SealSetting(String key, String nameKey, boolean fallback, @Nullable String formerKey) {
    /**
     * Declares a setting with no former save key.
     *
     * @param key      the save key, unique within the declaring type
     * @param nameKey  the translation key of the toggle label
     * @param fallback the starting value
     * @since 1.1.0
     */
    public SealSetting(String key, String nameKey, boolean fallback) {
        this(key, nameKey, fallback, null);
    }

    /**
     * Returns a copy that also reads an older save key. Values are always written under {@link #key()}.
     *
     * @param former the older save key
     * @return the new setting
     * @since 1.1.0
     */
    public SealSetting readingAlso(String former) {
        return new SealSetting(key, nameKey, fallback, former);
    }

    /**
     * @return the older save key, when the setting has one
     * @since 1.1.0
     */
    public Optional<String> former() {
        return Optional.ofNullable(formerKey);
    }
}
