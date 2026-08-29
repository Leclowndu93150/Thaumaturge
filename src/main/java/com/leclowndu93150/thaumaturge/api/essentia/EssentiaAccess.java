package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Convenience lookups for the sided essentia transport capability.
 *
 * <p>These wrap {@link EssentiaCapabilities#TRANSPORT} so that tooltip, JEI, and integration code
 * can ask "does this block expose essentia transport on this face" without repeating the capability
 * query. The {@code face} argument is the side of the queried block that the caller is approaching
 * from, matching the sided-capability convention.
 *
 * @since 1.0.0
 */
public final class EssentiaAccess {
    private EssentiaAccess() {}

    /**
     * Returns the essentia transport exposed by the block at {@code pos} on the given face.
     *
     * @param level the level
     * @param pos   the block position
     * @param face  the side of the block being approached
     * @return the transport, or {@code null} when the block exposes none on that face
     */
    public static @Nullable IEssentiaTransport transport(Level level, BlockPos pos, Direction face) {
        return level.getCapability(EssentiaCapabilities.TRANSPORT, pos, face);
    }

    /**
     * Whether the block at {@code pos} exposes essentia transport on the given face.
     *
     * @param level the level
     * @param pos   the block position
     * @param face  the side of the block being approached
     * @return {@code true} when a transport capability is present on that face
     */
    public static boolean isEssentiaTransport(Level level, BlockPos pos, Direction face) {
        return transport(level, pos, face) != null;
    }

    /**
     * Returns the aspect a stack advertises as filter intent. Filter intent is scanning metadata;
     * it does not make the item an essentia container or grant transfer support.
     */
    public static @Nullable ResourceKey<IAspect> aspectFilter(ItemStack stack) {
        return stack.get(TCDataComponents.ASPECT_FILTER.get());
    }

    /**
     * Returns a configured copy of {@code stack}. Passing {@code null} removes its filter. The
     * supplied stack is never changed.
     */
    public static ItemStack withAspectFilter(ItemStack stack, @Nullable ResourceKey<IAspect> aspect) {
        ItemStack copy = stack.copy();
        if (aspect == null) copy.remove(TCDataComponents.ASPECT_FILTER.get());
        else copy.set(TCDataComponents.ASPECT_FILTER.get(), aspect);
        return copy;
    }
}
