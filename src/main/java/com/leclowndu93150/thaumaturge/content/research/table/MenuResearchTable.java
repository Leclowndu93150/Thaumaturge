package com.leclowndu93150.thaumaturge.content.research.table;

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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jspecify.annotations.Nullable;

public final class MenuResearchTable extends AbstractTTMenu implements BlockMenu<BlockEntityResearchTable> {
    public static final int SCRIBE_TOOLS_X = 14;
    public static final int SCRIBE_TOOLS_Y = 10;
    public static final int NOTE_X = 70;
    public static final int NOTE_Y = 10;

    public static final int PLAYER_GRID_X = 48;
    public static final int PLAYER_GRID_Y = 175;
    public static final int HOTBAR_Y = 233;

    public static final int TABLE_SLOT_COUNT = BlockEntityResearchTable.SLOT_COUNT;

    private final ItemStackHandler items;
    private final ContainerLevelAccess access;
    private final BlockPos pos;
    private final @Nullable BlockEntityResearchTable blockEntity;

    public MenuResearchTable(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, clientBlockEntity(playerInventory, buf.readBlockPos()));
    }

    private static @Nullable BlockEntityResearchTable clientBlockEntity(Inventory playerInventory, BlockPos pos) {
        return playerInventory.player.level().getBlockEntity(pos) instanceof BlockEntityResearchTable table
                ? table
                : null;
    }

    public MenuResearchTable(
            int containerId, Inventory playerInventory, @Nullable BlockEntityResearchTable blockEntity) {
        super(TTMenus.RESEARCH_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.items =
                blockEntity != null ? blockEntity.items() : new ItemStackHandler(BlockEntityResearchTable.SLOT_COUNT);
        this.access = blockEntity != null
                ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;
        this.pos = blockEntity != null ? blockEntity.getBlockPos() : BlockPos.ZERO;
        ItemStackHandler items = this.items;

        addSlot(new SlotItemHandler(items, BlockEntityResearchTable.SLOT_SCRIBE_TOOLS, SCRIBE_TOOLS_X, SCRIBE_TOOLS_Y));
        addSlot(new SlotItemHandler(items, BlockEntityResearchTable.SLOT_NOTE, NOTE_X, NOTE_Y));

        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);
    }

    private ItemStack lastTools = ItemStack.EMPTY;
    private ItemStack lastNote = ItemStack.EMPTY;

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (blockEntity == null
                || blockEntity.getLevel() == null
                || blockEntity.getLevel().isClientSide()) {
            return;
        }
        ItemStack tools = slots.get(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS).getItem();
        ItemStack note = slots.get(BlockEntityResearchTable.SLOT_NOTE).getItem();
        if (!ItemStack.matches(tools, lastTools) || !ItemStack.matches(note, lastNote)) {
            lastTools = tools.copy();
            lastNote = note.copy();
            blockEntity.ensureNotePuzzle();
            blockEntity.setChanged();
            blockEntity
                    .getLevel()
                    .sendBlockUpdated(
                            blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }

    public BlockPos pos() {
        return pos;
    }

    @Override
    public @Nullable BlockEntityResearchTable blockEntity() {
        return blockEntity;
    }

    public ItemStackHandler tableItems() {
        return items;
    }

    public BlockPos tablePos() {
        return pos;
    }

    public boolean hasUsableScribeTools() {
        ItemStack resource = items.getStackInSlot(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS);
        int amount =
                items.getStackInSlot(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS).getCount();
        if (resource.isEmpty() || amount <= 0) return false;
        ItemStack stack = resource.copyWithCount(amount);
        return stack.isDamageableItem() && stack.getDamageValue() < stack.getMaxDamage();
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.RESEARCH_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return quickMoveBetween(slotIndex, TABLE_SLOT_COUNT, stack -> true);
    }
}
