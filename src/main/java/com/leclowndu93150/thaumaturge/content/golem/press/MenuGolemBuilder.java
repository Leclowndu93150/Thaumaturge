package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.content.golem.ItemGolemPlacer;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuGolemBuilder extends AbstractContainerMenu {
    public static final int OUTPUT_X = 160;
    public static final int OUTPUT_Y = 104;
    public static final int PLAYER_GRID_X = 24;
    public static final int PLAYER_GRID_Y = 142;
    public static final int HOTBAR_Y = 200;

    public static final int SLOT_COUNT = 1;
    public static final int PLAYER_ROW_SLOTS = 9;
    public static final int PLAYER_ROWS = 3;
    public static final int TOTAL_INVENTORY_SLOTS = SLOT_COUNT + PLAYER_ROW_SLOTS * (PLAYER_ROWS + 1);

    private final ContainerLevelAccess access;
    private final DataSlot cost = DataSlot.standalone();
    private final DataSlot maxCost = DataSlot.standalone();

    public MenuGolemBuilder(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new ItemStacksResourceHandler(SLOT_COUNT), ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()));
    }

    public MenuGolemBuilder(int containerId, Inventory playerInventory, BlockEntityGolemBuilder blockEntity) {
        this(containerId, playerInventory, blockEntity.output(), ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    private MenuGolemBuilder(int containerId, Inventory playerInventory, ItemStacksResourceHandler items, ContainerLevelAccess access) {
        super(TCMenus.GOLEM_BUILDER.get(), containerId);
        this.access = access;

        addSlot(new ResourceHandlerSlot(items, items::set, 0, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemGolemPlacer;
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

        addDataSlot(cost);
        addDataSlot(maxCost);
    }

    @Override
    public void broadcastChanges() {
        BlockEntityGolemBuilder builder = blockEntity();
        if (builder != null) {
            cost.set(builder.cost());
            maxCost.set(builder.maxCost());
        }
        super.broadcastChanges();
    }

    public int cost() {
        return cost.get();
    }

    public int maxCost() {
        return maxCost.get();
    }

    public @Nullable BlockEntityGolemBuilder blockEntity() {
        return (BlockEntityGolemBuilder) access.evaluate(Level::getBlockEntity).filter(be -> be instanceof BlockEntityGolemBuilder).orElse(null);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TCBlocks.GOLEM_BUILDER.get());
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
            } else if (!(stackInSlot.getItem() instanceof ItemGolemPlacer) || !moveItemStackTo(stackInSlot, 0, SLOT_COUNT, false)) {
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
