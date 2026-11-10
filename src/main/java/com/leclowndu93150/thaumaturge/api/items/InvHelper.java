package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.api.transaction.SnapshotJournal;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jspecify.annotations.Nullable;

/**
 * Inventory access and filter matching used by golem seals and provisioning, built on the
 * NeoForge item handler capability.
 *
 * @since 1.0.0
 */
public final class InvHelper {
    private InvHelper() {}

    /**
     * Comparison flags for filter matching.
     *
     * @since 1.0.0
     * @deprecated use immutable {@link ItemMatch} instances
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static final class InvFilter {
        /**
         * Compares item, damage and all components exactly.
         *
         * @deprecated use {@link ItemMatch#EXACT}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public static final InvFilter STRICT = new InvFilter(ItemMatch.EXACT, true);

        private final boolean shared;
        private final boolean ignoreDamage;
        private final boolean ignoreComponents;
        private final boolean useTags;
        private final boolean useMod;
        private boolean relaxedComponents;

        private InvFilter(ItemMatch match, boolean shared) {
            this.ignoreDamage = match.anyDamage();
            this.ignoreComponents = match.anyComponents();
            this.useTags = match.sharedTag();
            this.useMod = match.sameMod();
            this.relaxedComponents = match.componentSubset();
            this.shared = shared;
        }

        /**
         * @deprecated use {@link ItemMatch#ItemMatch(boolean, boolean, boolean, boolean, boolean)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public InvFilter(boolean ignoreDamage, boolean ignoreComponents, boolean useTags, boolean useMod) {
            this(new ItemMatch(ignoreDamage, ignoreComponents, false, useTags, useMod), false);
        }

        /**
         * Relaxes component matching: the first stack's components must be present on the
         * second, which may carry extras.
         *
         * @return this filter
         * @deprecated use {@link ItemMatch#withComponentSubset(boolean)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public InvFilter setRelaxedComponents() {
            if (shared) {
                return new InvFilter(new ItemMatch(ignoreDamage, ignoreComponents, true, useTags, useMod), false);
            }
            this.relaxedComponents = true;
            return this;
        }
    }

    /**
     * A filter match result: the matched stack and the size limit of the filter slot that
     * matched, or zero when unlimited.
     *
     * @since 1.0.0
     */
    public record FilterMatch(ItemStack stack, int sizeLimit) {}

    /** Returns the item handler exposed at a block face. */
    public static @Nullable IItemHandler handlerAt(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
    }

    /**
     * @param level the level
     * @param pos   the block to query
     * @param side  the side to access from, or null for the unsided handler
     * @return the item handler there, or null when absent
     * @deprecated use {@link #handlerAt(Level, BlockPos, Direction)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static @Nullable IItemHandler getItemHandlerAt(Level level, BlockPos pos, @Nullable Direction side) {
        return handlerAt(level, pos, side);
    }

    /**
     * Inserts a stack into a handler.
     *
     * @return the remainder that did not fit
     */
    public static ItemStack insertStack(@Nullable IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        return ItemHandlerHelper.insertItemStacked(handler, stack.copy(), simulate);
    }

