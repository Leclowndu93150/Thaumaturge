package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.content.golem.ItemGolemPlacer;
import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.content.menu.BlockMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jspecify.annotations.Nullable;

public final class MenuGolemBuilder extends AbstractTTMenu implements BlockMenu<BlockEntityGolemBuilder> {
    public static final int OUTPUT_X = 160;
    public static final int OUTPUT_Y = 104;
    public static final int PLAYER_GRID_X = 24;
    public static final int PLAYER_GRID_Y = 142;
    public static final int HOTBAR_Y = 200;

    public static final int SLOT_COUNT = 1;

    private final ContainerLevelAccess access;
    private final DataSlot cost = DataSlot.standalone();
    private final DataSlot maxCost = DataSlot.standalone();

    public MenuGolemBuilder(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(
                containerId,
                playerInventory,
                new ItemStackHandler(SLOT_COUNT),
                ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()));
    }

    public MenuGolemBuilder(int containerId, Inventory playerInventory, BlockEntityGolemBuilder blockEntity) {
        this(
                containerId,
                playerInventory,
                blockEntity.output(),
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    private MenuGolemBuilder(
            int containerId, Inventory playerInventory, ItemStackHandler items, ContainerLevelAccess access) {
        super(TTMenus.GOLEM_BUILDER.get(), containerId);
        this.access = access;

        addSlot(new SlotItemHandler(items, 0, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemGolemPlacer;
            }
        });

        addInventoryExtendedSlots(playerInventory, PLAYER_GRID_X, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, PLAYER_GRID_X, HOTBAR_Y);

        addDataSlot(cost);
        addDataSlot(maxCost);
    }

    @Override
    public void broadcastChanges() {
        BlockEntityGolemBuilder builder = blockEntity();
        if (builder != null) {
            cost.set(builder.cost());
            maxCost.set(builder.maxCost());
        }
        super.broadcastChanges();
    }

    public int cost() {
        return cost.get();
    }

    public int maxCost() {
        return maxCost.get();
    }

    @Override
    public @Nullable BlockEntityGolemBuilder blockEntity() {
        return (BlockEntityGolemBuilder) access.evaluate(Level::getBlockEntity)
                .filter(be -> be instanceof BlockEntityGolemBuilder)
                .orElse(null);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, TTBlocks.GOLEM_BUILDER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return quickMoveBetween(
                slotIndex, SLOT_COUNT, stackInSlot -> (stackInSlot.getItem() instanceof ItemGolemPlacer));
    }
}
