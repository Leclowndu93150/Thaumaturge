package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuFocalManipulator extends AbstractContainerMenu {
    public static final int BUTTON_START_CRAFT = 0;
    private static final int FOCUS_SLOT_X = 31;
    private static final int FOCUS_SLOT_Y = 191;
    private static final int PLAYER_COLUMN_X = -62;
    private static final int PLAYER_MAIN_Y = 64;
    private static final int HOTBAR_GRID_Y = 7;
    private static final int TABLE_SLOT_COUNT = 1;
    private static final int TOTAL_SLOTS = 37;

    private final ItemStacksResourceHandler items;
    private final ContainerLevelAccess access;
    private final BlockPos pos;
    private final @Nullable BlockEntityFocalManipulator table;

    public MenuFocalManipulator(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new ItemStacksResourceHandler(1), ContainerLevelAccess.NULL, buf.readBlockPos(), null);
    }

    public MenuFocalManipulator(int containerId, Inventory playerInventory, BlockEntityFocalManipulator table) {
        this(containerId, playerInventory, table.items(), ContainerLevelAccess.create(table.getLevel(), table.getBlockPos()), table.getBlockPos(), table);
    }

    private MenuFocalManipulator(int containerId, Inventory playerInventory, ItemStacksResourceHandler items, ContainerLevelAccess access, BlockPos pos, @Nullable BlockEntityFocalManipulator table) {
        super(TCMenus.FOCAL_MANIPULATOR.get(), containerId);
        this.items = items;
        this.access = access;
        this.pos = pos;
        this.table = table;

        addSlot(new ResourceHandlerSlot(items, items::set, BlockEntityFocalManipulator.SLOT_FOCUS, FOCUS_SLOT_X, FOCUS_SLOT_Y));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, i * 18 + PLAYER_COLUMN_X, PLAYER_MAIN_Y + j * 18));
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlot(new Slot(playerInventory, i + j * 3, i * 18 + PLAYER_COLUMN_X, j * 18 + HOTBAR_GRID_Y));
            }
        }
    }

    public BlockPos pos() {
        return pos;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_START_CRAFT && table != null && !table.startCraft(player)) {
            access.execute((level, blockPos) -> level.playSound(null, blockPos, TCSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F));
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TCBlocks.FOCAL_MANIPULATOR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            returnStack = stackInSlot.copy();
            if (slotIndex != 0) {
                if (stackInSlot.getItem() instanceof ItemFocus) {
                    if (!moveItemStackTo(stackInSlot, 0, TABLE_SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex < 28) {
                    if (!moveItemStackTo(stackInSlot, 28, TOTAL_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stackInSlot, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 1, TOTAL_SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stackInSlot.getCount() == returnStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stackInSlot);
        }
        return returnStack;
    }
}
