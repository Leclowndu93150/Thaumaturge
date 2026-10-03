package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Base class for research-related events fired on the {@link NeoForge#EVENT_BUS NeoForge event bus}.
 *
 * <p>Concrete events are cancellable. Cancelling a research event prevents the corresponding
 * mutation from being applied to the player's {@link com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge knowledge record}.
 *
 * @since 1.0.0
 */
public abstract class ResearchEvent extends Event implements ICancellableEvent {
    private final Player player;

    protected ResearchEvent(Player player) {
        this.player = player;
    }

    /**
     * The player whose record is being mutated.
     *
     * @return the player, never null
     */
    public Player player() {
        return player;
    }

    /**
     * Fired when a research entry is about to be added to a player's record.
     */
    public static final class Unlocked extends ResearchEvent {
        private final ResourceLocation research;

        public Unlocked(Player player, ResourceLocation research) {
            super(player);
            this.research = research;
        }

        /**
         * ResourceLocation of the research entry being unlocked.
         *
         * @return the research entry identifier
         */
        public ResourceLocation research() {
            return research;
        }
    }

    /**
     * Fired when a player advances or sets a research entry's stage.
     */
    public static final class StageAdvanced extends ResearchEvent {
        private final ResourceLocation research;
        private final int previousStage;
        private final int newStage;

        public StageAdvanced(Player player, ResourceLocation research, int previousStage, int newStage) {
            super(player);
            this.research = research;
            this.previousStage = previousStage;
            this.newStage = newStage;
        }

        /**
         * ResourceLocation of the research entry whose stage is changing.
         *
         * @return the research entry identifier
         */
        public ResourceLocation research() {
            return research;
        }

        /**
         * The stage the player was at before this change.
         *
         * @return the previous stage
         */
        public int previousStage() {
            return previousStage;
        }

        /**
         * The stage the player is moving to.
         *
         * @return the new stage
         */
        public int newStage() {
            return newStage;
        }
    }

    /**
     * Fired when a research entry is about to be marked complete.
     */
    public static final class Completed extends ResearchEvent {
        private final ResourceLocation research;

        public Completed(Player player, ResourceLocation research) {
            super(player);
            this.research = research;
        }

        /**
         * ResourceLocation of the research entry being completed.
         *
         * @return the research entry identifier
         */
        public ResourceLocation research() {
            return research;
        }
    }

    /**
     * Fired when a player is about to gain category knowledge.
     */
    public static final class KnowledgeGained extends ResearchEvent {
        private final KnowledgeType type;
        private final Holder<IResearchCategory> category;
        private final int amount;

        public KnowledgeGained(Player player, KnowledgeType type, Holder<IResearchCategory> category, int amount) {
            super(player);
            this.type = type;
            this.category = category;
            this.amount = amount;
        }

        /**
         * The type of knowledge being gained.
         *
         * @return the knowledge type
         */
        public KnowledgeType type() {
            return type;
        }

        /**
         * The category the knowledge is being added to.
         *
         * @return the category holder
         */
        public Holder<IResearchCategory> category() {
            return category;
        }

        /**
         * Raw amount of knowledge being added before progression scaling.
         *
         * @return the raw amount
         */
        public int amount() {
            return amount;
        }
    }
}