    /**
     * Inserts a stack into the inventory at a position.
     *
     * @return the remainder that did not fit
     * @deprecated use {@link #insertAt(Level, BlockPos, Direction, ItemStack, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack insertStackAt(
            Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        return insertAt(level, pos, side, stack, simulate);
    }

    /** Inserts a stack at a block face and returns its uninserted remainder. */
    public static ItemStack insertAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        IItemHandler inventory = handlerAt(level, pos, side);
        return inventory != null ? insertStack(inventory, stack, simulate) : stack;
    }

    /**
     * @return a copy of the portion of the stack the inventory can accept
     * @deprecated use {@link #acceptedPortion(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack hasRoomFor(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return acceptedPortion(level, pos, side, stack);
    }

    /** Returns a copy containing the portion that could be accepted at a block face. */
    public static ItemStack acceptedPortion(Level level, BlockPos pos, Direction side, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack rejected = insertAt(level, pos, side, stack, true);
        int accepted = stack.getCount() - (rejected.isEmpty() ? 0 : rejected.getCount());
        return accepted <= 0 ? ItemStack.EMPTY : stack.copyWithCount(accepted);
    }

    /**
     * @return whether the inventory can accept at least one item of the stack
     * @deprecated use {@link #acceptsAny(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean hasRoomForSome(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return acceptsAny(level, pos, side, stack);
    }

    /** Returns whether at least one item (or an empty stack) could be accepted at a block face. */
    public static boolean acceptsAny(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return stack.isEmpty() || !acceptedPortion(level, pos, side, stack).isEmpty();
    }

    /**
     * @return whether the inventory can accept the whole stack
     * @deprecated use {@link #acceptsAll(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean hasRoomForAll(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return acceptsAll(level, pos, side, stack);
    }

    /** Returns whether the complete stack could be accepted at a block face. */
    public static boolean acceptsAll(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return stack.isEmpty() || insertAt(level, pos, side, stack, true).isEmpty();
    }

    /** Counts matching items in a handler. */
    public static int count(@Nullable IItemHandler inventory, ItemStack wanted, ItemMatch match) {
        if (inventory == null || wanted.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack held = inventory.getStackInSlot(slot);
            if (!held.isEmpty() && match.test(wanted, held)) {
                count += held.getCount();
            }
        }
        return count;
    }

    /** Counts matching items at a block face. */
    public static int countAt(Level level, BlockPos pos, Direction side, ItemStack wanted, ItemMatch match) {
        return count(handlerAt(level, pos, side), wanted, match);
    }

    /** Counts matching dropped items around a position. */
    public static int countDropped(Level level, BlockPos pos, ItemStack wanted, double range, ItemMatch match) {
        if (wanted.isEmpty()) {
            return 0;
        }
        int count = 0;
        AABB area = new AABB(pos).inflate(range);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (!item.getItem().isEmpty() && match.test(wanted, item.getItem())) {
                count += item.getItem().getCount();
            }
        }
        return count;
    }

    /** Extracts matching items from a handler, returning the amount actually removed. */
    public static ItemStack extract(
            @Nullable IItemHandler inventory, ItemStack wanted, ItemMatch match, boolean simulate) {
        if (inventory == null || wanted.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int remaining = wanted.getCount();
        int removed = 0;
        for (int slot = 0; slot < inventory.getSlots() && remaining > 0; slot++) {
            ItemStack held = inventory.getStackInSlot(slot);
            if (held.isEmpty() || !match.test(wanted, held)) {
                continue;
            }
            ItemStack extracted = inventory.extractItem(slot, remaining, simulate);
            int amount = !extracted.isEmpty() && match.test(wanted, extracted)
                    ? Math.min(remaining, extracted.getCount())
                    : 0;
            removed += amount;
            remaining -= amount;
        }
        return removed == 0 ? ItemStack.EMPTY : wanted.copyWithCount(removed);
    }

    /** Extracts matching items from the handler exposed at a block face. */
    public static ItemStack extractAt(
            Level level, BlockPos pos, Direction side, ItemStack wanted, ItemMatch match, boolean simulate) {
        return extract(handlerAt(level, pos, side), wanted, match, simulate);
    }

    /** Inserts into the adjacent inventory, dropping any remainder into the world. */
    public static void eject(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ItemStack remainder = insertAt(level, pos.relative(side), side.getOpposite(), stack, false);
        dropRemainder(level, pos, side, remainder);
    }

    /** Inserts into the adjacent inventory and returns the remainder when one is present. */
    public static ItemStack ejectOrReturn(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ItemStack remainder = insertAt(level, pos.relative(side), side.getOpposite(), stack, false);
        if (handlerAt(level, pos.relative(side), side.getOpposite()) != null) {
            return remainder;
        }
        dropRemainder(level, pos, side, remainder);
        return ItemStack.EMPTY;
    }

    /** Drops an item at an entity's chest height on the logical server. */
    public static void dropAt(Level level, ItemStack stack, Entity entity) {
        if (!level.isClientSide() && !stack.isEmpty()) {
            level.addFreshEntity(new ItemEntity(
                    level, entity.getX(), entity.getY() + entity.getEyeHeight() / 2.0F, entity.getZ(), stack.copy()));
        }
    }

    /** Returns a copy capped to the requested count. */
    public static ItemStack capped(ItemStack stack, int limit) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        if (copy.getCount() > limit) {
            copy.setCount(limit);
        }
        return copy;
    }

    /** Returns whether adjacent inventories, excluding the top, hold the requested amount. */
    public static boolean neighboursHold(Level level, BlockPos pos, ItemStack wanted) {
        return countNeighbours(level, pos, wanted, ItemMatch.ITEM_OR_TAG) >= wanted.getCount();
    }

    /** Returns whether the player's main inventory holds the requested amount. */
    public static boolean playerHolds(Player player, ItemStack wanted, boolean byTag) {
        if (wanted.isEmpty()) {
            return false;
        }
        ItemMatch match = ItemMatch.ITEM_OR_TAG
                .byTag(byTag)
                .ignoringComponents(wanted.isComponentsPatchEmpty())
                .withComponentSubset(true);
        int count = 0;
        for (ItemStack held : player.getInventory().items) {
            if (!held.isEmpty() && match.test(wanted, held)) {
                count += held.getCount();
                if (count >= wanted.getCount()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Removes the requested amount from the player's main inventory, or makes no change. */
    public static boolean takeFromPlayer(Player player, ItemStack wanted, boolean byTag) {
        return drainPlayer(player, wanted, byTag, false);
    }

    /**
     * Removes the requested stack from adjacent inventories, excluding the top face.
     * The operation is atomic and fails without mutation if the amount is unavailable or
     * any required provider cannot be safely restored after a failed extraction.
     */
    public static boolean takeFromNeighbours(Level level, BlockPos pos, ItemStack wanted) {
        return !level.isClientSide() && takeFromSources(level, pos, null, false, wanted);
    }

    /**
     * Draws each ingredient from adjacent inventories first, then the player's main inventory.
     * Each ingredient must be independently available from either the adjacent inventories or
     * the player. The complete draw is atomic across all ingredients.
     *
     * <p>Simulation can inspect any item-handler provider. An executing draw fails closed if
     * it would need to mutate a provider that does not implement {@link IItemHandlerModifiable}.
     */
    public static boolean takeFromNeighboursOrPlayer(
            Level level, BlockPos pos, Player player, boolean simulate, ItemStack... wanted) {
        return (simulate || !level.isClientSide()) && takeFromSources(level, pos, player, simulate, wanted);
    }

    /** Finds the first accepted candidate using modern immutable match rules. */
    public static ItemStack findFirstMatchFromFilter(
            List<ItemStack> ghosts,
            boolean blacklist,
            @Nullable IItemHandler inventory,
            ItemMatch match,
            boolean leaveOne) {
        return StackFilter.of(ghosts, blacklist, match).firstAccepted(inventory, leaveOne);
    }

    /** Tests a candidate against a ghost-stack filter. */
    public static boolean matchesFilters(List<ItemStack> ghosts, boolean blacklist, ItemStack stack, ItemMatch match) {
        return StackFilter.of(ghosts, blacklist, match).accepts(stack);
    }

    /** Finds the first candidate that passes the filter. */
    public static ItemStack findFirstMatchFromFilter(
            List<ItemStack> ghosts,
            List<Integer> sizes,
            boolean blacklist,
            List<ItemStack> candidates,
            ItemMatch match) {
        return new StackFilter(ghosts, sizes, blacklist, match).firstAccepted(candidates);
    }

    /** Finds the first candidate that passes the filter, including its applicable size limit. */
    public static FilterMatch findFirstMatchFromFilterWithSize(
            List<ItemStack> ghosts,
            List<Integer> sizes,
            boolean blacklist,
            List<ItemStack> candidates,
            ItemMatch match) {
        return new StackFilter(ghosts, sizes, blacklist, match).firstAcceptedWithLimit(candidates);
    }

    /**
     * @return the total count of matching items in the handler
     * @deprecated use {@link #count(IItemHandler, ItemStack, ItemMatch)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static int countTotalItemsIn(@Nullable IItemHandler inventory, ItemStack stack, InvFilter filter) {
        return count(inventory, stack, itemMatch(filter));
    }

    /**
     * @return the total count of matching items in the inventory at a position
     * @deprecated use {@link #countAt(Level, BlockPos, Direction, ItemStack, ItemMatch)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static int countTotalItemsIn(Level level, BlockPos pos, Direction side, ItemStack stack, InvFilter filter) {
        return countAt(level, pos, side, stack, itemMatch(filter));
    }

    /**
     * Compares two stacks under the filter's flags. Counts are ignored.
     * @deprecated use {@link ItemMatch#test(ItemStack, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean areItemStacksEqual(ItemStack first, ItemStack second, InvFilter filter) {
        return itemMatch(filter).test(first, second);
    }

    private static ItemMatch itemMatch(InvFilter filter) {
        return new ItemMatch(
                filter.ignoreDamage, filter.ignoreComponents, filter.relaxedComponents, filter.useTags, filter.useMod);
    }

    /**
     * Finds the first stack in a handler that passes a ghost-stack filter.
     *
     * @param filterStacks the ghost stacks
     * @param blacklist    whether matching stacks are excluded instead of required
     * @param inv          the inventory to scan
     * @param filter       the comparison flags
     * @param leaveOne     whether the last item of a kind must stay behind
     * @return the first matching stack, or an empty stack
     * @deprecated use {@link StackFilter#firstAccepted(IItemHandler, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack findFirstMatchFromFilter(
            List<ItemStack> filterStacks, boolean blacklist, IItemHandler inv, InvFilter filter, boolean leaveOne) {
        return StackFilter.of(filterStacks, blacklist, itemMatch(filter)).firstAccepted(inv, leaveOne);
    }

    /**
     * @return whether a stack passes a ghost-stack filter
     * @deprecated use {@link StackFilter#accepts(ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean matchesFilters(
            List<ItemStack> filterStacks, boolean blacklist, ItemStack stack, InvFilter filter) {
        return StackFilter.of(filterStacks, blacklist, itemMatch(filter)).accepts(stack);
    }

    /**
     * Finds the first candidate stack that passes a ghost-stack filter.
     *
     * @return the matching stack, or an empty stack
     * @deprecated use {@link StackFilter#firstAccepted(List)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack findFirstMatchFromFilter(
            List<ItemStack> filterStacks,
            List<Integer> filterSizes,
            boolean blacklist,
            List<ItemStack> candidates,
            InvFilter filter) {
        return new StackFilter(filterStacks, filterSizes, blacklist, itemMatch(filter)).firstAccepted(candidates);
    }

    /**
     * Finds the first candidate stack that passes a ghost-stack filter, with the matching
     * slot's size limit.
     * @deprecated use {@link StackFilter#firstAcceptedWithLimit(List)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static FilterMatch findFirstMatchFromFilterWithSize(
            List<ItemStack> filterStacks,
            List<Integer> filterSizes,
            boolean blacklist,
            List<ItemStack> candidates,
            InvFilter filter) {
        return new StackFilter(filterStacks, filterSizes, blacklist, itemMatch(filter))
                .firstAcceptedWithLimit(candidates);
    }

    /**
     * @return a copy of the stack, capped at the given count
     * @deprecated use {@link #capped(ItemStack, int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack copyLimitedStack(ItemStack stack, int limit) {
        return capped(stack, limit);
    }

    /**
     * Removes up to the stack's count of matching items from the inventory at a position.
     *
     * @return the removed items, or an empty stack
     * @deprecated use {@link #extractAt(Level, BlockPos, Direction, ItemStack, ItemMatch, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack removeStackFrom(
            Level level, BlockPos pos, Direction side, ItemStack stack, InvFilter filter, boolean simulate) {
        return extractAt(level, pos, side, stack, itemMatch(filter), simulate);
    }

    /**
     * Removes up to the stack's count of matching items from a handler.
     *
     * @return the removed items, or an empty stack
     * @deprecated use {@link #extract(IItemHandler, ItemStack, ItemMatch, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack removeStackFrom(
            @Nullable IItemHandler inventory, ItemStack stack, InvFilter filter, boolean simulate) {
        return extract(inventory, stack, itemMatch(filter), simulate);
    }

    /**
     * Pushes a stack into the inventory beyond the given side, spilling the remainder into
     * the world as an item entity.
     * @deprecated use {@link #eject(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out) {
        eject(level, pos, side, out);
    }

    /**
     * Pushes a stack into the inventory beyond the given side. In smart mode the remainder
     * is returned instead of spilled when an inventory exists there.
     *
     * @return the remainder in smart mode, otherwise an empty stack
     * @deprecated use {@link #ejectOrReturn(Level, BlockPos, Direction, ItemStack)} or {@link #eject(Level, BlockPos, Direction, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out, boolean smart) {
        return smart ? ejectOrReturn(level, pos, side, out) : dropEjected(level, pos, side, out);
    }

    private static ItemStack dropEjected(Level level, BlockPos pos, Direction side, ItemStack out) {
        ItemStack remainder = insertAt(level, pos.relative(side), side.getOpposite(), out, false);
        dropRemainder(level, pos, side, remainder);
        return ItemStack.EMPTY;
    }

    private static void dropRemainder(Level level, BlockPos pos, Direction side, ItemStack remainder) {
        if (remainder.isEmpty()) {
            return;
        }
        BlockPos spawnPos = pos;
        BlockPos target = pos.relative(side);
        if (level.getBlockState(target).isCollisionShapeFullBlock(level, target)) {
            spawnPos = pos.relative(side.getOpposite());
        }
        ItemEntity entity = new ItemEntity(
                level,
                spawnPos.getX() + 0.5 + side.getStepX(),
                spawnPos.getY() + side.getStepY(),
                spawnPos.getZ() + 0.5 + side.getStepZ(),
                remainder);
        entity.setDeltaMovement(0.3 * side.getStepX(), 0.3 * side.getStepY(), 0.3 * side.getStepZ());
        level.addFreshEntity(entity);
    }

    /**
     * @return the total count of matching dropped items around a position
     * @deprecated use {@link #countDropped(Level, BlockPos, ItemStack, double, ItemMatch)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static int countStackInWorld(Level level, BlockPos pos, ItemStack stack, double range, InvFilter filter) {
        return countDropped(level, pos, stack, range, itemMatch(filter));
    }

    /**
     * Drops a stack at an entity's chest height. Server side only.
     * @deprecated use {@link #dropAt(Level, ItemStack, Entity)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void dropItemAtEntity(Level level, ItemStack stack, Entity entity) {
        dropAt(level, stack, entity);
    }

    /**
     * Matches by item identity or shared tag.
     * @deprecated use {@link ItemMatch#ITEM_OR_TAG}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static final InvFilter BASE_TAGS = new InvFilter(ItemMatch.ITEM_OR_TAG, true);

    /**
     * @return whether inventories adjacent to the position, ignoring the top, together hold
     *         the stack's count of matching items
     * @deprecated use {@link #neighboursHold(Level, BlockPos, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean checkAdjacentChests(Level level, BlockPos pos, ItemStack stack) {
        return neighboursHold(level, pos, stack);
    }

    /**
     * Removes the stack's count of matching items from inventories adjacent to the position.
     *
     * @return whether the full amount was consumed
     * @deprecated use {@link #takeFromNeighbours(Level, BlockPos, ItemStack)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean consumeFromAdjacentChests(Level level, BlockPos pos, ItemStack stack) {
        return takeFromNeighbours(level, pos, stack);
    }

    /**
     * @return whether the player's main inventory holds the stack's count of matching items
     * @deprecated use {@link #playerHolds(Player, ItemStack, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean isPlayerCarryingAmount(Player player, ItemStack stack, boolean useTags) {
        return playerHolds(player, stack, useTags);
    }

    /**
     * Removes the stack's count of matching items from the player's main inventory.
     *
     * @param skipCheck whether to skip the carrying pre-check
     * @return whether the full amount was consumed
     * @deprecated use {@link #takeFromPlayer(Player, ItemStack, boolean)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean consumePlayerItem(Player player, ItemStack stack, boolean skipCheck, boolean useTags) {
        return drainPlayer(player, stack, useTags, skipCheck);
    }

    private static boolean drainPlayer(Player player, ItemStack wanted, boolean byTag, boolean keepPartial) {
        if (wanted.isEmpty()) {
            return false;
        }
        ItemMatch match = playerMatch(wanted, byTag);
        PlayerInventoryJournal journal = new PlayerInventoryJournal(player.getInventory());
        try (Transaction transaction = Transaction.openRoot()) {
            int removed = journal.drain(wanted, match, wanted.getCount(), transaction);
            boolean complete = removed == wanted.getCount();
            if (complete || keepPartial) {
                transaction.commit();
            }
            return complete;
        }
    }

    private static ItemMatch playerMatch(ItemStack wanted, boolean byTag) {
        return ItemMatch.EXACT
                .byTag(byTag)
                .ignoringComponents(wanted.isComponentsPatchEmpty())
                .withComponentSubset(true);
    }

    /** One reusable inventory participant for all player drains in an outer transaction. */
    static final class PlayerInventoryJournal extends SnapshotJournal<PlayerInventoryJournal.InventorySnapshot> {
        private final Inventory inventory;

        PlayerInventoryJournal(Inventory inventory) {
            this.inventory = inventory;
        }

        int drain(ItemStack wanted, ItemMatch match, int amount, TransactionContext transaction) {
            int remaining = amount;
            List<ItemStack> items = inventory.items;
            for (int slot = 0; slot < items.size(); slot++) {
                if (remaining <= 0) {
                    break;
                }
                ItemStack held = items.get(slot);
                if (held.isEmpty() || !match.test(wanted, held)) {
                    continue;
                }
                int taken = Math.min(remaining, held.getCount());
                updateSnapshots(transaction);
                if (taken == held.getCount()) {
                    items.set(slot, ItemStack.EMPTY);
                } else {
                    items.set(slot, held.copyWithCount(held.getCount() - taken));
                }
                remaining -= taken;
            }
            return amount - remaining;
        }

        @Override
        protected InventorySnapshot createSnapshot() {
            List<ItemStack> stacks = new ArrayList<>(inventory.items);
            int[] counts = new int[stacks.size()];
            for (int slot = 0; slot < stacks.size(); slot++) {
                counts[slot] = stacks.get(slot).getCount();
            }
            return new InventorySnapshot(stacks, counts);
        }

        @Override
        protected void revertToSnapshot(InventorySnapshot snapshot) {
            for (int slot = 0; slot < snapshot.stacks().size(); slot++) {
                ItemStack original = snapshot.stacks().get(slot);
                if (!original.isEmpty()) {
                    original.setCount(snapshot.counts()[slot]);
                }
                inventory.items.set(slot, original);
            }
        }

        @Override
        protected void onRootCommit(InventorySnapshot original) {
            inventory.setChanged();
        }

        private record InventorySnapshot(List<ItemStack> stacks, int[] counts) {}
    }

    /**
     * Checks or consumes the given stacks from adjacent inventories first, then the player.
     *
     * @param simulate whether to only check availability
     * @return whether every stack is available or was consumed
     * @deprecated use {@link #takeFromNeighboursOrPlayer(Level, BlockPos, Player, boolean, ItemStack...)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean consumeItemsFromAdjacentInventoryOrPlayer(
            Level level, BlockPos pos, Player player, boolean simulate, ItemStack... items) {
        return takeFromNeighboursOrPlayer(level, pos, player, simulate, items);
    }

    private static boolean takeFromSources(
            Level level, BlockPos pos, @Nullable Player player, boolean simulate, ItemStack... wantedStacks) {
        List<HandlerSource> sources = neighbourSources(level, pos);
        List<PlannedPlayerDrain> playerDrains = new ArrayList<>();
        List<PlannedHandlerDrain> handlerDrains = new ArrayList<>();
        List<ItemStack> virtualPlayer = player == null ? List.of() : copyStacks(player.getInventory().items);
        for (ItemStack wanted : wantedStacks) {
            if (wanted.isEmpty()) {
                continue;
            }
            int neighbourCount = countInSources(sources, wanted, ItemMatch.ITEM_OR_TAG);
            boolean playerCanSupply = player != null && playerHolds(player, wanted, true);
            if (neighbourCount < wanted.getCount() && !playerCanSupply) {
                return false;
            }

            int remaining = wanted.getCount();
            for (HandlerSource source : sources) {
                for (int slot = 0; slot < source.slots().size() && remaining > 0; slot++) {
                    ItemStack held = source.slots().get(slot);
                    if (held.isEmpty() || !ItemMatch.ITEM_OR_TAG.test(wanted, held)) {
                        continue;
                    }
                    int amount = Math.min(remaining, held.getCount());
                    handlerDrains.add(new PlannedHandlerDrain(source.handler(), slot, wanted.copy(), amount));
                    held.shrink(amount);
                    remaining -= amount;
                }
                if (remaining == 0) {
                    break;
                }
            }

            if (remaining > 0 && player != null) {
                ItemMatch match = playerMatch(wanted, true);
                if (countMatching(virtualPlayer, wanted, match) < remaining) {
                    return false;
                }
                playerDrains.add(new PlannedPlayerDrain(wanted.copy(), remaining));
                drainVirtualPlayer(virtualPlayer, wanted, match, remaining);
                remaining = 0;
            }
            if (remaining > 0) {
                return false;
            }
        }

        if (simulate) {
            return true;
        }
        for (PlannedHandlerDrain drain : handlerDrains) {
            if (!(drain.handler() instanceof IItemHandlerModifiable)) {
                return false;
            }
        }

        Map<IItemHandlerModifiable, HandlerSnapshotJournal> journals = new IdentityHashMap<>();
        try (Transaction transaction = Transaction.openRoot()) {
            PlayerInventoryJournal playerJournal = null;
            if (player != null && !playerDrains.isEmpty()) {
                playerJournal = new PlayerInventoryJournal(player.getInventory());
                playerJournal.updateSnapshots(transaction);
            }
            for (PlannedHandlerDrain drain : handlerDrains) {
                IItemHandlerModifiable handler = (IItemHandlerModifiable) drain.handler();
                HandlerSnapshotJournal journal = journals.computeIfAbsent(handler, HandlerSnapshotJournal::new);
                journal.updateSnapshots(transaction);
                ItemStack extracted = handler.extractItem(drain.slot(), drain.amount(), false);
                if (extracted.isEmpty()
                        || extracted.getCount() != drain.amount()
                        || !ItemMatch.ITEM_OR_TAG.test(drain.wanted(), extracted)) {
                    return false;
                }
            }
            if (playerJournal != null) {
                for (PlannedPlayerDrain drain : playerDrains) {
                    if (playerJournal.drain(
                                    drain.wanted(), playerMatch(drain.wanted(), true), drain.amount(), transaction)
                            != drain.amount()) {
                        return false;
                    }
                }
            }
            transaction.commit();
            return true;
        }
    }

    private static void drainVirtualPlayer(List<ItemStack> stacks, ItemStack wanted, ItemMatch match, int amount) {
        int remaining = amount;
        for (ItemStack held : stacks) {
            if (remaining <= 0) {
                return;
            }
            if (!held.isEmpty() && match.test(wanted, held)) {
                int taken = Math.min(remaining, held.getCount());
                held.shrink(taken);
                remaining -= taken;
            }
        }
    }

    private static int countMatching(List<ItemStack> stacks, ItemStack wanted, ItemMatch match) {
        int count = 0;
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty() && match.test(wanted, stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        return stacks.stream().map(ItemStack::copy).toList();
    }

    private static int countNeighbours(Level level, BlockPos pos, ItemStack wanted, ItemMatch match) {
        return countInSources(neighbourSources(level, pos), wanted, match);
    }

    private static int countInSources(List<HandlerSource> sources, ItemStack wanted, ItemMatch match) {
        int count = 0;
        for (HandlerSource source : sources) {
            count += countMatching(source.slots(), wanted, match);
        }
        return count;
    }

    private static List<HandlerSource> neighbourSources(Level level, BlockPos pos) {
        List<HandlerSource> sources = new ArrayList<>();
        Map<IItemHandler, Boolean> seen = new IdentityHashMap<>();
        for (Direction face : Direction.values()) {
            if (face == Direction.UP) {
                continue;
            }
            BlockPos neighbour = pos.relative(face);
            if (!LoadedChunks.contains(level, neighbour)) {
                continue;
            }
            IItemHandler handler = handlerAt(level, neighbour, face.getOpposite());
            if (handler != null && seen.put(handler, Boolean.TRUE) == null) {
                sources.add(new HandlerSource(handler, copyHandlerStacks(handler)));
            }
        }
        return sources;
    }

    private static List<ItemStack> copyHandlerStacks(IItemHandler handler) {
        List<ItemStack> stacks = new ArrayList<>(handler.getSlots());
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            stacks.add(handler.getStackInSlot(slot).copy());
        }
        return stacks;
    }

    private record HandlerSource(IItemHandler handler, List<ItemStack> slots) {}

    private record PlannedHandlerDrain(IItemHandler handler, int slot, ItemStack wanted, int amount) {}

    private record PlannedPlayerDrain(ItemStack wanted, int amount) {}

    private static final class HandlerSnapshotJournal extends SnapshotJournal<List<ItemStack>> {
        private final IItemHandlerModifiable handler;

        private HandlerSnapshotJournal(IItemHandlerModifiable handler) {
            this.handler = handler;
        }

        @Override
        protected List<ItemStack> createSnapshot() {
            return copyHandlerStacks(handler);
        }

        @Override
        protected void revertToSnapshot(List<ItemStack> snapshot) {
            for (int slot = 0; slot < snapshot.size(); slot++) {
                handler.setStackInSlot(slot, snapshot.get(slot).copy());
            }
        }
    }
}
