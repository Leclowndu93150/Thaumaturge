package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealProvide;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jspecify.annotations.Nullable;

public final class MenuGolemLogistics extends AbstractContainerMenu {
    public static final int ROWS = 6;
    private static final int SIZE = ROWS * 9;
    private static final int RANGE = 32;

    private final SimpleContainer catalogue;
    private final Player player;
    private final @Nullable BlockPos destination;
    private final @Nullable Direction side;

    public MenuGolemLogistics(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, new SimpleContainer(SIZE), null, null);
    }

    private MenuGolemLogistics(
            int containerId,
            Inventory inventory,
            SimpleContainer catalogue,
            @Nullable BlockPos destination,
            @Nullable Direction side) {
        super(TCMenus.GOLEM_LOGISTICS.get(), containerId);
        this.catalogue = catalogue;
        this.player = inventory.player;
        this.destination = destination;
        this.side = side;
        for (int slot = 0; slot < SIZE; slot++) {
            int x = 8 + slot % 9 * 18;
            int y = 18 + slot / 9 * 18;
            addSlot(new CatalogueSlot(catalogue, slot, x, y));
        }
    }

    public static MenuGolemLogistics server(
            int containerId, Inventory inventory, @Nullable BlockPos destination, @Nullable Direction side) {
        SimpleContainer catalogue = collect(inventory.player);
        return new MenuGolemLogistics(containerId, inventory, catalogue, destination, side);
    }

    private static SimpleContainer collect(Player player) {
        SimpleContainer catalogue = new SimpleContainer(SIZE);
        if (!(player.level() instanceof ServerLevel level)) {
            return catalogue;
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (SealEntity entity : SealHandler.getSealsInRange(level, player.blockPosition(), RANGE)) {
            if (!(entity.getSeal() instanceof SealProvide provider)) {
                continue;
            }
            IItemHandler inventory = InvHelper.getItemHandlerAt(
                    level, entity.getSealPos().pos(), entity.getSealPos().face());
            if (inventory == null) {
                continue;
            }
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack found = inventory.getStackInSlot(slot);
                if (found.isEmpty() || !provider.matchesFilters(found)) {
                    continue;
                }
                ItemStack existing = stacks.stream()
                        .filter(stack -> ItemStack.isSameItemSameComponents(stack, found))
                        .findFirst()
                        .orElse(null);
                if (existing == null) {
                    stacks.add(found.copy());
                } else {
                    existing.grow(found.getCount());
                }
            }
        }
        stacks.sort((left, right) -> left.getHoverName()
                .getString()
                .compareToIgnoreCase(right.getHoverName().getString()));
        for (int slot = 0; slot < Math.min(SIZE, stacks.size()); slot++) {
            catalogue.setItem(slot, stacks.get(slot));
        }
        return catalogue;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId < 0 || slotId >= catalogue.getContainerSize()) {
            return;
        }
        ItemStack available = catalogue.getItem(slotId);
        if (available.isEmpty() || player.level().isClientSide()) {
            return;
        }
        int requested =
                clickType == ClickType.QUICK_MOVE ? Math.min(available.getCount(), available.getMaxStackSize()) : 1;
        ItemStack stack = available.copyWithCount(requested);
        if (destination != null && side != null) {
            GolemHelper.requestProvisioning(player.level(), destination, side, stack, containerId);
        } else {
            GolemHelper.requestProvisioning(player.level(), player, stack, containerId);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    private static final class CatalogueSlot extends Slot {
        private CatalogueSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
