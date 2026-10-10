package com.leclowndu93150.thaumaturge.api.research.scan;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Registry and dispatcher for everything the thaumometer can scan.
 *
 * <p>A scan asks every registered {@link IScannable} and every {@link ScanEntry} whether it matches the target, advances the research
 * of each match, then tells the player what happened. Scanning an inventory also scans all distinct stacks exposed by its item
 * handlers, or its vanilla container when it has no item handler.
 *
 * <p>Registration is not thread-safe; register during mod construction only. Knowledge and aspect lookups go through a binding the mod
 * installs at construction; addons must not call {@link #bind}.
 *
 * @since 1.0.0
 */
public final class ScanningManager {
    private static final List<IScannable> SUBJECTS = new ArrayList<>();
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("ScanningManager");

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
        if (player.level().isClientSide()) {
            return;
        }
        List<ItemStack> contents = ScanInventories.contents(player, target);
        List<ScanEntry> entries = entries(player);
        ScanResult result = scanTarget(player, target, entries);
        if (contents.isEmpty()) {
            report(player, result.refusal(), result.found(), result.silent());
            return;
        }
        List<ItemStack> pending = new ArrayList<>(contents);
        boolean progressed;
        do {
            progressed = false;
            List<ItemStack> refused = new ArrayList<>();
            for (ItemStack stack : pending) {
                ScanResult item = scanTarget(player, ScanTarget.stack(stack), entries);
                result = result.merge(item);
                progressed |= item.progressed();
                if (item.refusal() != null) {
                    refused.add(stack);
                }
            }
            pending = refused;
        } while (progressed && !pending.isEmpty());
        report(player, result.found() ? null : result.refusal(), result.found(), result.silent());
    }

    private record ScanResult(
            boolean found, boolean silent, @Nullable Component refusal, boolean progressed) {
        ScanResult merge(ScanResult other) {
            return new ScanResult(
                    found || other.found,
                    silent || other.silent,
                    refusal == null ? other.refusal : refusal,
                    progressed || other.progressed);
        }
    }

    private static ScanResult scanTarget(Player player, ScanTarget target, List<ScanEntry> entries) {
        boolean found = false;
        boolean silent = false;
        boolean progressed = false;
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
            ResourceLocation research = subject.research(player, target);
            if (research != null) {
                boolean advanced = advance(player, research);
                if (!advanced
                        && !(KnowledgeAccess.of(player).isResearchKnown(research)
                                && subject.rescannable(player, target))) {
                    continue;
                }
                progressed |= advanced;
            }
            silent |= research == null;
            found = true;
            subject.onScanned(player, target);
        }
        for (ScanEntry entry : entries) {
            if (entry.matches(player, target) && advance(player, entry.key())) {
                found = true;
                progressed = true;
            }
        }
        return new ScanResult(found, silent, refusal, progressed);
    }

    private static boolean advance(Player player, ResourceLocation research) {
        return BINDING.get().progressResearch(player, research);
    }

    private static void report(Player player, @Nullable Component refusal, boolean found, boolean silent) {
        if (refusal != null) {
            player.displayClientMessage(
                    refusal.copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
        } else if (!silent) {
            player.displayClientMessage(
                    found
                            ? Component.translatable("message.thaumaturge.scan.learned")
                                    .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC)
                            : Component.translatable("message.thaumaturge.scan.nothing_new")
                                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
                    true);
        }
    }

    /**
     * @param player the scanning player
     * @param target the candidate target
     * @return whether the target has an inventory, could still advance research the player lacks, or would trigger a keyless subject
     */
    public static boolean isStillScannable(Player player, ScanTarget target) {
        if (ScanInventories.hasInventory(player, target)) {
            return true;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        for (IScannable subject : SUBJECTS) {
            if (!subject.matches(player, target)) {
                continue;
            }
            ResourceLocation research = subject.research(player, target);
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
        return player.level()
                .registryAccess()
                .lookup(ScanEntry.REGISTRY_KEY)
                .map(registry ->
                        registry.listElements().map(Holder.Reference::value).toList())
                .orElse(List.of());
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
        if (BINDING.get().hidesItemForm(state)) {
            return ItemStack.EMPTY;
        }
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        ItemStack stack = state.getCloneItemStack(hit, player.level(), pos, player);
        FluidState fluid = state.getFluidState();
        return stack.isEmpty() && !fluid.isEmpty()
                ? new ItemStack(fluid.getType().getBucket())
                : stack;
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
        return BINDING.get().itemAspects(stack);
    }

    /**
     * @param entity an entity
     * @return the entity's aspects as the aspect index sees them
     */
    public static AspectList entityAspects(Entity entity) {
        return BINDING.get().entityAspects(entity);
    }

    /**
     * Advances a research by one scan step for a player. Server side only.
     *
     * @param player   the player
     * @param research the research key
     * @return whether the research advanced
     */
    public static boolean progressResearch(Player player, ResourceLocation research) {
        return BINDING.get().progressResearch(player, research);
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
    public static boolean addKnowledge(Player player, KnowledgeType type, ResourceLocation category, int amount) {
        return BINDING.get().addKnowledge(player, type, category, amount);
    }

    /**
     * Installs the implementation. Called once by Thaumaturge during mod construction; addons must not call it.
     *
     * @param impl the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings impl) {
        BINDING.bind(impl);
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
        boolean progressResearch(Player player, ResourceLocation research);

        /**
         * @param player   the player
         * @param type     the knowledge type
         * @param category the category
         * @param amount   the points
         * @return whether knowledge was granted
         */
        boolean addKnowledge(Player player, KnowledgeType type, ResourceLocation category, int amount);

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
