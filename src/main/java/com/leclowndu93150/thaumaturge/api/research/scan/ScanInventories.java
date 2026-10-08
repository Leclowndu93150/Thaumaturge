package com.leclowndu93150.thaumaturge.api.research.scan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

final class ScanInventories {
    private ScanInventories() {}

    static boolean hasInventory(Player player, ScanTarget target) {
        return !handlers(player, target).isEmpty() || container(player, target) != null;
    }

    static List<ItemStack> contents(Player player, ScanTarget target) {
        Set<ItemStack> stacks = ItemStackLinkedSet.createTypeAndComponentsSet();
        List<IItemHandler> handlers = handlers(player, target);
        for (IItemHandler handler : handlers) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    stacks.add(stack.copy());
                }
            }
        }
        if (handlers.isEmpty() && container(player, target) instanceof Container container) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (!stack.isEmpty()) {
                    stacks.add(stack.copy());
                }
            }
        }
        return List.copyOf(stacks);
    }

    private static List<IItemHandler> handlers(Player player, ScanTarget target) {
        Set<IItemHandler> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<IItemHandler> handlers = new ArrayList<>();
        addHandler(player, target, null, seen, handlers);
        for (Direction side : Direction.values()) {
            addHandler(player, target, side, seen, handlers);
        }
        return handlers;
    }

    private static void addHandler(
            Player player,
            ScanTarget target,
            @Nullable Direction side,
            Set<IItemHandler> seen,
            List<IItemHandler> handlers) {
        IItemHandler handler =
                switch (target) {
                    case ScannedBlock(var pos) ->
                        player.level().hasChunkAt(pos)
                                ? player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, side)
                                : null;
                    case ScannedEntity(var entity) ->
                        side == null ? entity.getCapability(Capabilities.ItemHandler.ENTITY) : null;
                    default -> null;
                };
        if (handler != null && seen.add(handler)) {
            handlers.add(handler);
        }
    }

    private static @Nullable Container container(Player player, ScanTarget target) {
        Object candidate =
                switch (target) {
                    case ScannedBlock(var pos) ->
                        player.level().hasChunkAt(pos) ? player.level().getBlockEntity(pos) : null;
                    case ScannedEntity(var entity) -> entity;
                    default -> null;
                };
        return candidate instanceof Container container ? container : null;
    }
}
