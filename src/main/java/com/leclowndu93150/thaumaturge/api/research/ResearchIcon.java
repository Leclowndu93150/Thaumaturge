package com.leclowndu93150.thaumaturge.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

/**
 * Icon shown on a research entry's node in the Thaumonomicon. An icon is a full texture path,
 * an item identifier, or a focus element reference; entries may declare several icons, which
 * the browser cycles through while the node is visible.
 *
 * <p>The serialized form is a single string. Strings prefixed with {@code focus:} name a focus
 * element whose tinted part icon is rendered; strings ending in {@code .png} are treated as
 * texture paths; anything else is treated as an item identifier.
 *
 * @param id the texture path, item identifier, or focus element identifier
 * @param kind how {@link #id} is resolved when the icon is rendered
 * @since 1.0.0
 */
public record ResearchIcon(ResourceLocation id, Kind kind) {
    /** String codec using the {@code focus:} prefix and {@code .png} suffix conventions. */
    public static final Codec<ResearchIcon> CODEC = Codec.STRING.xmap(ResearchIcon::parse, ResearchIcon::serialize);

    /** How a research icon's identifier is resolved. */
    public enum Kind {
        /** {@link ResearchIcon#id()} names an item. */
        ITEM,
        /** {@link ResearchIcon#id()} is a full texture path. */
        TEXTURE,
        /** {@link ResearchIcon#id()} names a focus element registered with the caster system. */
        FOCUS
    }

    /**
     * Creates a texture icon.
     *
     * @param id the full texture path, e.g. {@code thaumaturge:textures/research/r_wisp.png}
     * @return the icon
     */
    public static ResearchIcon ofTexture(ResourceLocation id) {
        return new ResearchIcon(id, Kind.TEXTURE);
    }

    /**
     * Creates an item icon.
     *
     * @param id the item identifier
     * @return the icon
     */
    public static ResearchIcon ofItem(ResourceLocation id) {
        return new ResearchIcon(id, Kind.ITEM);
    }

    /**
     * Creates a focus element icon.
     *
     * @param id the focus element identifier
     * @return the icon
     */
    public static ResearchIcon ofFocus(ResourceLocation id) {
        return new ResearchIcon(id, Kind.FOCUS);
    }

    /**
     * Returns whether this icon is a raw texture.
     *
     * @return {@code true} when {@link #kind()} is {@link Kind#TEXTURE}
     */
    public boolean texture() {
        return kind == Kind.TEXTURE;
    }

    private static ResearchIcon parse(String value) {
        if (value.startsWith("focus:")) {
            return ofFocus(ResourceLocation.parse(value.substring("focus:".length())));
        }
        ResourceLocation id = ResourceLocation.parse(value);
        return new ResearchIcon(id, id.getPath().endsWith(".png") ? Kind.TEXTURE : Kind.ITEM);
    }

    private static String serialize(ResearchIcon icon) {
        return icon.kind == Kind.FOCUS ? "focus:" + icon.id : icon.id.toString();
    }
}
