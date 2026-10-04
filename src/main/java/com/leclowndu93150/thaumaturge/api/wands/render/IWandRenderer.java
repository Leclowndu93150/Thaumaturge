package com.leclowndu93150.thaumaturge.api.wands.render;

/**
 * Renders a wand for either an item display or the first-person hand.
 *
 * <p>Renderers may replace the standard model or call {@link WandRenderContext#renderDefault()} to
 * draw it. The tinted overload lets a renderer decorate the standard geometry without rebuilding
 * the rod, caps, focus, or runes.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IWandRenderer {
    /**
     * Renders the wand represented by the supplied context.
     *
     * @param context stack, wand parts, and render state for this draw
     */
    void render(WandRenderContext context);
}
