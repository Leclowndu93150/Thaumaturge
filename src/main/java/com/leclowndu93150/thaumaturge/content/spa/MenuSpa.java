package com.leclowndu93150.thaumaturge.content.spa;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jspecify.annotations.Nullable;

public final class MenuSpa extends AbstractTTMenu {
    public static final int SALTS_X = 65;
    public static final int SALTS_Y = 31;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;
    public static final int HOTBAR_Y = 142;
    public static final int MIX_BUTTON_ID = 1;

    public static final int SLOT_COUNT = 1;

    private final ItemStackHandler items;
    private final ContainerLevelAccess access;

    public MenuSpa(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(
                containerId,
                playerInventory,
                new ItemStackHandler(SLOT_COUNT),
                ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()));
    }

    public MenuSpa(int containerId, Inventory playerInventory, BlockEntitySpa blockEntity) {
        this(
                containerId,
                playerInventory,
                blockEntity.getItems(),
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    private MenuSpa(int containerId, Inventory playerInventory, ItemStackHandler items, ContainerLevelAccess access) {
        super(TTMenus.SPA.get(), containerId);
        this.items = items;
        this.access = access;

        addSlot(new SlotItemHandler(items, 0, SALTS_X, SALTS_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(TTItems.BATH_SALTS.get());
            }
        });

        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
    }

    public @Nullable BlockEntitySpa blockEntity() {
        return (BlockEntitySpa) access.evaluate(Level::getBlockEntity)
                .filter(be -> be instanceof BlockEntitySpa)
                .orElse(null);
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
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.SPA.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return quickMoveBetween(slotIndex, SLOT_COUNT, stackInSlot -> stackInSlot.is(TTItems.BATH_SALTS.get()));
    }
}
