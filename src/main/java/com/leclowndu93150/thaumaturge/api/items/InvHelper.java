package com.leclowndu93150.thaumaturge.api.items;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

/**
 * Inventory access and filter matching used by golem seals and provisioning. Replaces the Inventory
 * access and filter matching used by golem seals and provisioning, built on the NeoForge resource
 * transfer API.
 *
 * @since 1.0.0
 */
public final class InvHelper {
    private InvHelper() {}

    /**
     * Comparison flags for filter matching. Damage maps the legacy metadata toggle, tags
     * replace the ore dictionary toggle and mod matches by item namespace.
     *
     * @since 1.0.0
     */
    public static final class InvFilter {
        /** Compares item, damage and all components exactly. */
        public static final InvFilter STRICT = new InvFilter(false, false, false, false);

        private final boolean ignoreDamage;
        private final boolean ignoreComponents;
        private final boolean useTags;
        private final boolean useMod;
        private boolean relaxedComponents;

        public InvFilter(boolean ignoreDamage, boolean ignoreComponents, boolean useTags, boolean useMod) {
            this.ignoreDamage = ignoreDamage;
            this.ignoreComponents = ignoreComponents;
            this.useTags = useTags;
            this.useMod = useMod;
        }

        /**
         * Relaxes component matching: the first stack's components must be present on the
         * second, which may carry extras.
         *
         * @return this filter
         */
        public InvFilter setRelaxedComponents() {
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
    public record FilterMatch(ItemStack stack, int sizeLimit) {
    }

    /**
     * @param level the level
     * @param pos   the block to query
     * @param side  the side to access from, or null for the unsided handler
     * @return the item handler there, or null when absent
     */
    public static @Nullable ResourceHandler<ItemResource> getItemHandlerAt(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(Capabilities.Item.BLOCK, pos, side);
    }

    /**
     * Inserts a stack into a handler.
     *
     * @return the remainder that did not fit
     */
    public static ItemStack insertStack(@Nullable ResourceHandler<ItemResource> handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) {
            return stack;
        }
        int inserted;
        try (Transaction transaction = Transaction.openRoot()) {
            inserted = handler.insert(ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
        }
        if (inserted >= stack.getCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = stack.copy();
        remainder.setCount(stack.getCount() - inserted);
        return remainder;
    }

    /**
     * Inserts a stack into the inventory at a position.
     *
     * @return the remainder that did not fit
     */
    public static ItemStack insertStackAt(Level level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        ResourceHandler<ItemResource> inventory = getItemHandlerAt(level, pos, side);
        return inventory != null ? insertStack(inventory, stack, simulate) : stack;
    }

    /**
     * @return a copy of the portion of the stack the inventory can accept
     */
    public static ItemStack hasRoomFor(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ItemStack rejected = insertStackAt(level, pos, side, stack.copy(), true);
        if (rejected.isEmpty()) {
            return stack.copy();
        }
        ItemStack accepted = stack.copy();
        accepted.setCount(stack.getCount() - rejected.getCount());
        return accepted;
    }

    /**
     * @return whether the inventory can accept at least one item of the stack
     */
    public static boolean hasRoomForSome(Level level, BlockPos pos, Direction side, ItemStack stack) {
        ItemStack rejected = insertStackAt(level, pos, side, stack.copy(), true);
        return stack.getCount() == 0 || rejected.getCount() != stack.getCount();
    }

    /**
     * @return whether the inventory can accept the whole stack
     */
    public static boolean hasRoomForAll(Level level, BlockPos pos, Direction side, ItemStack stack) {
        return insertStackAt(level, pos, side, stack.copy(), true).isEmpty();
    }

    /**
     * @return the total count of matching items in the handler
     */
    public static int countTotalItemsIn(@Nullable ResourceHandler<ItemResource> inventory, ItemStack stack, InvFilter filter) {
        int count = 0;
        if (inventory != null) {
            for (int index = 0; index < inventory.size(); index++) {
                ItemResource resource = inventory.getResource(index);
                if (!resource.isEmpty() && areItemStacksEqual(stack, resource.toStack(1), filter)) {
                    count += inventory.getAmountAsInt(index);
                }
            }
        }
        return count;
    }

    /**
     * @return the total count of matching items in the inventory at a position
     */
    public static int countTotalItemsIn(Level level, BlockPos pos, Direction side, ItemStack stack, InvFilter filter) {
        return countTotalItemsIn(getItemHandlerAt(level, pos, side), stack, filter);
    }

    /**
     * Compares two stacks under the filter's flags. Counts are ignored.
     */
    public static boolean areItemStacksEqual(ItemStack first, ItemStack second, InvFilter filter) {
        if (first.isEmpty() || second.isEmpty()) {
            return first.isEmpty() && second.isEmpty();
        }
        if (filter.useMod) {
            return itemId(first).getNamespace().equals(itemId(second).getNamespace());
        }
        if (filter.useTags) {
            return first.getItem() == second.getItem() || shareTag(first, second);
        }
        if (first.getItem() != second.getItem()) {
            return false;
        }
        if (!filter.ignoreDamage && first.getDamageValue() != second.getDamageValue()) {
            return false;
        }
        if (filter.ignoreComponents) {
            return true;
        }
        if (filter.relaxedComponents) {
            return componentsContainedIn(first, second);
        }
        return componentsEqualIgnoringDamage(first, second, filter.ignoreDamage);
    }

    private static Identifier itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static boolean shareTag(ItemStack first, ItemStack second) {
        return first.getItem().builtInRegistryHolder().tags().anyMatch(second::is);
    }

    private static boolean componentsContainedIn(ItemStack first, ItemStack second) {
        for (DataComponentType<?> type : first.getComponents().keySet()) {
            if (!Objects.equals(first.getComponents().get(type), second.getComponents().get(type))) {
                return false;
            }
        }
        return true;
    }

    private static boolean componentsEqualIgnoringDamage(ItemStack first, ItemStack second, boolean ignoreDamage) {
        if (!ignoreDamage) {
            return ItemStack.isSameItemSameComponents(first, second);
        }
        DataComponentMap firstComponents = first.getComponents();
        DataComponentMap secondComponents = second.getComponents();
        for (DataComponentType<?> type : firstComponents.keySet()) {
            if (type != DataComponents.DAMAGE && !Objects.equals(firstComponents.get(type), secondComponents.get(type))) {
                return false;
            }
        }
        for (DataComponentType<?> type : secondComponents.keySet()) {
            if (type != DataComponents.DAMAGE && !firstComponents.keySet().contains(type)) {
                return false;
            }
        }
        return true;
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
     */
    public static ItemStack findFirstMatchFromFilter(List<ItemStack> filterStacks, boolean blacklist, ResourceHandler<ItemResource> inv, InvFilter filter, boolean leaveOne) {
        slots : for (int index = 0; index < inv.size(); index++) {
            ItemResource resource = inv.getResource(index);
            if (resource.isEmpty()) {
                continue;
            }
            ItemStack candidate = resource.toStack(Math.min(inv.getAmountAsInt(index), resource.toStack(1).getMaxStackSize()));
            if (candidate.isEmpty() || (leaveOne && countTotalItemsIn(inv, candidate, filter) < 2)) {
                continue;
            }
            boolean allow = false;
            boolean allEmpty = true;
            for (ItemStack ghost : filterStacks) {
                if (ghost.isEmpty()) {
                    continue;
                }
                allEmpty = false;
                boolean matches = areItemStacksEqual(ghost, candidate, filter);
                if (blacklist) {
                    if (matches) {
                        continue slots;
                    }
                    allow = true;
                } else if (matches) {
                    return candidate;
                }
            }
            if (blacklist && (allow || allEmpty)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * @return whether a stack passes a ghost-stack filter
     */
    public static boolean matchesFilters(List<ItemStack> filterStacks, boolean blacklist, ItemStack stack, InvFilter filter) {
        if (stack.isEmpty()) {
            return false;
        }
        boolean allow = false;
        boolean allEmpty = true;
        for (ItemStack ghost : filterStacks) {
            if (ghost.isEmpty()) {
                continue;
            }
            allEmpty = false;
            boolean matches = areItemStacksEqual(ghost, stack, filter);
            if (blacklist) {
                if (matches) {
                    return false;
                }
                allow = true;
            } else if (matches) {
                return true;
            }
        }
        return blacklist && (allow || allEmpty);
    }

    /**
     * Finds the first candidate stack that passes a ghost-stack filter.
     *
     * @return the matching stack, or an empty stack
     */
    public static ItemStack findFirstMatchFromFilter(List<ItemStack> filterStacks, List<Integer> filterSizes, boolean blacklist, List<ItemStack> candidates, InvFilter filter) {
        return findFirstMatchFromFilterWithSize(filterStacks, filterSizes, blacklist, candidates, filter).stack();
    }

    /**
     * Finds the first candidate stack that passes a ghost-stack filter, with the matching
     * slot's size limit.
     */
    public static FilterMatch findFirstMatchFromFilterWithSize(List<ItemStack> filterStacks, List<Integer> filterSizes, boolean blacklist, List<ItemStack> candidates, InvFilter filter) {
        candidates : for (ItemStack candidate : candidates) {
            if (candidate.isEmpty()) {
                continue;
            }
            boolean allow = false;
            boolean allEmpty = true;
            for (int slot = 0; slot < filterStacks.size(); slot++) {
                ItemStack ghost = filterStacks.get(slot);
                if (ghost.isEmpty()) {
                    continue;
                }
                allEmpty = false;
                boolean matches = areItemStacksEqual(ghost, candidate, filter);
                if (blacklist) {
                    if (matches) {
                        continue candidates;
                    }
                    allow = true;
                } else if (matches) {
                    return new FilterMatch(candidate, filterSizes.get(slot));
                }
            }
            if (blacklist && (allow || allEmpty)) {
                return new FilterMatch(candidate, 0);
            }
        }
        return new FilterMatch(ItemStack.EMPTY, 0);
    }

    /**
     * @return a copy of the stack, capped at the given count
     */
    public static ItemStack copyLimitedStack(ItemStack stack, int limit) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        if (copy.getCount() > limit) {
            copy.setCount(limit);
        }
        return copy;
    }

    /**
     * Removes up to the stack's count of matching items from the inventory at a position.
     *
     * @return the removed items, or an empty stack
     */
    public static ItemStack removeStackFrom(Level level, BlockPos pos, Direction side, ItemStack stack, InvFilter filter, boolean simulate) {
        return removeStackFrom(getItemHandlerAt(level, pos, side), stack, filter, simulate);
    }

    /**
     * Removes up to the stack's count of matching items from a handler.
     *
     * @return the removed items, or an empty stack
     */
    public static ItemStack removeStackFrom(@Nullable ResourceHandler<ItemResource> inventory, ItemStack stack, InvFilter filter, boolean simulate) {
        if (inventory == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int amount = stack.getCount();
        int removed = 0;
        try (Transaction transaction = Transaction.openRoot()) {
            for (int index = 0; index < inventory.size() && removed < amount; index++) {
                ItemResource resource = inventory.getResource(index);
                if (!resource.isEmpty() && areItemStacksEqual(stack, resource.toStack(1), filter)) {
                    removed += inventory.extract(index, resource, amount - removed, transaction);
                }
            }
            if (!simulate) {
                transaction.commit();
            }
        }
        if (removed == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack out = stack.copy();
        out.setCount(removed);
        return out;
    }

    /**
     * Pushes a stack into the inventory beyond the given side, spilling the remainder into
     * the world as an item entity.
     */
    public static void ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out) {
        ejectStackAt(level, pos, side, out, false);
    }

    /**
     * Pushes a stack into the inventory beyond the given side. In smart mode the remainder
     * is returned instead of spilled when an inventory exists there.
     *
     * @return the remainder in smart mode, otherwise an empty stack
     */
    public static ItemStack ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out, boolean smart) {
        ItemStack remainder = insertStackAt(level, pos.relative(side), side.getOpposite(), out, false);
        if (smart && getItemHandlerAt(level, pos.relative(side), side.getOpposite()) != null) {
            return remainder;
        }
        if (remainder.isEmpty()) {
            return ItemStack.EMPTY;
        }
        BlockPos spawnPos = pos;
        BlockPos target = pos.relative(side);
        if (level.getBlockState(target).isCollisionShapeFullBlock(level, target)) {
            spawnPos = pos.relative(side.getOpposite());
        }
        ItemEntity entity = new ItemEntity(level, spawnPos.getX() + 0.5 + side.getStepX(), spawnPos.getY() + side.getStepY(), spawnPos.getZ() + 0.5 + side.getStepZ(), remainder);
        entity.setDeltaMovement(0.3 * side.getStepX(), 0.3 * side.getStepY(), 0.3 * side.getStepZ());
        level.addFreshEntity(entity);
        return ItemStack.EMPTY;
    }

    /**
     * @return the total count of matching dropped items around a position
     */
    public static int countStackInWorld(Level level, BlockPos pos, ItemStack stack, double range, InvFilter filter) {
        int count = 0;
        AABB area = new AABB(pos).inflate(range);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (!item.getItem().isEmpty() && areItemStacksEqual(stack, item.getItem(), filter)) {
                count += item.getItem().getCount();
            }
        }
        return count;
    }

    /**
     * Drops a stack at an entity's chest height. Server side only.
     */
    public static void dropItemAtEntity(Level level, ItemStack stack, Entity entity) {
        if (!level.isClientSide() && !stack.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY() + entity.getEyeHeight() / 2.0F, entity.getZ(), stack.copy()));
        }
    }

    /** Matches by item identity or shared tag, like the legacy ore dictionary base filter. */
    public static final InvFilter BASE_TAGS = new InvFilter(false, false, true, false);

    /**
     * @return whether inventories adjacent to the position, ignoring the top, together hold
     *         the stack's count of matching items
     */
    public static boolean checkAdjacentChests(Level level, BlockPos pos, ItemStack stack) {
        int needed = stack.getCount();
        for (Direction face : Direction.values()) {
            if (face == Direction.UP) {
                continue;
            }
            needed -= countTotalItemsIn(level, pos.relative(face), face.getOpposite(), stack.copy(), BASE_TAGS);
            if (needed <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the stack's count of matching items from inventories adjacent to the position.
     *
     * @return whether the full amount was consumed
     */
    public static boolean consumeFromAdjacentChests(Level level, BlockPos pos, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (Direction face : Direction.values()) {
            if (face == Direction.UP || remaining.isEmpty()) {
                continue;
            }
            ItemStack removed = removeStackFrom(level, pos.relative(face), face.getOpposite(), remaining, BASE_TAGS, false);
            remaining.setCount(remaining.getCount() - removed.getCount());
        }
        return remaining.isEmpty();
    }

    /**
     * @return whether the player's main inventory holds the stack's count of matching items
     */
    public static boolean isPlayerCarryingAmount(Player player, ItemStack stack, boolean useTags) {
        if (stack.isEmpty()) {
            return false;
        }
        int needed = stack.getCount();
        InvFilter filter = new InvFilter(false, stack.isComponentsPatchEmpty(), useTags, false).setRelaxedComponents();
        for (ItemStack held : player.getInventory().getNonEquipmentItems()) {
            if (areItemStacksEqual(held, stack, filter)) {
                needed -= held.getCount();
                if (needed <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes the stack's count of matching items from the player's main inventory.
     *
     * @param skipCheck whether to skip the carrying pre-check
     * @return whether the full amount was consumed
     */
    public static boolean consumePlayerItem(Player player, ItemStack stack, boolean skipCheck, boolean useTags) {
        if (!skipCheck && !isPlayerCarryingAmount(player, stack, useTags)) {
            return false;
        }
        int remaining = stack.getCount();
        InvFilter filter = new InvFilter(false, stack.isComponentsPatchEmpty(), useTags, false).setRelaxedComponents();
        List<ItemStack> items = player.getInventory().getNonEquipmentItems();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack held = items.get(slot);
            if (!areItemStacksEqual(held, stack, filter)) {
                continue;
            }
            if (held.getCount() > remaining) {
                held.shrink(remaining);
                remaining = 0;
            } else {
                remaining -= held.getCount();
                items.set(slot, ItemStack.EMPTY);
            }
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks or consumes the given stacks from adjacent inventories first, then the player.
     *
     * @param simulate whether to only check availability
     * @return whether every stack is available or was consumed
     */
    public static boolean consumeItemsFromAdjacentInventoryOrPlayer(Level level, BlockPos pos, Player player, boolean simulate, ItemStack... items) {
        for (ItemStack stack : items) {
            if (!checkAdjacentChests(level, pos, stack) && !isPlayerCarryingAmount(player, stack, true)) {
                return false;
            }
        }
        if (!simulate) {
            for (ItemStack stack : items) {
                if (!consumeFromAdjacentChests(level, pos, stack.copy())) {
                    consumePlayerItem(player, stack, true, true);
                }
            }
        }
        return true;
    }
}
