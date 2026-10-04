package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuResearchTable extends AbstractContainerMenu {
    public static final int SCRIBE_TOOLS_X = 14;
    public static final int SCRIBE_TOOLS_Y = 10;
    public static final int NOTE_X = 70;
    public static final int NOTE_Y = 10;

    public static final int PLAYER_GRID_X = 48;
    public static final int PLAYER_GRID_Y = 175;
    public static final int HOTBAR_Y = 233;

    public static final int TABLE_SLOT_COUNT = BlockEntityResearchTable.SLOT_COUNT;
    public static final int PLAYER_ROW_SLOTS = 9;
    public static final int PLAYER_ROWS = 3;
    public static final int PLAYER_TOTAL_SLOTS = PLAYER_ROW_SLOTS * (PLAYER_ROWS + 1);
    public static final int TOTAL_INVENTORY_SLOTS = TABLE_SLOT_COUNT + PLAYER_TOTAL_SLOTS;

    private final ItemStacksResourceHandler items;
    private final ContainerLevelAccess access;
    private final BlockPos pos;
    private final @Nullable BlockEntityResearchTable blockEntity;

    public MenuResearchTable(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, clientBlockEntity(playerInventory, buf.readBlockPos()));
    }

    private static @Nullable BlockEntityResearchTable clientBlockEntity(Inventory playerInventory, BlockPos pos) {
        return playerInventory.player.level().getBlockEntity(pos) instanceof BlockEntityResearchTable table ? table : null;
    }

    public MenuResearchTable(int containerId, Inventory playerInventory, @Nullable BlockEntityResearchTable blockEntity) {
        super(TCMenus.RESEARCH_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.items = blockEntity != null ? blockEntity.items() : new ItemStacksResourceHandler(BlockEntityResearchTable.SLOT_COUNT);
        this.access = blockEntity != null ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()) : ContainerLevelAccess.NULL;
        this.pos = blockEntity != null ? blockEntity.getBlockPos() : BlockPos.ZERO;
        ItemStacksResourceHandler items = this.items;

        addSlot(new ResourceHandlerSlot(items, items::set, BlockEntityResearchTable.SLOT_SCRIBE_TOOLS, SCRIBE_TOOLS_X, SCRIBE_TOOLS_Y));
        addSlot(new ResourceHandlerSlot(items, items::set, BlockEntityResearchTable.SLOT_NOTE, NOTE_X, NOTE_Y));

        for (int row = 0; row < PLAYER_ROWS; row++) {
            for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
                addSlot(new Slot(playerInventory, col + row * PLAYER_ROW_SLOTS + PLAYER_ROW_SLOTS, PLAYER_GRID_X + col * 18, PLAYER_GRID_Y + row * 18));
            }
        }
        for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_GRID_X + col * 18, HOTBAR_Y));
        }
    }

    private ItemStack lastTools = ItemStack.EMPTY;
    private ItemStack lastNote = ItemStack.EMPTY;

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (blockEntity == null || blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide()) {
            return;
        }
        ItemStack tools = slots.get(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS).getItem();
        ItemStack note = slots.get(BlockEntityResearchTable.SLOT_NOTE).getItem();
        if (!ItemStack.matches(tools, lastTools) || !ItemStack.matches(note, lastNote)) {
            lastTools = tools.copy();
            lastNote = note.copy();
            blockEntity.ensureNotePuzzle();
            blockEntity.setChanged();
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }

    public BlockPos pos() {
        return pos;
    }

    public @Nullable BlockEntityResearchTable blockEntity() {
        return blockEntity;
    }

    public ItemStacksResourceHandler tableItems() {
        return items;
    }

    public BlockPos tablePos() {
        return pos;
    }

    public boolean hasUsableScribeTools() {
        ItemResource resource = items.getResource(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS);
        int amount = items.getAmountAsInt(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS);
        if (resource.isEmpty() || amount <= 0)
            return false;
        ItemStack stack = resource.toStack(amount);
        return stack.isDamageableItem() && stack.getDamageValue() < stack.getMaxDamage();
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TCBlocks.RESEARCH_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            returnStack = stackInSlot.copy();
            if (slotIndex < TABLE_SLOT_COUNT) {
                if (!moveItemStackTo(stackInSlot, TABLE_SLOT_COUNT, TOTAL_INVENTORY_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, TABLE_SLOT_COUNT, false)) {
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
