package com.leclowndu93150.thaumaturge.api.items;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvHelperConsumptionCutoffTest {
    private Level level;
    private Player player;
    private Inventory inventory;
    private ItemStackHandler chest;

    @BeforeEach
    void setUp() {
        level = mock(Level.class);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        player = mock(Player.class);
        inventory = new Inventory(player);
        when(player.getInventory()).thenReturn(inventory);
        chest = new ItemStackHandler(1);
        when(level.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.ZERO.below(), Direction.UP))
                .thenReturn(chest);
    }

    @Test
    void partialAdjacentSupplyConsumesOnlyThePlayersShortfall() {
        chest.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 1));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 4));
        ItemStack wanted = new ItemStack(Items.IRON_INGOT, 4);
        assertTrue(InvHelper.takeFromNeighboursOrPlayer(level, BlockPos.ZERO, player, false, wanted));
        assertTrue(chest.getStackInSlot(0).isEmpty());
        assertEquals(1, inventory.getItem(0).getCount());
        assertEquals(4, wanted.getCount());
    }

    @Test
    void completeAdjacentSupplyLeavesThePlayerUntouchedAndSimulationMutatesNeither() {
        chest.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 4));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 7));
        ItemStack wanted = new ItemStack(Items.IRON_INGOT, 4);
        assertTrue(InvHelper.takeFromNeighboursOrPlayer(level, BlockPos.ZERO, player, true, wanted));
        assertEquals(4, chest.getStackInSlot(0).getCount());
        assertEquals(7, inventory.getItem(0).getCount());
        assertTrue(InvHelper.takeFromNeighboursOrPlayer(level, BlockPos.ZERO, player, false, wanted));
        assertTrue(chest.getStackInSlot(0).isEmpty());
        assertEquals(7, inventory.getItem(0).getCount());
    }

    @Test
    void nativePreflightKeepsCutoffRequirementForOneFullSourceAndRejectsWithoutMutation() {
        chest.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 2));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 2));
        ItemStack wanted = new ItemStack(Items.IRON_INGOT, 4);
        assertFalse(InvHelper.takeFromNeighboursOrPlayer(level, BlockPos.ZERO, player, false, wanted));
        assertEquals(2, chest.getStackInSlot(0).getCount());
        assertEquals(2, inventory.getItem(0).getCount());
    }

    @Test
    void refusingProviderIsNotReportedAsSuccessfulConsumption() {
        IItemHandler refusing = mock(IItemHandler.class);
        when(refusing.getSlots()).thenReturn(1);
        when(refusing.getStackInSlot(0)).thenReturn(new ItemStack(Items.IRON_INGOT, 4));
        when(refusing.extractItem(anyInt(), anyInt(), anyBoolean())).thenReturn(ItemStack.EMPTY);
        when(level.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.ZERO.below(), Direction.UP))
                .thenReturn(refusing);
        assertFalse(InvHelper.takeFromNeighboursOrPlayer(
                level, BlockPos.ZERO, player, false, new ItemStack(Items.IRON_INGOT, 4)));
        assertTrue(inventory.getItem(0).isEmpty());
    }

    @Test
    void competingIngredientsFailWithoutMutatingEitherSource() {
        chest.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 5));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 2));

        assertFalse(InvHelper.takeFromNeighboursOrPlayer(
                level,
                BlockPos.ZERO,
                player,
                false,
                new ItemStack(Items.IRON_INGOT, 4),
                new ItemStack(Items.IRON_INGOT, 4)));

        assertEquals(5, chest.getStackInSlot(0).getCount());
        assertEquals(2, inventory.getItem(0).getCount());
    }

    @Test
    void unexpectedShortExtractionRollsBackNeighborAndPlayerTogether() {
        ItemStackHandler shortExtracting = new ItemStackHandler(1) {
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return super.extractItem(slot, Math.max(0, amount - 1), simulate);
            }
        };
        shortExtracting.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 3));
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 4));
        when(level.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.ZERO.below(), Direction.UP))
                .thenReturn(shortExtracting);

        assertFalse(InvHelper.takeFromNeighboursOrPlayer(
                level, BlockPos.ZERO, player, false, new ItemStack(Items.IRON_INGOT, 4)));

        assertEquals(3, shortExtracting.getStackInSlot(0).getCount());
        assertEquals(4, inventory.getItem(0).getCount());
    }
}
