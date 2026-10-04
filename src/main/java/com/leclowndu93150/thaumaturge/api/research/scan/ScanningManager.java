package com.leclowndu93150.thaumaturge.api.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

/**
 * Registry and dispatcher for everything the thaumometer can scan.
 *
 * <p>A scan asks every registered {@link IScannable} and every {@link ScanEntry} whether it matches the target, advances the research
 * of each match, then tells the player what happened. Scanning a block that holds items also scans up to 100 of the stacks inside.
 *
 * <p>Registration is not thread-safe; register during mod construction only. Knowledge and aspect lookups go through a binding the mod
 * installs at construction; addons must not call {@link #bind}.
 *
 * @since 1.0.0
 */
public final class ScanningManager {
    private static final List<IScannable> SUBJECTS = new ArrayList<>();
    private static final int CONTAINER_SCAN_LIMIT = 100;
    private static @Nullable Bindings bindings;

    private ScanningManager() {}

    /**
     * Adds a code-driven scan subject.
     *
     * @param subject the subject
     */
    public static void register(IScannable subject) {
        SUBJECTS.add(subject);
    }

    /**
     * Scans a target for a player: advances research, runs side effects and shows the result in the action bar. Server side only.
     *
     * @param player the scanning player
     * @param target what the thaumometer points at
     */
    public static void scan(Player player, ScanTarget target) {
        boolean found = false;
        boolean silent = false;
        Component refusal = null;
        for (IScannable subject : SUBJECTS) {
            if (!subject.matches(player, target)) {
                continue;
            }
            Component refused = subject.refusal(player, target);
            if (refused != null) {
                refusal = refusal == null ? refused : refusal;
                continue;
            }
            Identifier research = subject.research(player, target);
            if (research != null && !advance(player, research) && !(KnowledgeAccess.of(player).isResearchKnown(research) && subject.rescannable(player, target))) {
                continue;
            }
            silent |= research == null;
            found = true;
            subject.onScanned(player, target);
        }
        for (ScanEntry entry : entries(player)) {
            if (entry.matches(player, target) && advance(player, entry.key())) {
                found = true;
            }
        }
        report(player, refusal, found, silent);
        if (target instanceof ScannedBlock(var pos)) {
            scanContents(player, player.level().getCapability(Capabilities.Item.BLOCK, pos, Direction.UP));
        }
    }

    private static boolean advance(Player player, Identifier research) {
        return impl().progressResearch(player, research);
    }

