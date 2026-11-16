package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jspecify.annotations.Nullable;

public final class MenuSmelter extends AbstractTTMenu {
    public static final int ITEM_X = 80;
    public static final int ITEM_Y = 7;
    public static final int FUEL_X = 80;
    public static final int FUEL_Y = 47;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;
    public static final int SLOT_COUNT = BlockEntitySmelter.SLOT_COUNT;

    private static final int HOTBAR_GAP = 4;
    private static final int PLAYER_GRID_HEIGHT = 54;
    private static final int HOTBAR_Y = PLAYER_GRID_Y + PLAYER_GRID_HEIGHT + HOTBAR_GAP;
    private static final double INTERACTION_BUFFER = 4.0;

    private final Player player;
    private final BlockPos pos;
    private final ItemStackHandler items;

    public MenuSmelter(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos(), new ItemStackHandler(SLOT_COUNT));
    }

    public MenuSmelter(int containerId, Inventory playerInventory, BlockEntitySmelter blockEntity) {
        this(containerId, playerInventory, blockEntity.getBlockPos(), blockEntity.itemSlots());
    }

    private MenuSmelter(int containerId, Inventory playerInventory, BlockPos pos, ItemStackHandler items) {
        super(TTMenus.SMELTER.get(), containerId);
        this.player = playerInventory.player;
        this.pos = pos;
        this.items = items;
        addSlot(new InputSlot(items, BlockEntitySmelter.INPUT_SLOT, ITEM_X, ITEM_Y));
        addSlot(new FuelSlot(items, BlockEntitySmelter.FUEL_SLOT, FUEL_X, FUEL_Y, player.level()));
        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
    }

    public BlockPos pos() {
        return pos;
    }

    public ItemStackHandler items() {
        return items;
    }

    public @Nullable BlockEntitySmelter blockEntity() {
        return player.level().getBlockEntity(pos) instanceof BlockEntitySmelter smelter ? smelter : null;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(pos).getBlock() instanceof BlockSmelter
                && player.canInteractWithBlock(pos, INTERACTION_BUFFER);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Level level = player.level();
        if (index >= SLOT_COUNT) {
            Slot source = slots.get(index);
            ItemStack stack = source.getItem();
            if (!stack.isEmpty() && BlockEntitySmelter.isFuelValid(level, stack)) {
                ItemStack original = stack.copy();
                if (moveItemStackTo(stack, BlockEntitySmelter.FUEL_SLOT, BlockEntitySmelter.FUEL_SLOT + 1, false)) {
                    if (stack.isEmpty()) {
                        source.setByPlayer(ItemStack.EMPTY);
                    } else {
                        source.setChanged();
                    }
                    return original;
                }
            }
        }
        return quickMoveBetween(
                index,
                SLOT_COUNT,
                stack -> BlockEntitySmelter.isInputValid(stack) || BlockEntitySmelter.isFuelValid(level, stack));
    }

    private static final class InputSlot extends SlotItemHandler {
        InputSlot(ItemStackHandler items, int index, int x, int y) {
            super(items, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return BlockEntitySmelter.isInputValid(stack);
        }
    }

    private static final class FuelSlot extends SlotItemHandler {
        private final Level level;

        FuelSlot(ItemStackHandler items, int index, int x, int y, Level level) {
            super(items, index, x, y);
            this.level = level;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return BlockEntitySmelter.isFuelValid(level, stack);
        }
    }
}
