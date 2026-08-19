package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MenuPotionSprayer extends AbstractContainerMenu {
    public static final int POTION_SLOT_X = 56;
    public static final int POTION_SLOT_Y = 64;
    public static final int INVENTORY_X = 16;
    public static final int INVENTORY_Y = 151;
    public static final int HOTBAR_Y = 209;
    private static final int SLOT_STRIDE = 18;
    private static final int PLAYER_ROWS = 3;
    private static final int PLAYER_COLUMNS = 9;

    private final Container container;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public MenuPotionSprayer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(1), ContainerLevelAccess.NULL, buf.readBlockPos());
    }

    public MenuPotionSprayer(int containerId, Inventory playerInventory, BlockEntityPotionSprayer sprayer) {
        this(containerId, playerInventory, new SprayerContainer(sprayer), ContainerLevelAccess.create(sprayer.getLevel(), sprayer.getBlockPos()), sprayer.getBlockPos());
    }

    private MenuPotionSprayer(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access, BlockPos pos) {
        super(TCMenus.POTION_SPRAYER.get(), containerId);
        this.container = container;
        this.access = access;
        this.pos = pos;
        addSlot(new Slot(container, 0, POTION_SLOT_X, POTION_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BlockEntityPotionSprayer.isValidPotion(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int row = 0; row < PLAYER_ROWS; row++) {
            for (int column = 0; column < PLAYER_COLUMNS; column++) {
                addSlot(new Slot(playerInventory, column + row * PLAYER_COLUMNS + PLAYER_COLUMNS, INVENTORY_X + column * SLOT_STRIDE, INVENTORY_Y + row * SLOT_STRIDE));
            }
        }
        for (int column = 0; column < PLAYER_COLUMNS; column++) {
            addSlot(new Slot(playerInventory, column, INVENTORY_X + column * SLOT_STRIDE, HOTBAR_Y));
        }
    }

    public BlockPos sprayerPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TCBlocks.POTION_SPRAYER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!BlockEntityPotionSprayer.isValidPotion(stack) || !moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private static final class SprayerContainer extends SimpleContainer {
        private final BlockEntityPotionSprayer sprayer;

        SprayerContainer(BlockEntityPotionSprayer sprayer) {
            super(1);
            this.sprayer = sprayer;
            setItem(0, sprayer.getPotion().copy());
        }

        @Override
        public void setChanged() {
            super.setChanged();
            sprayer.setPotion(getItem(0).copy());
        }
    }
}
