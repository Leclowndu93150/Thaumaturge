package com.leclowndu93150.thaumaturge.api.golems.parts;

import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * Visual description of a golem part: the mesh rendered on the golem and where it
 * attaches. Object parts whose name starts with {@code bm} sample the golem material's
 * texture instead of {@link #texture()}. A mesh part's explicit namespaced texture slot
 * overrides this fallback. Built-in parts are fittings around Minecraft's copper golem rig.
 *
 * <p>Dynamic per-part render behavior (wheel spin, dart recoil, hauler chest swap) is
 * registered client side against the owning part rather than subclassed here.
 *
 * @since 1.0.0
 */
public final class GolemPartModel {
    /** Where a part model attaches on the golem body. */
    public enum AttachPoint {
        ARMS,
        LEGS,
        BODY,
        HEAD
    }

    /** Which limb of a paired attachment a model instance renders on. */
    public enum LimbSide {
        LEFT,
        RIGHT,
        MIDDLE
    }

    private final ResourceLocation objModel;
    private final ResourceLocation texture;
    private final AttachPoint attachPoint;

    /**
     * @param objModel    the OBJ model location
     * @param texture     the texture applied to non-material object parts, or null when every
     *                    part uses the material texture
     * @param attachPoint where the model attaches
     */
    public GolemPartModel(ResourceLocation objModel, @Nullable ResourceLocation texture, AttachPoint attachPoint) {
        this.objModel = objModel;
        this.texture = texture;
        this.attachPoint = attachPoint;
    }

    /**
     * @return the OBJ model location
     */
    public ResourceLocation objModel() {
        return objModel;
    }

    /**
     * @return the texture for non-material object parts, or null when absent
     */
    public @Nullable ResourceLocation texture() {
        return texture;
    }

    /**
     * @return where the model attaches on the golem
     */
    public AttachPoint attachPoint() {
        return attachPoint;
    }

    /**
     * @param partName an object group name inside the OBJ model
     * @return whether that object samples the golem material's texture
     */
    public boolean useMaterialTextureForObjectPart(String partName) {
        return partName.startsWith("bm");
    }
}
