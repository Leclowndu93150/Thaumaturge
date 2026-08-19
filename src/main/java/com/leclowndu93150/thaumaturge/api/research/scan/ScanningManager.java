package com.leclowndu93150.thaumaturge.api.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

/**
 * Registry and dispatcher for everything the thaumometer can scan. Addons register
 * {@link IScanThing} subjects at mod init; the thaumometer routes every scan attempt through
 * {@link #scanTheThing scanTheThing}.
 *
 * <p>Knowledge and aspect lookups go through a binding installed by the implementation at mod
 * init; addons must not call {@link #bind bind}.
 *
 * @since 1.0.0
 */
public final class ScanningManager {
    private static final List<IScanThing> THINGS = new ArrayList<>();
    private static final int MAX_CONTAINER_SCANS = 100;
    private static Bindings impl;

    private ScanningManager() {}

    /**
     * Internal operations the manager delegates to the implementation.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * Grants the given research key, advancing the entry when one exists for the key or
         * recording the bare key otherwise.
         *
         * @param player the player; must be a server player to take effect
         * @param research the research key
         * @return {@code true} when the player's record changed
         */
        boolean progressResearch(Player player, ResourceLocation research);

        /**
         * Awards raw knowledge of the given type toward a category.
         *
         * @param player the player; must be a server player to take effect
         * @param type the knowledge type
         * @param category the research category identifier
         * @param amount the amount of raw knowledge to add
         * @return {@code true} when knowledge was added
         */
        boolean addKnowledge(Player player, KnowledgeType type, ResourceLocation category, int amount);

        /**
         * The effective aspect composition of an item stack.
         *
         * @param stack the stack
         * @return the aspects, or {@link AspectList#EMPTY} when none
         */
        AspectList itemAspects(ItemStack stack);

