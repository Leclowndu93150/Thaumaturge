package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

/**
 * Registry-style holder for the essentia capability types.
 *
 * <p>{@link #TRANSPORT} is the sided block capability that exposes {@link IEssentiaTransport};
 * it accepts a nullable {@link Direction} context the way vanilla item / fluid handlers do.
 *
 * <p>{@link #STORAGE} is the sided block capability for {@link IEssentiaStorage}: listable,
 * transactional storage bound to the queried side. Querying with a {@code null} side returns no
 * view.
 *
 * <p>{@link #CONTAINER} is the item capability for {@link IItemEssentia}; it requires no context and returns a view bound to the
 * queried stack.
 *
 * <p>{@link #ITEM_STORAGE} is the item capability for {@link IEssentiaItemStorage}, present only
 * on items automation may fill and drain. Scan-only aspect items do not expose it.
 *
 * @since 1.0.0
 */
public final class EssentiaCapabilities {
    /** Sided block capability for essentia transport. */
    public static final BlockCapability<IEssentiaTransport, Direction> TRANSPORT =
            BlockCapability.createSided(TTIds.rl("essentia_transport"), IEssentiaTransport.class);

    /** Sided block capability for listable, transactional essentia storage. */
    public static final BlockCapability<IEssentiaStorage, Direction> STORAGE =
            BlockCapability.createSided(TTIds.rl("essentia_storage"), IEssentiaStorage.class);

    /** Item capability for the essentia a stack carries. */
    public static final ItemCapability<IItemEssentia, Void> CONTAINER =
            ItemCapability.createVoid(TTIds.rl("essentia_container"), IItemEssentia.class);

    /** Item capability for essentia items automation may fill and drain. */
    public static final ItemCapability<IEssentiaItemStorage, Void> ITEM_STORAGE =
            ItemCapability.createVoid(TTIds.rl("essentia_item_storage"), IEssentiaItemStorage.class);

    /** Sided block capability for synthetic aspect queries (filters, routing intents). */
    public static final BlockCapability<IAspectQuery, Direction> ASPECT_QUERY =
            BlockCapability.createSided(TTIds.rl("aspect_query"), IAspectQuery.class);

    private EssentiaCapabilities() {}
}
