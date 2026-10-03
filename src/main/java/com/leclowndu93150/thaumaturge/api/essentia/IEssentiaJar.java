package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.resources.Identifier;

/**
 * Implemented by jar blocks to declare the capacity and glass textures of one jar kind.
 *
 * <p>Both properties belong to the block rather than the block entity, so every jar of a given
 * kind shares them. Renderers derive the fill level from {@link #jarCapacity()}, so a jar
 * reports a full body at whatever capacity is chosen.
 *
 * <p>The three texture slots correspond to the slots of the shared jar model. A jar kind is
 * defined by supplying textures for them; the geometry, the fluid overlay and the metal
 * fittings are shared by every jar and cannot be replaced through this interface.
 *
 * @since 1.0.0
 */
public interface IEssentiaJar {
    /** The capacity of a standard jar. */
    int DEFAULT_CAPACITY = 250;

    /** The side texture of a standard jar. */
    Identifier DEFAULT_SIDE_TEXTURE = TCIds.rl("block/jar_side");

    /** The top texture of a standard jar. */
    Identifier DEFAULT_TOP_TEXTURE = TCIds.rl("block/jar_top");

    /** The bottom texture of a standard jar. */
    Identifier DEFAULT_BOTTOM_TEXTURE = TCIds.rl("block/jar_bottom");

    /**
     * The amount of a single aspect this jar kind can hold.
     *
     * @return the capacity in essentia units, greater than zero
     */
    default int jarCapacity() {
        return DEFAULT_CAPACITY;
    }

    /**
     * The texture applied to the four sides of the jar's glass body. Also used as the
     * particle texture.
     *
     * @return the side texture location, without the {@code textures/} prefix or extension
     */
    default Identifier jarSideTexture() {
        return DEFAULT_SIDE_TEXTURE;
    }

    /**
     * The texture applied to the top of the jar, including its metal rim.
     *
     * @return the top texture location, without the {@code textures/} prefix or extension
     */
    default Identifier jarTopTexture() {
        return DEFAULT_TOP_TEXTURE;
    }

    /**
     * The texture applied to the underside of the jar.
     *
     * @return the bottom texture location, without the {@code textures/} prefix or extension
     */
    default Identifier jarBottomTexture() {
        return DEFAULT_BOTTOM_TEXTURE;
    }
}
