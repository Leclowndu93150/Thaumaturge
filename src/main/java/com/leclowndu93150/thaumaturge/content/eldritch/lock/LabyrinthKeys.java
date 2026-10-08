package com.leclowndu93150.thaumaturge.content.eldritch.lock;

import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Optional;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class LabyrinthKeys {
    private LabyrinthKeys() {}

    public static ItemStack bound(MazeId maze) {
        ItemStack tablet = new ItemStack(TTItems.RUNED_TABLET.get());
        tablet.set(TTDataComponents.LABYRINTH_KEY.get(), maze);
        return tablet;
    }

    public static Optional<MazeId> boundTo(ItemStack stack) {
        return Optional.ofNullable(stack.get(TTDataComponents.LABYRINTH_KEY.get()));
    }

    public static boolean carries(Player player, MazeId maze) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(TTItems.RUNED_TABLET.get())
                    && boundTo(stack).filter(maze::equals).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
