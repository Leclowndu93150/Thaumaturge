package com.leclowndu93150.thaumaturge.content.equipment.hover;

import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MenuThaumostaticHarness extends AbstractContainerMenu {
    private static final int JAR_SLOT = 0;
    private static final int PLAYER_SLOTS_START = 1;
    private static final int JAR_SLOT_X = 79;
    private static final int JAR_SLOT_Y = 31;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int SLOT_SIZE = 18;

    private final InteractionHand hand;
    private final Player player;
    private final SimpleContainer jarContainer = new SimpleContainer(1);
    public final int blockedHotbarSlot;

    public MenuThaumostaticHarness(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public MenuThaumostaticHarness(int containerId, Inventory inventory, InteractionHand hand) {
        super(TCMenus.THAUMOSTATIC_HARNESS.get(), containerId);
        this.hand = hand;
        this.player = inventory.player;
        this.blockedHotbarSlot = hand == InteractionHand.MAIN_HAND ? inventory.getSelectedSlot() : -1;
        jarContainer.setItem(JAR_SLOT, ThaumostaticHarnessItem.getJar(harness()));
        addSlot(new JarSlot(jarContainer, JAR_SLOT, JAR_SLOT_X, JAR_SLOT_Y));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, PLAYER_INV_X + column * SLOT_SIZE, PLAYER_INV_Y + row * SLOT_SIZE));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PLAYER_INV_X + column * SLOT_SIZE, HOTBAR_Y));
        }
    }

    private ItemStack harness() {
        return player.getItemInHand(hand);
    }

    private void saveToStack() {
        ItemStack harness = harness();
        if (harness.getItem() instanceof ThaumostaticHarnessItem) {
            ThaumostaticHarnessItem.setJar(harness, jarContainer.getItem(JAR_SLOT));
        }
    }

    private boolean isBlocked(int slotId, Player clickPlayer) {
        return blockedHotbarSlot >= 0 && slotId >= 0 && slotId < slots.size() && slots.get(slotId).container == clickPlayer.getInventory() && slots.get(slotId).getContainerSlot() == blockedHotbarSlot;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput containerInput, Player clickPlayer) {
        if (containerInput == ContainerInput.SWAP || isBlocked(slotId, clickPlayer)) {
            return;
        }
        super.clicked(slotId, button, containerInput, clickPlayer);
        saveToStack();
    }

    @Override
    public void removed(Player removedPlayer) {
        super.removed(removedPlayer);
        saveToStack();
    }

    @Override
    public ItemStack quickMoveStack(Player quickMovePlayer, int index) {
        if (isBlocked(index, quickMovePlayer)) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack moved = slot.getItem();
        ItemStack original = moved.copy();
        if (index == JAR_SLOT) {
            if (!moveItemStackTo(moved, PLAYER_SLOTS_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!ThaumostaticHarnessItem.isFuelJar(moved) || !moveItemStackTo(moved, JAR_SLOT, PLAYER_SLOTS_START, false)) {
            return ItemStack.EMPTY;
        }
        if (moved.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        saveToStack();
        return original;
    }

    @Override
    public boolean stillValid(Player checkPlayer) {
        return checkPlayer.getItemInHand(hand).getItem() instanceof ThaumostaticHarnessItem;
    }

    private static final class JarSlot extends Slot {
        JarSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ThaumostaticHarnessItem.isFuelJar(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
