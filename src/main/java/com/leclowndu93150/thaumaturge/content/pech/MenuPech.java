package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.stream.IntStream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class MenuPech extends AbstractContainerMenu {
    public static final int TRADE_BUTTON_ID = 0;

    private static final int OFFER_SLOT = 0;
    private static final int OFFER_X = 36;
    private static final int OFFER_Y = 29;
    private static final int PAYOUT_COLUMNS = 2;
    private static final int PAYOUT_ROWS = 2;
    private static final int PAYOUT_SLOTS = PAYOUT_COLUMNS * PAYOUT_ROWS;
    private static final int PAYOUT_X = 106;
    private static final int PAYOUT_Y = 20;
    private static final int TABLE_SLOTS = 1 + PAYOUT_SLOTS;
    private static final int SLOT_PITCH = 18;
    private static final int BACKPACK_X = 8;
    private static final int BACKPACK_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int BACKPACK_ROWS = 3;
    private static final int ROW_LENGTH = 9;
    private static final int PLAYER_SLOTS = ROW_LENGTH * (BACKPACK_ROWS + 1);

    private final @Nullable EntityPech pech;
    private final SimpleContainer table = new SimpleContainer(TABLE_SLOTS);

    public MenuPech(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(
                containerId,
                playerInventory,
                playerInventory.player.level().getEntity(buf.readVarInt()) instanceof EntityPech trader
                        ? trader
                        : null);
    }

    public MenuPech(int containerId, Inventory playerInventory, @Nullable EntityPech pech) {
        super(TTMenus.PECH.get(), containerId);
        this.pech = pech;
        if (pech != null) {
            pech.trading = true;
        }
        addSlot(new Slot(table, OFFER_SLOT, OFFER_X, OFFER_Y));
        for (int index = 0; index < PAYOUT_SLOTS; index++) {
            addSlot(new PechPayoutSlot(
                    table,
                    1 + index,
                    PAYOUT_X + SLOT_PITCH * (index % PAYOUT_COLUMNS),
                    PAYOUT_Y + SLOT_PITCH * (index / PAYOUT_COLUMNS)));
        }
        for (int index = ROW_LENGTH; index < PLAYER_SLOTS; index++) {
            addSlot(new Slot(
                    playerInventory,
                    index,
                    BACKPACK_X + SLOT_PITCH * (index % ROW_LENGTH),
                    BACKPACK_Y + SLOT_PITCH * (index / ROW_LENGTH - 1)));
        }
        for (int index = 0; index < ROW_LENGTH; index++) {
            addSlot(new Slot(playerInventory, index, BACKPACK_X + SLOT_PITCH * index, HOTBAR_Y));
        }
    }

    public @Nullable EntityPech pech() {
        return pech;
    }

    public boolean canTrade() {
        ItemStack offer = table.getItem(OFFER_SLOT);
        return pech != null
                && !offer.isEmpty()
                && pech.isValued(offer)
                && IntStream.rangeClosed(1, PAYOUT_SLOTS)
                        .allMatch(index -> table.getItem(index).isEmpty());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != TRADE_BUTTON_ID) {
            return super.clickMenuButton(player, id);
        }
        if (!player.level().isClientSide() && pech != null && canTrade()) {
            PechBarter.haggle(
                            pech,
                            table.getItem(OFFER_SLOT),
                            player.level().getRandom(),
                            player.level().registryAccess())
                    .forEach(this::shelve);
            table.removeItem(OFFER_SLOT, 1);
        }
        return true;
    }

    private void shelve(ItemStack stack) {
        for (int index = 1; index <= PAYOUT_SLOTS; index++) {
            ItemStack shelved = table.getItem(index);
            if (shelved.isEmpty()) {
                table.setItem(index, stack);
                return;
            }
            if (ItemStack.isSameItemSameComponents(shelved, stack)
                    && shelved.getCount() + stack.getCount() < shelved.getMaxStackSize()) {
                shelved.grow(stack.getCount());
                return;
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return pech != null && pech.isAlive() && pech.isTamed();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (pech != null) {
            pech.trading = false;
        }
        if (player.level().isClientSide()) {
            return;
        }
        for (int index = 0; index < TABLE_SLOTS; index++) {
            ItemStack leftover = table.removeItemNoUpdate(index);
            ItemEntity dropped = leftover.isEmpty() ? null : player.drop(leftover, false);
            if (dropped != null) {
                dropped.addTag(EntityPech.DROPPED_BY_PECH_TAG);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack moving = slot.getItem();
        ItemStack original = moving.copy();
        boolean moved = slotIndex < TABLE_SLOTS
                ? moveItemStackTo(moving, TABLE_SLOTS, TABLE_SLOTS + PLAYER_SLOTS, true)
                : moveItemStackTo(moving, OFFER_SLOT, OFFER_SLOT + 1, true);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (moving.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, moving);
        return original;
    }
}
