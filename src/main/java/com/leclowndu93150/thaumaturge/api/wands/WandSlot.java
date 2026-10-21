package com.leclowndu93150.thaumaturge.api.wands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * A live, server-owned slot. Implementations must return the actual stack, check ownership and
 * proximity in {@link #mayAccess}, and persist and synchronize it in {@link #changed}.
 * Keep the slot and its contents stable until the enclosing transaction closes.
 */
public interface WandSlot {
    ItemStack stack();

    boolean mayAccess(ServerPlayer player);

    void changed();

    static WandSlot playerInventory(ServerPlayer owner, int index) {
        if (index < 0 || index >= owner.getInventory().getContainerSize()) {
            throw new IllegalArgumentException("Invalid inventory slot");
        }
        return new WandSlot() {
            @Override
            public ItemStack stack() {
                return owner.getInventory().getItem(index);
            }

            @Override
            public boolean mayAccess(ServerPlayer player) {
                return player == owner && owner.isAlive() && !owner.isSpectator();
            }

            @Override
            public void changed() {
                owner.getInventory().setChanged();
                owner.containerMenu.broadcastChanges();
            }
        };
    }
}