        /**
         * The registered aspect composition of an entity.
         *
         * @param entity the entity
         * @return the aspects, or {@link AspectList#EMPTY} when none
         */
        AspectList entityAspects(Entity entity);
    }

    /**
     * Registers a scannable subject. Call during mod init or later; registration is not
     * thread-safe and must happen on the main thread.
     *
     * @param thing the subject to add
     */
    public static void addScannableThing(IScanThing thing) {
        THINGS.add(thing);
    }

    /**
     * Scans the given target for the player, granting every matching subject's research key and
     * running its success hook. Sends the player an action-bar result message. When the target is
     * a block position exposing an item handler, every contained stack is scanned as well, up to
     * an internal limit.
     *
     * <p>Server-side only; calling on the client does nothing useful because research grants
     * require a server player.
     *
     * @param player the scanning player
     * @param target the scan target; entity, stack, position, or {@code null}
     */
    public static void scanTheThing(Player player, @Nullable Object target) {
        boolean found = false;
        boolean suppress = false;
        Component failure = null;
        List<IScanThing> matchingThings = new ArrayList<>();
        for (IScanThing thing : THINGS) {
            if (!thing.checkThing(player, target)) {
                continue;
            }
            matchingThings.add(thing);
            Component fail = thing.scanFailure(player, target);
            if (failure == null && fail != null) {
                failure = fail;
            }
        }
        if (failure != null) {
            player.displayClientMessage(
                    failure.copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
            return;
        }
        for (IScanThing thing : matchingThings) {
            ResourceLocation key = thing.getResearchKey(player, target);
            if (key != null && !bindingOrThrow().progressResearch(player, key)) {
                continue;
            }
            if (key == null) {
                suppress = true;
            }
            found = true;
            thing.onSuccess(player, target);
        }
        for (ScanEntry entry : scanEntries(player)) {
            if (entry.matches(player, target) && bindingOrThrow().progressResearch(player, entry.key())) {
                found = true;
            }
        }
        if (!suppress) {
            if (found) {
                player.displayClientMessage(
                        Component.translatable("tc.knownobject").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),
                        true);
            } else {
                player.displayClientMessage(
                        Component.translatable("tc.unknownobject")
                                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
                        true);
            }
        }
        if (target instanceof BlockPos pos) {
            IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
            if (handler != null) {
                int scanned = 0;
                for (int index = 0; index < handler.getSlots(); index++) {
                    ItemStack held = handler.getStackInSlot(index);
                    if (!held.isEmpty()) {
                        scanTheThing(player, held);
                        scanned++;
                    }
                    if (scanned >= MAX_CONTAINER_SCANS) {
                        player.displayClientMessage(
                                Component.translatable("tc.invtoolarge")
                                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
                                true);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns whether any subject still has something to teach the player about the target.
     * Safe to call on the client; reads the synced knowledge copy.
     *
     * @param player the player
     * @param target the scan target
     * @return {@code true} when scanning the target would grant something new
     */
    public static boolean isThingStillScannable(Player player, @Nullable Object target) {
        for (IScanThing thing : THINGS) {
            if (!thing.checkThing(player, target)) {
                continue;
            }
            ResourceLocation key = thing.getResearchKey(player, target);
            if (key != null && !KnowledgeAccess.of(player).isResearchKnown(key)) {
                return true;
            }
        }
        for (ScanEntry entry : scanEntries(player)) {
            if (entry.matches(player, target) && !KnowledgeAccess.of(player).isResearchKnown(entry.key())) {
                return true;
            }
        }
        return false;
    }

    private static Iterable<ScanEntry> scanEntries(Player player) {
        return player.level()
                .registryAccess()
                .registry(ScanEntry.REGISTRY_KEY)
                .<Iterable<ScanEntry>>map(registry ->
                        registry.holders().map(Holder.Reference::value).toList())
                .orElse(List.of());
    }

    /**
     * Normalizes a scan target into an item stack when possible. Item entities yield their stack,
     * positions yield the block's clone item, and fluid blocks yield the fluid's bucket.
     *
     * @param player the scanning player, used for level access
     * @param target the scan target
     * @return the stack, or {@link ItemStack#EMPTY} when the target has no item form
     */
    public static ItemStack getItemFromParms(Player player, @Nullable Object target) {
        if (target instanceof ItemStack stack) {
            return stack;
        }
        if (target instanceof ItemEntity itemEntity) {
            return itemEntity.getItem();
        }
        if (target instanceof BlockPos pos) {
            BlockState state = player.level().getBlockState(pos);
            ItemStack stack = state.getBlock().getCloneItemStack(player.level(), pos, state);
            if (stack.isEmpty()) {
                FluidState fluid = state.getFluidState();
                if (!fluid.isEmpty()) {
                    stack = new ItemStack(fluid.getType().getBucket());
                }
            }
            return stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * The effective aspects of an item stack, resolved through the bound implementation.
     *
     * @param stack the stack
     * @return the aspects, or {@link AspectList#EMPTY} when none
     */
    public static AspectList itemAspects(ItemStack stack) {
        return bindingOrThrow().itemAspects(stack);
    }

    /**
     * The registered aspects of an entity, resolved through the bound implementation.
     *
     * @param entity the entity
     * @return the aspects, or {@link AspectList#EMPTY} when none
     */
    public static AspectList entityAspects(Entity entity) {
        return bindingOrThrow().entityAspects(entity);
    }

    /**
     * Grants a research key through the bound implementation.
     *
     * @param player the player; must be a server player to take effect
     * @param research the research key
     * @return {@code true} when the player's record changed
     */
    public static boolean progressResearch(Player player, ResourceLocation research) {
        return bindingOrThrow().progressResearch(player, research);
    }

    /**
     * Awards raw knowledge through the bound implementation.
     *
     * @param player the player; must be a server player to take effect
     * @param type the knowledge type
     * @param category the research category identifier
     * @param amount the amount to add
     * @return {@code true} when knowledge was added
     */
    public static boolean addKnowledge(Player player, KnowledgeType type, ResourceLocation category, int amount) {
        return bindingOrThrow().addKnowledge(player, type, category, amount);
    }

    /**
     * Binds the implementation. Called once at mod init; addons must not call this.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        if (impl != null) {
            throw new IllegalStateException("ScanningManager already bound");
        }
        impl = bindings;
    }

    private static Bindings bindingOrThrow() {
        if (impl == null) {
            throw new IllegalStateException("ScanningManager accessed before binding");
        }
        return impl;
    }
}
