package com.leclowndu93150.thaumaturge.content.infusion.grindstone;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class MenuArcaneGrindstone extends AbstractTTMenu {
    public static final int INPUT_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final int INPUT_COUNT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int HOTBAR_SLOT_END = 39;
    private static final int INPUT_X = 49;
    private static final int INPUT_Y = 19;
    private static final int ADDITIONAL_Y = 40;
    private static final int RESULT_X = 129;
    private static final int RESULT_Y = 34;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int XP_PER_LEVEL = 10;
    private static final int LEVEL_EVENT_GRINDSTONE_USE = 1042;

    private final InputContainer inputs = new InputContainer(this);
    private final Container result = new ResultContainer();
    private final ContainerLevelAccess access;

    public MenuArcaneGrindstone(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public MenuArcaneGrindstone(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(TTMenus.ARCANE_GRINDSTONE.get(), containerId);
        this.access = access;
        addSlot(new InputSlot(inputs, INPUT_SLOT, INPUT_X, INPUT_Y));
        addSlot(new InputSlot(inputs, ADDITIONAL_SLOT, INPUT_X, ADDITIONAL_Y));
        addSlot(new ResultSlot(this, result, RESULT_X, RESULT_Y));
        addInventoryExtendedSlots(inventory, INVENTORY_X, INVENTORY_Y);
        addInventoryHotbarSlots(inventory, INVENTORY_X, HOTBAR_Y);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == inputs) {
            result.setItem(0, computeResult());
            broadcastChanges();
        }
    }

    private ItemStack computeResult() {
        ItemStack first = inputs.getItem(INPUT_SLOT);
        ItemStack second = inputs.getItem(ADDITIONAL_SLOT);
        if (first.isEmpty() == second.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack input = first.isEmpty() ? second : first;
        if (!isInfused(input)) {
            return ItemStack.EMPTY;
        }
        ItemStack stripped = input.copy();
        stripped.remove(TTDataComponents.INFUSION_ENCHANTMENTS.get());
        return stripped;
    }

    private static boolean isInfused(ItemStack stack) {
        return !InfusionEnchantmentHelper.list(stack).isEmpty();
    }

    private int experienceFor(Level level) {
        int total = 0;
        for (int slot = 0; slot < INPUT_COUNT; slot++) {
            ItemStack stack = inputs.getItem(slot);
            for (InfusionEnchantment enchantment : InfusionEnchantmentHelper.list(stack)) {
                total += InfusionEnchantmentHelper.level(stack, enchantment) * XP_PER_LEVEL;
            }
        }
        if (total <= 0) {
            return 0;
        }
        int half = (int) Math.ceil(total / 2.0);
        return half + level.getRandom().nextInt(half);
    }

    private void onResultTaken() {
        access.execute((level, pos) -> {
            if (level instanceof ServerLevel serverLevel) {
                ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), experienceFor(level));
            }
            level.levelEvent(LEVEL_EVENT_GRINDSTONE_USE, pos, 0);
        });
        inputs.setItem(INPUT_SLOT, ItemStack.EMPTY);
        inputs.setItem(ADDITIONAL_SLOT, ItemStack.EMPTY);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == RESULT_SLOT) {
            if (!moveItemStackTo(stack, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else if (index < INV_SLOT_START) {
            if (!moveItemStackTo(stack, INV_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (isInfused(stack)) {
            if (!moveItemStackTo(stack, INPUT_SLOT, RESULT_SLOT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < INV_SLOT_END) {
            if (!moveItemStackTo(stack, INV_SLOT_END, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, inputs));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, TTBlocks.ARCANE_GRINDSTONE.get());
    }

    private static final class InputContainer extends SimpleContainer {
        private final MenuArcaneGrindstone menu;

        private InputContainer(MenuArcaneGrindstone menu) {
            super(INPUT_COUNT);
            this.menu = menu;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            menu.slotsChanged(this);
        }
    }

    private static final class InputSlot extends Slot {
        private InputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isInfused(stack);
        }
    }

    private static final class ResultSlot extends Slot {
        private final MenuArcaneGrindstone menu;

        private ResultSlot(MenuArcaneGrindstone menu, Container container, int x, int y) {
            super(container, 0, x, y);
            this.menu = menu;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            menu.onResultTaken();
        }
    }
}
