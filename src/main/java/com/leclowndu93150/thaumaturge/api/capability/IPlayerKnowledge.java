package com.leclowndu93150.thaumaturge.api.capability;

import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Per-player record of completed research, in-progress stages, per-entry flags, and accumulated
 * category knowledge.
 *
 * <p>The instance lives on the player as a NeoForge {@link net.neoforged.neoforge.attachment.AttachmentType data attachment}
 * and is the authoritative source for research progression. The server owns the value; clients
 * receive a copy on login, on dimension change, and whenever the server publishes a change.
 *
 * <p>All mutating methods are server-side; they return {@code true} when the call produced an
 * observable change, {@code false} otherwise. Callers that mutate must invoke {@link #sync(ServerPlayer)}
 * to publish the new state to the owner.
 *
 * <p>Research is identified by {@link ResourceLocation}. Query staged progress through
 * {@link #researchStage(ResourceLocation)}; see {@link #isResearchKnown(ResourceLocation, int)}.
 *
 * @since 1.0.0
 */
public interface IPlayerKnowledge {
    /**
     * Removes every recorded research, stage, flag, and knowledge counter.
     */
    void clear();

    /**
     * Returns the current status of a given research entry.
     *
     * @param research the research entry identifier; must not be null
     * @return the status, never null; {@link ResearchStatus#UNKNOWN} when the entry has not been added
     */
    ResearchStatus researchStatus(ResourceLocation research);

    /**
     * Returns whether the entry is known and every stage is complete.
     *
     * @param research the research entry identifier
     * @return {@code true} when the entry is complete
     */
    boolean isResearchComplete(ResourceLocation research);

    /**
     * Returns whether the entry has been added to this player's record.
     *
     * @param research the research entry identifier
     * @return {@code true} when the entry is known regardless of stage
     */
    boolean isResearchKnown(ResourceLocation research);

    /**
     * Returns whether the entry is known and the player has reached at least the given stage.
     *
     * @param research the research entry identifier
     * @param minimumStage the minimum stage required
     * @return {@code true} when the entry is known and the player is at or beyond {@code minimumStage}
     */
    boolean isResearchKnown(ResourceLocation research, int minimumStage);

    /**
     * Returns the current stage of the given research, or {@code -1} when the entry is not known.
     *
     * @param research the research entry identifier
     * @return the stage index, or {@code -1} when unknown
     */
    int researchStage(ResourceLocation research);

    /**
     * Adds the research to the player's record. Has no effect when already added.
     *
     * @param research the research entry identifier
     * @return {@code true} when the entry was newly added
     */
    boolean addResearch(ResourceLocation research);

    /**
     * Sets the current stage of the given research. Has no effect when the entry is not known or
     * when {@code stage} is non-positive.
     *
     * @param research the research entry identifier
     * @param stage the new stage; must be positive
     * @return {@code true} when the stored stage changed
     */
    boolean setResearchStage(ResourceLocation research, int stage);

    /**
     * Removes the research and any associated stage or flags from the player's record.
     *
     * @param research the research entry identifier
     * @return {@code true} when an entry was removed
     */
    boolean removeResearch(ResourceLocation research);

    /**
     * The unmodifiable set of every research entry currently in the player's record.
     *
     * @return an unmodifiable set, never null
     */
    Set<ResourceLocation> researchList();

    /**
     * Adds a flag to the given research entry.
     *
     * @param research the research entry identifier
     * @param flag the flag to set
     * @return {@code true} when the flag was newly set
     */
    boolean setResearchFlag(ResourceLocation research, ResearchFlag flag);

    /**
     * Removes a flag from the given research entry.
     *
     * @param research the research entry identifier
     * @param flag the flag to clear
     * @return {@code true} when the flag was present and removed
     */
    boolean clearResearchFlag(ResourceLocation research, ResearchFlag flag);

    /**
     * Returns whether the given research entry has the given flag set.
     *
     * @param research the research entry identifier
     * @param flag the flag to test
     * @return {@code true} when set
     */
    boolean hasResearchFlag(ResourceLocation research, ResearchFlag flag);

    /**
     * Adds raw progress for the given knowledge type and category. The raw counter is scaled by
     * {@link KnowledgeType#progression()} when read through {@link #knowledge(KnowledgeType, ResourceKey)}.
     * Negative {@code amount} is permitted; the counter clamps at zero.
     *
     * @param type the knowledge type
     * @param category the category key; may be the synthetic global category when {@link KnowledgeType}
     *                 does not partition by category
     * @param amount the raw amount to add, may be negative
     * @return {@code true} when the stored counter changed
     */
    boolean addKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount);

    /**
     * Returns the visible level of accumulated category knowledge, computed as
     * {@code rawKnowledge / type.progression()}.
     *
     * @param type the knowledge type
     * @param category the category key
     * @return the visible level, never negative
     */
    int knowledge(KnowledgeType type, ResourceKey<IResearchCategory> category);

    /**
     * Returns the raw accumulated counter for the given category and knowledge type before scaling.
     *
     * @param type the knowledge type
     * @param category the category key
     * @return the raw counter, never negative
     */
    int rawKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category);

    /**
     * Publishes this record to the given player. Call after any mutation that must be reflected
     * in the client's mirror.
     *
     * @param player the player to sync; must not be null
     */
    void sync(ServerPlayer player);
}
