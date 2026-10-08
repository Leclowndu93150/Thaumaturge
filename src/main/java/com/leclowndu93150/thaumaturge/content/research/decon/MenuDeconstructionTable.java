package com.leclowndu93150.thaumaturge.content.research.decon;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.content.menu.BlockMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jspecify.annotations.Nullable;

public final class MenuDeconstructionTable extends AbstractTTMenu implements BlockMenu<BlockEntityDeconstructionTable> {
    public static final int INPUT_X = 63;
    public static final int INPUT_Y = 15;
    private static final int PLAYER_GRID_X = 8;
    private static final int PLAYER_GRID_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int TABLE_SLOTS = 1;

    private final @Nullable BlockEntityDeconstructionTable blockEntity;
    private final ContainerLevelAccess access;
    private final BlockPos pos;
    private final DataSlot breakTime = DataSlot.standalone();

    public MenuDeconstructionTable(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, clientBlockEntity(playerInventory, buf.readBlockPos()));
    }

    private static @Nullable BlockEntityDeconstructionTable clientBlockEntity(Inventory playerInventory, BlockPos pos) {
        return playerInventory.player.level().getBlockEntity(pos) instanceof BlockEntityDeconstructionTable table
                ? table
                : null;
    }

    public MenuDeconstructionTable(
            int containerId, Inventory playerInventory, @Nullable BlockEntityDeconstructionTable blockEntity) {
        super(TTMenus.DECONSTRUCTION_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = blockEntity != null
                ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;
        this.pos = blockEntity != null ? blockEntity.getBlockPos() : BlockPos.ZERO;
        ItemStackHandler items = blockEntity != null ? blockEntity.items() : new ItemStackHandler(TABLE_SLOTS);

        addSlot(new SlotItemHandler(items, BlockEntityDeconstructionTable.SLOT_INPUT, INPUT_X, INPUT_Y));

        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
        breakTime.set(BlockEntityDeconstructionTable.BREAK_TIME_TICKS);
        addDataSlot(breakTime);
    }

    @Override
    public void broadcastChanges() {
        if (blockEntity != null
                && blockEntity.getLevel() != null
                && !blockEntity.getLevel().isClientSide()) {
            breakTime.set(blockEntity.breakTime());
        }
        super.broadcastChanges();
    }

    public int breakTime() {
        return breakTime.get();
    }

    @Override
    public @Nullable BlockEntityDeconstructionTable blockEntity() {
        return blockEntity;
    }

    public BlockPos pos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.DECONSTRUCTION_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return quickMoveBetween(slotIndex, TABLE_SLOTS, stack -> true);
    }
}
