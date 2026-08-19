package com.leclowndu93150.thaumaturge.api.casters;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Registration entry for a focus element: the stateless behavior singleton plus the icon
 * and color used to draw the element in casting UIs.
 *
 * @param element the behavior singleton; stateless and shared by every spell
 * @param icon    the texture drawn for this element
 * @param color   the packed {@code 0xRRGGBB} tint applied to the icon
 * @since 1.0.0
 */
public record FocusElementType(FocusElement element, Identifier icon, int color) {
    /** The registry key for focus element types. */
    public static final ResourceKey<Registry<FocusElementType>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "focus_element"));
}
