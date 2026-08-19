package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.leclowndu93150.thaumaturge.content.entity.ai.PechItemGoal;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class MenuPech extends AbstractContainerMenu {
    public static final int INPUT_X = 36;
    public static final int INPUT_Y = 29;
    public static final int OUTPUT_X = 106;
    public static final int OUTPUT_Y = 20;
    public static final int PLAYER_GRID_X = 8;
    public static final int PLAYER_GRID_Y = 84;
    public static final int HOTBAR_Y = 142;
    public static final int TRADE_BUTTON_ID = 0;

    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOTS = 4;
    public static final int SLOT_COUNT = 1 + OUTPUT_SLOTS;
    public static final int TOTAL_INVENTORY_SLOTS = SLOT_COUNT + 36;

    private static final int UNTAME_ROLL = 100;
    private static final int MAX_TIER = 5;
    private static final float PACK_ITEM_CHANCE = 0.5F;

    private final @Nullable EntityPech pech;
    private final SimpleContainer tradeContainer = new SimpleContainer(SLOT_COUNT);

    public MenuPech(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getEntity(buf.readVarInt()) instanceof EntityPech p ? p : null);
    }

    public MenuPech(int containerId, Inventory playerInventory, @Nullable EntityPech pech) {
        super(TCMenus.PECH.get(), containerId);
        this.pech = pech;
        if (pech != null) {
            pech.trading = true;
        }

        addSlot(new Slot(tradeContainer, INPUT_SLOT, INPUT_X, INPUT_Y));
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                addSlot(new Slot(tradeContainer, 1 + col + row * 2, OUTPUT_X + 18 * col, OUTPUT_Y + 18 * row) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_GRID_X + col * 18, PLAYER_GRID_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_GRID_X + col * 18, HOTBAR_Y));
        }
    }

    public @Nullable EntityPech pech() {
        return pech;
    }

    public boolean canTrade() {
        ItemStack input = tradeContainer.getItem(INPUT_SLOT);
        if (pech == null || input.isEmpty() || !pech.isValued(input)) {
            return false;
        }
        for (int slot = 1; slot <= OUTPUT_SLOTS; slot++) {
            if (!tradeContainer.getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == TRADE_BUTTON_ID) {
            generateContents(player);
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    private void generateContents(Player player) {
        if (player.level().isClientSide() || pech == null || !canTrade()) {
            return;
        }
        RandomSource rand = player.level().getRandom();
        int value = pech.getValue(tradeContainer.getItem(INPUT_SLOT));
        if (rand.nextInt(UNTAME_ROLL) <= value / 2) {
            pech.setTamed(false);
            pech.playSound(TCSounds.PECH_TRADE.get(), 0.4F, 1.0F);
        }
        if (rand.nextInt(5) == 0) {
            value += rand.nextInt(3);
        } else if (rand.nextBoolean()) {
            value -= rand.nextInt(3);
        }
        List<PechTrades.Entry> trades = PechTrades.tradesFor(pech.getPechType(), player.level().registryAccess());
        while (value > 0) {
            int amount = Math.min(MAX_TIER, Math.max((value + 1) / 2, rand.nextInt(value) + 1));
            value -= amount;
            if (amount == 1 && rand.nextFloat() < PACK_ITEM_CHANCE && hasStuffInPack()) {
                List<Integer> filled = new ArrayList<>();
                for (int a = 0; a < pech.loot.size(); a++) {
                    if (!pech.loot.get(a).isEmpty()) {
                        filled.add(a);
                    }
                }
                int slot = filled.get(rand.nextInt(filled.size()));
                ItemStack given = pech.loot.get(slot).copy();
                given.setCount(1);
                addStack(given);
                pech.loot.get(slot).shrink(1);
                if (pech.loot.get(slot).isEmpty()) {
                    pech.loot.set(slot, ItemStack.EMPTY);
                }
            } else if (amount < 4 || !rand.nextBoolean()) {
                int wanted = amount;
                List<PechTrades.Entry> pool = trades.stream().filter(entry -> entry.tier() == wanted).toList();
                if (!pool.isEmpty()) {
                    addStack(pool.get(rand.nextInt(pool.size())).stack().copy());
                }
            }
        }
        tradeContainer.removeItem(INPUT_SLOT, 1);
    }

    private boolean hasStuffInPack() {
        if (pech == null) {
            return false;
        }
        for (ItemStack stack : pech.loot) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void addStack(ItemStack stack) {
        for (int slot = 1; slot <= OUTPUT_SLOTS; slot++) {
            ItemStack existing = tradeContainer.getItem(slot);
            if (existing.isEmpty()) {
                tradeContainer.setItem(slot, stack);
                return;
            }
            if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() + stack.getCount() < existing.getMaxStackSize()) {
                existing.grow(stack.getCount());
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
        if (!player.level().isClientSide()) {
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                ItemStack stack = tradeContainer.removeItemNoUpdate(slot);
                if (!stack.isEmpty()) {
                    ItemEntity dropped = player.drop(stack, false);
                    if (dropped != null) {
                        dropped.addTag(PechItemGoal.PECH_DROP_TAG);
                    }
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            returnStack = stackInSlot.copy();
            if (slotIndex < SLOT_COUNT) {
                if (!moveItemStackTo(stackInSlot, SLOT_COUNT, TOTAL_INVENTORY_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, 1, true)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stackInSlot.getCount() == returnStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stackInSlot);
        }
        return returnStack;
    }
}
