package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class MenuTurretBasic extends AbstractContainerMenu {
    public static final int AMMO_X = 80;
    public static final int AMMO_Y = 29;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;
    public static final int HOTBAR_Y = 142;

    protected final @Nullable EntityTurretCrossbow turret;

    public MenuTurretBasic(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(TCMenus.TURRET_BASIC.get(), containerId, playerInventory, playerInventory.player.level().getEntity(buf.readVarInt()) instanceof EntityTurretCrossbow t ? t : null, AMMO_X, AMMO_Y);
    }

    protected MenuTurretBasic(MenuType<?> type, int containerId, Inventory playerInventory, @Nullable EntityTurretCrossbow turret, int slotX, int slotY) {
        super(type, containerId);
        this.turret = turret;
        addSlot(new Slot(new MobEquipmentContainer(turret), 0, slotX, slotY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return turret != null && turret.isValidAmmo(stack);
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

    public static void open(Player player, EntityTurretCrossbow turret) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new MenuTurretBasic(TCMenus.TURRET_BASIC.get(), id, inv, turret, AMMO_X, AMMO_Y), turret.getDisplayName()),
                    buf -> buf.writeVarInt(turret.getId()));
        }
    }

    public @Nullable EntityTurretCrossbow turret() {
        return turret;
    }

    @Override
    public boolean stillValid(Player player) {
        return turret != null && turret.isAlive();
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
