package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuSpa extends AbstractContainerMenu {
    public static final int SALTS_X = 65;
    public static final int SALTS_Y = 31;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;
    public static final int HOTBAR_Y = 142;
    public static final int MIX_BUTTON_ID = 1;

    public static final int SLOT_COUNT = 1;
    public static final int PLAYER_ROW_SLOTS = 9;
    public static final int PLAYER_ROWS = 3;
    public static final int TOTAL_INVENTORY_SLOTS = SLOT_COUNT + PLAYER_ROW_SLOTS * (PLAYER_ROWS + 1);

    private final ItemStacksResourceHandler items;
    private final ContainerLevelAccess access;

    public MenuSpa(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new ItemStacksResourceHandler(SLOT_COUNT), ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()));
    }

    public MenuSpa(int containerId, Inventory playerInventory, BlockEntitySpa blockEntity) {
        this(containerId, playerInventory, blockEntity.getItems(), ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    private MenuSpa(int containerId, Inventory playerInventory, ItemStacksResourceHandler items, ContainerLevelAccess access) {
        super(TCMenus.SPA.get(), containerId);
        this.items = items;
        this.access = access;

        addSlot(new ResourceHandlerSlot(items, items::set, 0, SALTS_X, SALTS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(TCItems.BATH_SALTS.get());
            }
        });

        for (int row = 0; row < PLAYER_ROWS; row++) {
            for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
                addSlot(new Slot(playerInventory, col + row * PLAYER_ROW_SLOTS + PLAYER_ROW_SLOTS, PLAYER_GRID_X + col * 18, PLAYER_GRID_Y + row * 18));
            }
        }
        for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_GRID_X + col * 18, HOTBAR_Y));
        }
    }

    public @Nullable BlockEntitySpa blockEntity() {
        return (BlockEntitySpa) access.evaluate(Level::getBlockEntity).filter(be -> be instanceof BlockEntitySpa).orElse(null);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == MIX_BUTTON_ID) {
            BlockEntitySpa spa = blockEntity();
            if (spa != null) {
                spa.toggleMix();
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TCBlocks.SPA.get());
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
            } else if (!stackInSlot.is(TCItems.BATH_SALTS.get()) || !moveItemStackTo(stackInSlot, 0, SLOT_COUNT, false)) {
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
