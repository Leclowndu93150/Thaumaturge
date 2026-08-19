package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuVoidSiphon extends AbstractContainerMenu {
    public static final int OUTPUT_X = 80;
    public static final int OUTPUT_Y = 32;
    private static final int PLAYER_GRID_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int PLAYER_ROW_SLOTS = 9;
    private static final int PLAYER_ROWS = 3;

    private final @Nullable BlockEntityVoidSiphon blockEntity;
    private final DataSlot progress = DataSlot.standalone();

    public MenuVoidSiphon(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, (BlockEntityVoidSiphon) null);
        buf.readBlockPos();
    }

    public MenuVoidSiphon(int containerId, Inventory playerInventory, @Nullable BlockEntityVoidSiphon blockEntity) {
        super(TCMenus.VOID_SIPHON.get(), containerId);
        this.blockEntity = blockEntity;
        ItemStacksResourceHandler items = blockEntity != null ? blockEntity.output() : new ItemStacksResourceHandler(1);

        addSlot(new ResourceHandlerSlot(items, items::set, 0, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < PLAYER_ROWS; row++) {
            for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
                addSlot(new Slot(playerInventory, col + row * PLAYER_ROW_SLOTS + PLAYER_ROW_SLOTS, 8 + col * 18, PLAYER_GRID_Y + row * 18));
            }
        }
        for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
        addDataSlot(progress);
    }

    @Override
    public void broadcastChanges() {
        if (blockEntity != null) {
            progress.set(blockEntity.progress());
        }
        super.broadcastChanges();
    }

    public int progress() {
        return progress.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.getBlockPos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) <= 64.0;
    }
}
