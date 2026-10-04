package com.leclowndu93150.thaumaturge.api.golems.seals;

/**
 * An on/off option a seal type declares. Placed seals store one value per declared setting.
 *
 * @param key      the save key, unique within the declaring type
 * @param nameKey  the translation key of the toggle label
 * @param fallback the value a fresh seal starts with, and the permanent value when the type hides its settings
 * @since 1.0.0
 */
public record SealSetting(String key, String nameKey, boolean fallback) {
}
