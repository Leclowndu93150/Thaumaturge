package com.leclowndu93150.thaumaturge.api.golems;

import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * A behavioral tag carried by golem materials and parts. The union of all part traits,
 * with opposing pairs cancelling each other, defines what a golem can do.
 *
 * <p>Traits live in the {@link #REGISTRY_KEY} registry, so addons may contribute their own.
 * The built-in traits are exposed as constants on {@code TCGolemTraits}.
 *
 * <p>Opposition is symmetric and is declared by {@link #opposite()} returning the id of the
 * opposing trait. A trait and its opposite cancel when merged onto the same golem. The
 * opposing trait need not be registered before this one; resolution happens on lookup.
 *
 * @since 1.0.0
 */
public final class GolemTrait {
    /** The registry key for golem traits. */
    public static final ResourceKey<Registry<GolemTrait>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "golem_trait"));

    private final Identifier icon;
    private final @Nullable ResourceKey<GolemTrait> opposite;

    /**
     * @param icon     the icon texture drawn for this trait in golem UIs
     * @param opposite the trait this one cancels and is cancelled by, or null for none
     */
    public GolemTrait(Identifier icon, @Nullable ResourceKey<GolemTrait> opposite) {
        this.icon = Objects.requireNonNull(icon, "icon");
        this.opposite = opposite;
    }

    /**
     * Creates a trait whose icon follows the default naming convention
     * {@code <namespace>:textures/misc/golem/tag_<path>.png}.
     *
     * @param id       the id this trait is registered under, used to derive the icon path
     * @param opposite the trait this one cancels and is cancelled by, or null for none
     * @return the trait
     */
    public static GolemTrait ofId(Identifier id, @Nullable ResourceKey<GolemTrait> opposite) {
        return new GolemTrait(defaultIcon(id), opposite);
    }

    /**
     * The conventional icon location for a trait id.
     *
     * @param id the trait id
     * @return {@code <namespace>:textures/misc/golem/tag_<path>.png}
     */
    public static Identifier defaultIcon(Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/misc/golem/tag_" + id.getPath() + ".png");
    }

    /**
     * @return the icon texture drawn for this trait in golem UIs
     */
    public Identifier icon() {
        return icon;
    }

    /**
     * @return the key of the trait this trait cancels and is cancelled by, or null when it
     *         has none
     */
    public @Nullable ResourceKey<GolemTrait> opposite() {
        return opposite;
    }

    /**
     * The translation key for a trait's display name.
     *
     * @param id the trait id
     * @return {@code golem.trait.<namespace>.<path>}
     */
    public static String nameKey(Identifier id) {
        return "golem.trait." + id.getNamespace() + "." + id.getPath();
    }

    /**
     * The translation key for a trait's descriptive text.
     *
     * @param id the trait id
     * @return {@code golem.trait.text.<namespace>.<path>}
     */
    public static String descriptionKey(Identifier id) {
        return "golem.trait.text." + id.getNamespace() + "." + id.getPath();
    }

    /** Network codec serializing a trait by its registry id. */
    public static final StreamCodec<RegistryFriendlyByteBuf, GolemTrait> STREAM_CODEC = ByteBufCodecs.registry(REGISTRY_KEY);
}
