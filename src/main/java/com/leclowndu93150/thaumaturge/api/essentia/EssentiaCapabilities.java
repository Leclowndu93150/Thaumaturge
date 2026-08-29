package com.leclowndu93150.thaumaturge.api.essentia;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

/**
 * Registry-style holder for the essentia capability types.
 *
 * <p>{@link #TRANSPORT} is the sided block capability that exposes {@link IEssentiaTransport};
 * it accepts a nullable {@link Direction} context the way vanilla item / fluid handlers do.
 *
 * <p>{@link #STORAGE} is the sided block capability for enumerable, typed and simulation-safe
 * essentia storage. The returned {@link IEssentiaStorage} is already bound to the queried side;
 * querying without a side returns no storage view.
 *
 * <p>{@link #CONTAINER} is the item capability for {@link IEssentiaContainerItem}; it requires
 * no context.
 *
 * @since 1.0.0
 */
public final class EssentiaCapabilities {
    /** Sided block capability for essentia transport. */
    public static final BlockCapability<IEssentiaTransport, Direction> TRANSPORT = BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "essentia_transport"), IEssentiaTransport.class);

    /** Sided block capability for enumerable essentia storage. */
    public static final BlockCapability<IEssentiaStorage, Direction> STORAGE = BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "essentia_storage"), IEssentiaStorage.class);

    /** Item capability for essentia containers. */
    public static final ItemCapability<IEssentiaContainerItem, Void> CONTAINER = ItemCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "essentia_container"), IEssentiaContainerItem.class);

    /** Sided block capability for synthetic aspect queries (filters, routing intents). */
    public static final BlockCapability<IAspectQuery, Direction> ASPECT_QUERY = BlockCapability.createSided(
            ResourceLocation.fromNamespaceAndPath("thaumaturge", "aspect_query"), IAspectQuery.class);

    private EssentiaCapabilities() {}
}
