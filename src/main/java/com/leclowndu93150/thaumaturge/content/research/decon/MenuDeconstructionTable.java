package com.leclowndu93150.thaumaturge.content.research.decon;

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
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuDeconstructionTable extends AbstractContainerMenu {
    public static final int INPUT_X = 63;
    public static final int INPUT_Y = 15;
    private static final int PLAYER_GRID_X = 8;
    private static final int PLAYER_GRID_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int PLAYER_ROW_SLOTS = 9;
    private static final int PLAYER_ROWS = 3;
    private static final int TABLE_SLOTS = 1;
    private static final int TOTAL_SLOTS = TABLE_SLOTS + PLAYER_ROW_SLOTS * (PLAYER_ROWS + 1);

    private final @Nullable BlockEntityDeconstructionTable blockEntity;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public MenuDeconstructionTable(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, clientBlockEntity(playerInventory, buf.readBlockPos()));
    }

    private static @Nullable BlockEntityDeconstructionTable clientBlockEntity(Inventory playerInventory, BlockPos pos) {
        return playerInventory.player.level().getBlockEntity(pos) instanceof BlockEntityDeconstructionTable table ? table : null;
    }

    public MenuDeconstructionTable(int containerId, Inventory playerInventory, @Nullable BlockEntityDeconstructionTable blockEntity) {
        super(TCMenus.DECONSTRUCTION_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = blockEntity != null ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()) : ContainerLevelAccess.NULL;
        this.pos = blockEntity != null ? blockEntity.getBlockPos() : BlockPos.ZERO;
        ItemStacksResourceHandler items = blockEntity != null ? blockEntity.items() : new ItemStacksResourceHandler(TABLE_SLOTS);

        addSlot(new ResourceHandlerSlot(items, items::set, BlockEntityDeconstructionTable.SLOT_INPUT, INPUT_X, INPUT_Y));

        for (int row = 0; row < PLAYER_ROWS; row++) {
            for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
                addSlot(new Slot(playerInventory, col + row * PLAYER_ROW_SLOTS + PLAYER_ROW_SLOTS, PLAYER_GRID_X + col * 18, PLAYER_GRID_Y + row * 18));
            }
        }
        for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_GRID_X + col * 18, HOTBAR_Y));
        }
    }

    public @Nullable BlockEntityDeconstructionTable blockEntity() {
        return blockEntity;
    }

    public BlockPos pos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TCBlocks.DECONSTRUCTION_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            returnStack = stackInSlot.copy();
            if (slotIndex < TABLE_SLOTS) {
                if (!moveItemStackTo(stackInSlot, TABLE_SLOTS, TOTAL_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, TABLE_SLOTS, false)) {
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
