package com.leclowndu93150.thaumaturge.content.equipment.hover;

import com.leclowndu93150.thaumaturge.content.menu.AbstractHeldItemMenu;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MenuThaumostaticHarness extends AbstractHeldItemMenu {
    private static final int JAR_SLOT = 0;
    private static final int JAR_SLOTS = 1;
    private static final int JAR_SLOT_X = 79;
    private static final int JAR_SLOT_Y = 31;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;

    private final SimpleContainer jarContainer = new SimpleContainer(JAR_SLOTS);

    public MenuThaumostaticHarness(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public MenuThaumostaticHarness(int containerId, Inventory inventory, InteractionHand hand) {
        super(TTMenus.THAUMOSTATIC_HARNESS.get(), containerId, inventory, hand);
        jarContainer.setItem(JAR_SLOT, ThaumostaticHarnessItem.getJar(held()));
        addSlot(new JarSlot(jarContainer, JAR_SLOT, JAR_SLOT_X, JAR_SLOT_Y));
        addStandardInventorySlots(inventory, PLAYER_INV_X, PLAYER_INV_Y);
    }

    @Override
    protected void writeBack(ItemStack harness) {
        if (harness.getItem() instanceof ThaumostaticHarnessItem) {
            ThaumostaticHarnessItem.setJar(harness, jarContainer.getItem(JAR_SLOT));
        }
    }

    @Override
    protected ItemStack quickMove(Player player, int index) {
        return quickMoveBetween(index, JAR_SLOTS, ThaumostaticHarnessItem::isFuelJar);
    }

    private static final class JarSlot extends Slot {
        JarSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ThaumostaticHarnessItem.isFuelJar(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
