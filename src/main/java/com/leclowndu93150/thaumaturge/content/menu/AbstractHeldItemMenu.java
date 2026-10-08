package com.leclowndu93150.thaumaturge.content.menu;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public abstract class AbstractHeldItemMenu extends AbstractTTMenu {
    private static final int OFFHAND_SWAP_BUTTON = 40;

    private final InteractionHand hand;
    private final ItemStack held;
    public final int blockedHotbarSlot;

    protected AbstractHeldItemMenu(
            @Nullable MenuType<?> type, int containerId, Inventory inventory, InteractionHand hand) {
        super(type, containerId);
        this.hand = hand;
        this.held = inventory.player.getItemInHand(hand);
        this.blockedHotbarSlot = hand == InteractionHand.MAIN_HAND ? inventory.selected : -1;
    }

    protected final ItemStack held() {
        return held;
    }

    protected abstract void writeBack(ItemStack held);

    private boolean touchesHeld(int slotId, int button, ClickType input, Player player) {
        if (slotId >= 0 && slotId < slots.size()) {
            Slot slot = slots.get(slotId);
            if (slot.container == player.getInventory() && slot.getItem() == held) {
                return true;
            }
        }
        if (input != ClickType.SWAP) {
            return false;
        }
        return hand == InteractionHand.OFF_HAND ? button == OFFHAND_SWAP_BUTTON : button == blockedHotbarSlot;
    }

    @Override
    public void clicked(int slotId, int button, ClickType input, Player player) {
        if (touchesHeld(slotId, button, input, player)) {
            return;
        }
        super.clicked(slotId, button, input, player);
        writeBack(held);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        writeBack(held);
    }

    @Override
    public final ItemStack quickMoveStack(Player player, int index) {
        if (index >= 0 && index < slots.size() && slots.get(index).getItem() == held) {
            return ItemStack.EMPTY;
        }
        ItemStack moved = quickMove(player, index);
        writeBack(held);
        return moved;
    }

    protected abstract ItemStack quickMove(Player player, int index);

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand) == held;
    }
}
