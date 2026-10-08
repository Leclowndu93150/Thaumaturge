package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
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

public final class MenuPotionSprayer extends AbstractTTMenu {
    private static final int MACHINE_SLOTS = 1;
    public static final int POTION_SLOT_X = 56;
    public static final int POTION_SLOT_Y = 64;
    public static final int INVENTORY_X = 16;
    public static final int INVENTORY_Y = 151;
    public static final int HOTBAR_Y = 209;

    private final Container container;
    private final ContainerLevelAccess access;
    private final BlockPos pos;

    public MenuPotionSprayer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(1), ContainerLevelAccess.NULL, buf.readBlockPos());
    }

    public MenuPotionSprayer(int containerId, Inventory playerInventory, BlockEntityPotionSprayer sprayer) {
        this(
                containerId,
                playerInventory,
                new PotionSprayerContainer(sprayer),
                ContainerLevelAccess.create(sprayer.getLevel(), sprayer.getBlockPos()),
                sprayer.getBlockPos());
    }

    private MenuPotionSprayer(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerLevelAccess access,
            BlockPos pos) {
        super(TTMenus.POTION_SPRAYER.get(), containerId);
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
        addInventoryExtendedSlots(playerInventory, INVENTORY_X, INVENTORY_Y);
        addInventoryHotbarSlots(playerInventory, INVENTORY_X, HOTBAR_Y);
    }

    public BlockPos sprayerPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.POTION_SPRAYER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, MACHINE_SLOTS, BlockEntityPotionSprayer::isValidPotion);
    }
}
