package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * A research entry. Entries are the nodes shown in a Thaumonomicon category; each entry contains
 * one or more {@link IResearchStage stages} that the player completes in order.
 *
 * <p>Entries are loaded from the {@link #REGISTRY_KEY} datapack registry under
 * {@code data/<namespace>/thaumaturge/research_entry/}.
 *
 * @since 1.0.0
 */
public interface IResearchEntry {
    /** Datapack registry key for research entries. */
    ResourceKey<Registry<IResearchEntry>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "research_entry"));

    /**
     * Category this entry belongs to.
     *
     * @return the owning category holder
     */
    Holder<IResearchCategory> category();

    /**
     * Translation key used to display this entry's name.
     *
     * @return the translation key, e.g. {@code research.thaumaturge.unlocking_secrets.title}
     */
    String nameKey();

    /**
     * Parent references that must be satisfied before this entry becomes available. A parent may
     * be referenced from another category and may require only a partial stage of its progress.
     *
     * @return the parent references, never null; may be empty
     */
    Set<ResearchParent> parents();

    /**
     * Sibling entries displayed in the Thaumonomicon as decorative connectors next to this entry,
     * with no progression effect.
     *
     * @return the sibling identifiers, never null
     */
    Set<Identifier> siblings();

    /**
     * Column position in the Thaumonomicon grid.
     *
     * @return the column
     */
    int column();

    /**
     * Row position in the Thaumonomicon grid.
     *
     * @return the row
     */
    int row();

    /**
     * Stages of this entry in order. The player advances through them as research progresses.
     *
     * @return the stages; never empty
     */
    List<IResearchStage> stages();

    /**
     * Meta flags that affect display and progression behaviour.
     *
     * @return the meta flag set
     */
    Set<ResearchEntryMeta> meta();

    /**
     * Whether this entry has the given meta flag.
     *
     * @param flag the flag to test
     * @return {@code true} when set
     */
    default boolean hasMeta(ResearchEntryMeta flag) {
        return meta().contains(flag);
    }

    /**
     * Icons displayed on this entry's node in the Thaumonomicon. The browser cycles through the
     * list over time; when empty, the node falls back to an icon derived from the first stage's
     * item requirements.
     *
     * @return the icons, never null, possibly empty
     */
    default List<ResearchIcon> icons() {
        return List.of();
    }

    /**
     * Extra pages shown once this entry is complete and each page's own research requirements are
     * met.
     *
     * @return the addenda, never null, possibly empty
     */
    default List<ResearchAddendum> addenda() {
        return List.of();
    }

    /**
     * Returns the hand-authored aspect cost of this entry. The distinct aspects seed the anchor
     * ring of research-note puzzles generated for theory gates; the amounts are the point price
     * paid from the player's aspect pool for observation gates.
     *
     * @return the aspect cost, or the empty list when the entry has no knowledge gates
     * @since 1.0.0
     */
    default AspectList noteAspects() {
        return AspectList.EMPTY;
    }

    /**
     * Returns the research-note puzzle complexity of this entry, clamped to {@code [1, 3]}.
     * Drives the hex grid radius and the number of holes punched into the sheet.
     *
     * @return the puzzle complexity
     * @since 1.0.0
     */
    default int complexity() {
        return 1;
    }
}
