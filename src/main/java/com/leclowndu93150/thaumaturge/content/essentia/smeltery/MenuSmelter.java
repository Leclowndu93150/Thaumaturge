package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MenuSmelter extends AbstractContainerMenu {
    public static final int ITEM_X = 80;
    public static final int ITEM_Y = 7;
    public static final int FUEL_X = 80;
    public static final int FUEL_Y = 47;

    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;

    public static final int SLOT_COUNT = 2;
    public static final int PLAYER_ROW_SLOTS = 9;
    public static final int PLAYER_ROWS = 3;
    public static final int PLAYER_TOTAL_SLOTS = PLAYER_ROW_SLOTS * (PLAYER_ROWS + 1);
    public static final int TOTAL_INVENTORY_SLOTS = SLOT_COUNT + PLAYER_TOTAL_SLOTS;

    private final ItemStackHandler items;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public MenuSmelter(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos());
    }

    private MenuSmelter(int containerId, Inventory playerInventory, BlockPos pos) {
        this(
                containerId,
                playerInventory,
                new ItemStackHandler(SLOT_COUNT),
                ContainerLevelAccess.create(playerInventory.player.level(), pos),
                pos);
    }

    public MenuSmelter(int containerId, Inventory playerInventory, BlockEntitySmelter blockEntity) {
        this(
                containerId,
                playerInventory,
                blockEntity.getInventory(),
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                blockEntity.getBlockPos());
    }

    private MenuSmelter(
            int containerId,
            Inventory playerInventory,
            ItemStackHandler items,
            ContainerLevelAccess access,
            BlockPos pos) {
        super(TCMenus.SMELTER.get(), containerId);
        this.items = items;
        this.access = access;
        this.pos = pos;

        addSlot(new SlotItemHandler(items, 0, ITEM_X, ITEM_Y));
        addSlot(new SlotItemHandler(items, 1, FUEL_X, FUEL_Y));

        for (int row = 0; row < PLAYER_ROWS; row++) {
            for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
                addSlot(new Slot(
                        playerInventory,
                        col + row * PLAYER_ROW_SLOTS + PLAYER_ROW_SLOTS,
                        PLAYER_GRID_X + col * 18,
                        PLAYER_GRID_Y + row * 18));
            }
        }

        for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_GRID_X + col * 18, PLAYER_GRID_Y + 3 * 18 + 4));
        }
    }

    public BlockPos pos() {
        return pos;
    }

    public ItemStackHandler items() {
        return items;
    }

    public BlockEntitySmelter blockEntity() {
        return (BlockEntitySmelter) access.evaluate(Level::getBlockEntity)
                .filter(be -> be instanceof BlockEntitySmelter)
                .orElse(null);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TCBlocks.SMELTER_BASIC.get())
                || AbstractContainerMenu.stillValid(access, player, TCBlocks.SMELTER_THAUMIUM.get())
                || AbstractContainerMenu.stillValid(access, player, TCBlocks.SMELTER_VOID.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            returnStack = stackInSlot.copy();
            if (slotIndex < SLOT_COUNT) {
                if (!moveItemStackTo(stackInSlot, SLOT_COUNT, TOTAL_INVENTORY_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return returnStack;
    }
}
