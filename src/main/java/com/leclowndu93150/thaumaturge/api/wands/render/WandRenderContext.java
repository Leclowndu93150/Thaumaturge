package com.leclowndu93150.thaumaturge.api.wands.render;

import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Snapshot input and render targets for a custom wand renderer.
 *
 * <p>The pose stack is already transformed into the standard wand model space. This convention is
 * the same for item rendering and first-person hand rendering. In hand rendering,
 * {@link #displayContext()} is {@code null} and {@link #firstPersonHand()} is true.
 *
 * <p>Contexts and their callbacks are valid only during the renderer invocation. Mutating the
 * stack snapshot does not change the inventory stack.
 *
 * @since 1.0.0
 */
public final class WandRenderContext {
    private final ItemStack stack;
    private final WandCap cap;
    private final WandRod rod;
    private final boolean sceptre;
    private final boolean hasFocus;
    private final int focusColor;
    private final PoseStack poseStack;
    private final MultiBufferSource buffers;
    private final int light;
    private final int overlay;
    private final @Nullable ItemDisplayContext displayContext;
    private final boolean firstPersonHand;
    private final DefaultRenderer defaultRenderer;

    /**
     * Creates a render context. Thaumaturge creates contexts for renderer dispatch; addons should
     * receive them through {@link IWandRenderer#render(WandRenderContext)}.
     *
     * @param stack the wand item stack
     * @param cap the wand cap
     * @param rod the wand rod
     * @param sceptre whether the wand is a sceptre
     * @param hasFocus whether the wand has a focus
     * @param focusColor the packed focus tint
     * @param poseStack pose stack already transformed into wand model space
     * @param buffers render buffers
     * @param light packed light value
     * @param overlay packed overlay value
     * @param displayContext item display context, or null for first-person hand rendering
     * @param firstPersonHand whether this is the custom first-person hand path
     * @param defaultRenderer direct callback for submitting the standard wand model
     */
    public WandRenderContext(
            ItemStack stack,
            WandCap cap,
            WandRod rod,
            boolean sceptre,
            boolean hasFocus,
            int focusColor,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay,
            @Nullable ItemDisplayContext displayContext,
            boolean firstPersonHand,
            DefaultRenderer defaultRenderer) {
        this.stack = stack.copy();
        this.cap = cap;
        this.rod = rod;
        this.sceptre = sceptre;
        this.hasFocus = hasFocus;
        this.focusColor = focusColor;
        this.poseStack = poseStack;
        this.buffers = buffers;
        this.light = light;
        this.overlay = overlay;
        this.displayContext = displayContext;
        this.firstPersonHand = firstPersonHand;
        this.defaultRenderer = defaultRenderer;
    }

    /** @return wand item stack */
    public ItemStack stack() {
        return stack;
    }

    /** @return wand cap */
    public WandCap cap() {
        return cap;
    }

    /** @return wand rod */
    public WandRod rod() {
        return rod;
    }

    /** @return whether the wand is a sceptre */
    public boolean sceptre() {
        return sceptre;
    }

    /** @return whether the wand has a focus */
    public boolean hasFocus() {
        return hasFocus;
    }

    /** @return packed focus tint */
    public int focusColor() {
        return focusColor;
    }

    /** @return pose stack already transformed into wand model space */
    public PoseStack poseStack() {
        return poseStack;
    }

    /** @return render buffers */
    public MultiBufferSource buffers() {
        return buffers;
    }

    /** @return packed light value */
    public int light() {
        return light;
    }

    /** @return packed overlay value */
    public int overlay() {
        return overlay;
    }

    /** @return item display context, or null for first-person hand rendering */
    public @Nullable ItemDisplayContext displayContext() {
        return displayContext;
    }

    /** @return whether this render is for the first-person hand */
    public boolean firstPersonHand() {
        return firstPersonHand;
    }

    /** Submits the standard wand model with white rod and cap tints. */
    public void renderDefault() {
        renderDefault(0xFFFFFFFF, 0xFFFFFFFF);
    }

    /**
     * Submits the standard wand model with caller-provided packed ARGB tints on its rod and caps.
     * Focus and rune rendering retain their standard colors.
     *
     * @param rodTint packed ARGB tint for the rod geometry
     * @param capTint packed ARGB tint for the cap geometry
     */
    public void renderDefault(int rodTint, int capTint) {
        defaultRenderer.render(rodTint, capTint);
    }

    /** Direct standard-model callback; it does not run renderer selection again. */
    @FunctionalInterface
    public interface DefaultRenderer {
        /**
         * Submits standard wand geometry.
         *
         * @param rodTint packed ARGB tint for the rod geometry
         * @param capTint packed ARGB tint for the cap geometry
         */
        void render(int rodTint, int capTint);
    }
}
