package com.leclowndu93150.thaumaturge.content.spell.manipulator;

import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Optional;
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
    public static final int BUTTON_INSCRIBE = 0;
    public static final int FOCUS_SLOT_X = 91;
    public static final int FOCUS_SLOT_Y = 19;
    public static final int CABINET_X = 13;
    public static final int HOTBAR_Y = 12;
    public static final int MAIN_Y = 69;
    public static final int SLOT_PITCH = 18;
    private static final int CABINET_COLUMNS = 3;
    private static final int MAIN_ROWS = 9;
    private static final int HOTBAR_SIZE = 9;
    private static final int PLAYER_SLOTS = 36;
    private static final int FOCUS_INDEX = 0;
    private static final int FIRST_PLAYER = 1;
    private static final int FIRST_HOTBAR = FIRST_PLAYER + PLAYER_SLOTS - HOTBAR_SIZE;
    private static final float FAIL_VOLUME = 0.33F;

    private final ContainerLevelAccess access;
    private final BlockPos pos;
    private final @Nullable BlockEntityFocalManipulator table;

    public MenuFocalManipulator(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, new ItemStacksResourceHandler(FIRST_PLAYER), ContainerLevelAccess.NULL, buf.readBlockPos(), null);
    }

    public MenuFocalManipulator(int containerId, Inventory inventory, BlockEntityFocalManipulator table) {
        this(containerId, inventory, table.items(), ContainerLevelAccess.create(table.getLevel(), table.getBlockPos()), table.getBlockPos(), table);
    }

    private MenuFocalManipulator(int containerId, Inventory inventory, ItemStacksResourceHandler items, ContainerLevelAccess access, BlockPos pos, @Nullable BlockEntityFocalManipulator table) {
        super(TTMenus.FOCAL_MANIPULATOR.get(), containerId);
        this.access = access;
        this.pos = pos;
        this.table = table;
        addSlot(new ResourceHandlerSlot(items, items::set, BlockEntityFocalManipulator.SLOT_FOCUS, FOCUS_SLOT_X, FOCUS_SLOT_Y));
        for (int index = HOTBAR_SIZE; index < PLAYER_SLOTS; index++) {
            addSlot(cabinetSlot(inventory, index));
        }
        for (int index = 0; index < HOTBAR_SIZE; index++) {
            addSlot(cabinetSlot(inventory, index));
        }
    }

    private static Slot cabinetSlot(Inventory inventory, int index) {
        boolean hotbar = index < HOTBAR_SIZE;
        int cell = hotbar ? index : index - HOTBAR_SIZE;
        int column = hotbar ? cell % CABINET_COLUMNS : cell / MAIN_ROWS;
        int row = hotbar ? cell / CABINET_COLUMNS : cell % MAIN_ROWS;
        return new Slot(inventory, index, CABINET_X + column * SLOT_PITCH, (hotbar ? HOTBAR_Y : MAIN_Y) + row * SLOT_PITCH);
    }

    public BlockPos pos() {
        return pos;
    }

    public Optional<BlockEntityFocalManipulator> table() {
        return Optional.ofNullable(table);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_INSCRIBE && table != null && !table.startInscribing(player)) {
            access.execute((level, at) -> level.playSound(null, at, TTSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, FAIL_VOLUME, 1.0F));
        }
        return id == BUTTON_INSCRIBE;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.FOCAL_MANIPULATOR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack before = stack.copy();
        if (!route(index, stack)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return before;
    }

    private boolean route(int index, ItemStack stack) {
        if (index == FOCUS_INDEX) {
            return moveItemStackTo(stack, FIRST_PLAYER, slots.size(), false);
        }
        if (FocusItems.isFocus(stack) && moveItemStackTo(stack, FOCUS_INDEX, FIRST_PLAYER, false)) {
            return true;
        }
        return index < FIRST_HOTBAR ? moveItemStackTo(stack, FIRST_HOTBAR, slots.size(), false) : moveItemStackTo(stack, FIRST_PLAYER, FIRST_HOTBAR, false);
    }
}
