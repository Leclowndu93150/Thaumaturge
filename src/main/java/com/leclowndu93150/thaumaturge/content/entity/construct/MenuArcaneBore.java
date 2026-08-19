package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class MenuArcaneBore extends AbstractContainerMenu {
    public static final int PICK_X = 80;
    public static final int PICK_Y = 29;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;
    public static final int HOTBAR_Y = 142;

    private final @Nullable EntityArcaneBore bore;

    public MenuArcaneBore(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getEntity(buf.readVarInt()) instanceof EntityArcaneBore b ? b : null);
    }

    private MenuArcaneBore(int containerId, Inventory playerInventory, @Nullable EntityArcaneBore bore) {
        super(TCMenus.ARCANE_BORE.get(), containerId);
        this.bore = bore;
        addSlot(new Slot(new MobEquipmentContainer(bore), 0, PICK_X, PICK_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return EntityArcaneBore.isPickaxe(stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_GRID_X + col * 18, PLAYER_GRID_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_GRID_X + col * 18, HOTBAR_Y));
        }
    }

    public static void open(Player player, EntityArcaneBore bore) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new MenuArcaneBore(id, inv, bore), bore.getDisplayName()), buf -> buf.writeVarInt(bore.getId()));
        }
    }

    public @Nullable EntityArcaneBore bore() {
        return bore;
    }

    @Override
    public boolean stillValid(Player player) {
        return bore != null && bore.isAlive();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            returnStack = stackInSlot.copy();
            if (slotIndex == 0) {
                if (!moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
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
