package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jspecify.annotations.Nullable;

public final class MenuVoidSiphon extends AbstractTTMenu {
    private static final int MACHINE_SLOTS = 1;
    private static final double REACH_BUFFER = 4.0;

    public static final int OUTPUT_X = 80;
    public static final int OUTPUT_Y = 32;
    private static final int PLAYER_GRID_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final @Nullable BlockEntityVoidSiphon blockEntity;
    private final DataSlot progress = DataSlot.standalone();

    public MenuVoidSiphon(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, (BlockEntityVoidSiphon) null);
        buf.readBlockPos();
    }

    public MenuVoidSiphon(int containerId, Inventory playerInventory, @Nullable BlockEntityVoidSiphon blockEntity) {
        super(TTMenus.VOID_SIPHON.get(), containerId);
        this.blockEntity = blockEntity;
        ItemStackHandler items = blockEntity != null ? blockEntity.output() : new ItemStackHandler(1);

        addSlot(new SlotItemHandler(items, 0, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addInventoryExtendedSlots(playerInventory, 8, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, 8, HOTBAR_Y);
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
        return quickMoveBetween(index, MACHINE_SLOTS, stack -> false);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null
                && !blockEntity.isRemoved()
                && player.canInteractWithBlock(blockEntity.getBlockPos(), REACH_BUFFER);
    }
}
