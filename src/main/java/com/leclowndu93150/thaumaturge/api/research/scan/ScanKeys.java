package com.leclowndu93150.thaumaturge.api.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * Canonical research-key layout for knowledge gained by scanning. Scanned subjects are recorded
 * in the player's research list under stable identifiers in the {@code thaumaturge} namespace
 * with a {@code scanned/} path prefix.
 *
 * @since 1.0.0
 */
public final class ScanKeys {
    private static final String NAMESPACE = "thaumaturge";
    private static final String PREFIX = "scanned/";

    private ScanKeys() {}

    /**
     * The key recorded when an item is scanned.
     *
     * @param item the item
     * @return the research key
     */
    public static ResourceLocation item(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return key("item/" + id.getNamespace() + "/" + id.getPath());
    }

    /**
     * The key recorded when an entity of the given type is scanned.
     *
     * @param type the entity type
     * @return the research key
     */
    public static ResourceLocation entity(EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key("entity/" + id.getNamespace() + "/" + id.getPath());
    }

    /**
     * The key recorded when an aspect is first discovered by scanning something carrying it.
     *
     * @param aspect the aspect key
     * @return the research key
     */
    public static ResourceLocation aspect(ResourceKey<IAspect> aspect) {
        ResourceLocation id = aspect.location();
        return key("aspect/" + id.getNamespace() + "/" + id.getPath());
    }

    /**
     * The key recorded when an enchantment is scanned on an item.
     *
     * @param enchantment the enchantment identifier
     * @return the research key
     */
    public static ResourceLocation enchantment(ResourceLocation enchantment) {
        return key("enchantment/" + enchantment.getNamespace() + "/" + enchantment.getPath());
    }

    /**
     * The key recorded when a mob effect is scanned on an entity or item.
     *
     * @param effect the effect identifier
     * @return the research key
     */
    public static ResourceLocation effect(ResourceLocation effect) {
        return key("effect/" + effect.getNamespace() + "/" + effect.getPath());
    }

    /**
     * The key recorded for a celestial observation.
     *
     * @param worldDay the world day the observation was made
     * @param body the observed body, e.g. {@code "sun"}, {@code "moon3"}, {@code "star1"}
     * @return the research key
     */
    public static ResourceLocation celestial(int worldDay, String body) {
        return key(celestialDayPrefix(worldDay) + body);
    }

    /**
     * The shared key path prefix of every celestial observation made on the given day. Used to
     * expire observations from previous days.
     *
     * @param worldDay the world day
     * @return the path prefix
     */
    public static String celestialDayPrefix(int worldDay) {
        return PREFIX + "celestial/" + worldDay + "/";
    }

    /**
     * Whether the given research key is a celestial observation from any day.
     *
     * @param research the research key
     * @return {@code true} when the key is a celestial observation
     */
    public static boolean isCelestial(ResourceLocation research) {
        return research.getNamespace().equals(NAMESPACE) && research.getPath().startsWith(PREFIX + "celestial/");
    }

    private static ResourceLocation key(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, PREFIX + path);
    }
}