    private static void report(Player player, @Nullable Component refusal, boolean found, boolean silent) {
        if (refusal != null) {
            player.sendOverlayMessage(refusal.copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else if (!silent) {
            player.sendOverlayMessage(found
                    ? Component.translatable("tc.knownobject").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC)
                    : Component.translatable("tc.unknownobject").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }

    private static void scanContents(Player player, @Nullable ResourceHandler<ItemResource> contents) {
        if (contents == null) {
            return;
        }
        int scanned = 0;
        for (int index = 0; index < contents.size(); index++) {
            ItemResource resource = contents.getResource(index);
            if (!resource.isEmpty()) {
                scan(player, ScanTarget.stack(resource.toStack(contents.getAmountAsInt(index))));
                scanned++;
            }
            if (scanned >= CONTAINER_SCAN_LIMIT) {
                player.sendOverlayMessage(Component.translatable("tc.invtoolarge").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
                return;
            }
        }
    }

    /**
     * @param player the scanning player
     * @param target the candidate target
     * @return whether scanning the target could still advance research the player lacks, or would trigger a keyless subject
     */
    public static boolean isStillScannable(Player player, ScanTarget target) {
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        for (IScannable subject : SUBJECTS) {
            if (!subject.matches(player, target)) {
                continue;
            }
            Identifier research = subject.research(player, target);
            if (research == null || !knowledge.isResearchKnown(research) || subject.rescannable(player, target)) {
                return true;
            }
        }
        for (ScanEntry entry : entries(player)) {
            if (entry.matches(player, target) && !knowledge.isResearchKnown(entry.key())) {
                return true;
            }
        }
        return false;
    }

    private static List<ScanEntry> entries(Player player) {
        return player.level().registryAccess().lookup(ScanEntry.REGISTRY_KEY).map(registry -> registry.listElements().map(Holder.Reference::value).toList()).orElse(List.of());
    }

    /**
     * Turns a target into the item stack item-based scans look at: the stack itself, a dropped item's stack, or the item form of a
     * block (its pick-block stack, or a bucket for a fluid). Blocks with no meaningful item form, such as aura nodes, give nothing.
     *
     * @param player the scanning player
     * @param target the target
     * @return the item form, or an empty stack
     */
    public static ItemStack stackOf(Player player, ScanTarget target) {
        if (!(target instanceof ScannedBlock(var pos))) {
            return target.carriedStack();
        }
        BlockState state = player.level().getBlockState(pos);
        if (impl().hidesItemForm(state)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = state.getCloneItemStack(player.level(), pos, false);
        FluidState fluid = state.getFluidState();
        return stack.isEmpty() && !fluid.isEmpty() ? new ItemStack(fluid.getType().getBucket()) : stack;
    }

    /**
     * @param player the scanning player
     * @param target the target
     * @return the aspects of the scanned creature, or of the target's item form; empty when it has neither
     */
    public static AspectList aspectsOf(Player player, ScanTarget target) {
        Entity creature = target.creature();
        if (creature != null) {
            return entityAspects(creature);
        }
        ItemStack stack = stackOf(player, target);
        return stack.isEmpty() ? AspectList.EMPTY : itemAspects(stack);
    }

    /**
     * @param stack a stack
     * @return the stack's aspects as the aspect index sees them
     */
    public static AspectList itemAspects(ItemStack stack) {
        return impl().itemAspects(stack);
    }

    /**
     * @param entity an entity
     * @return the entity's aspects as the aspect index sees them
     */
    public static AspectList entityAspects(Entity entity) {
        return impl().entityAspects(entity);
    }

    /**
     * Advances a research by one scan step for a player. Server side only.
     *
     * @param player   the player
     * @param research the research key
     * @return whether the research advanced
     */
    public static boolean progressResearch(Player player, Identifier research) {
        return impl().progressResearch(player, research);
    }

    /**
     * Grants knowledge points to a player. Server side only.
     *
     * @param player   the player
     * @param type     the knowledge type
     * @param category the research category
     * @param amount   the points
     * @return whether any knowledge was granted
     */
    public static boolean addKnowledge(Player player, KnowledgeType type, Identifier category, int amount) {
        return impl().addKnowledge(player, type, category, amount);
    }

    /**
     * Installs the implementation. Called once by Thaumaturge during mod construction; addons must not call it.
     *
     * @param impl the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings impl) {
        if (bindings != null) {
            throw new IllegalStateException("ScanningManager already bound");
        }
        bindings = impl;
    }

    private static Bindings impl() {
        if (bindings == null) {
            throw new IllegalStateException("ScanningManager accessed before binding");
        }
        return bindings;
    }

    /**
     * The hooks Thaumaturge implements behind this facade.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * @param player   the player
         * @param research the research key
         * @return whether the research advanced
         */
        boolean progressResearch(Player player, Identifier research);

        /**
         * @param player   the player
         * @param type     the knowledge type
         * @param category the category
         * @param amount   the points
         * @return whether knowledge was granted
         */
        boolean addKnowledge(Player player, KnowledgeType type, Identifier category, int amount);

        /**
         * @param stack a stack
         * @return the stack's aspects
         */
        AspectList itemAspects(ItemStack stack);

        /**
         * @param entity an entity
         * @return the entity's aspects
         */
        AspectList entityAspects(Entity entity);

        /**
         * @param state a block state
         * @return whether the block should not be scanned through its item form
         */
        boolean hidesItemForm(BlockState state);
    }
}
