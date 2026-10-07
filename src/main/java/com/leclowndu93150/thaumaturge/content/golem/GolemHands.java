package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.IGolemHands;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class GolemHands implements IGolemHands {
    private static final List<EquipmentSlot> ONE_HAND = List.of(EquipmentSlot.MAINHAND);
    private static final List<EquipmentSlot> TWO_HANDS = List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND);

    private final EntityThaumaturgeGolem golem;

    public GolemHands(EntityThaumaturgeGolem golem) {
        this.golem = golem;
    }

    private List<EquipmentSlot> hands() {
        return golem.properties().hasTrait(TTGolemTraits.HAULER.get()) ? TWO_HANDS : ONE_HAND;
    }

    @Override
    public ItemStack hold(ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        for (EquipmentSlot hand : hands()) {
            ItemStack held = golem.getItemBySlot(hand);
            if (held.isEmpty()) {
                golem.setItemSlot(hand, stack);
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents(held, stack) && held.getCount() < held.getMaxStackSize()) {
                int moved = Math.min(stack.getCount(), held.getMaxStackSize() - held.getCount());
                held.grow(moved);
                stack.shrink(moved);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    @Override
    public ItemStack release(ItemStack wanted) {
        ItemStack released = ItemStack.EMPTY;
        for (EquipmentSlot hand : hands()) {
            released = takeFrom(hand, wanted);
            if (!released.isEmpty()) {
                break;
            }
        }
        shiftToMainHand();
        return released;
    }

    private ItemStack takeFrom(EquipmentSlot hand, ItemStack wanted) {
        ItemStack held = golem.getItemBySlot(hand);
        if (held.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (wanted.isEmpty()) {
            golem.setItemSlot(hand, ItemStack.EMPTY);
            return held.copy();
        }
        if (!ItemStack.isSameItemSameComponents(held, wanted)) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = held.copyWithCount(Math.min(wanted.getCount(), held.getCount()));
        held.shrink(wanted.getCount());
        if (held.isEmpty()) {
            golem.setItemSlot(hand, ItemStack.EMPTY);
        }
        return taken;
    }

    private void shiftToMainHand() {
        if (hands().size() > 1 && golem.getMainHandItem().isEmpty() && !golem.getOffhandItem().isEmpty()) {
            golem.setItemSlot(EquipmentSlot.MAINHAND, golem.getOffhandItem().copy());
            golem.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public int room(ItemStack stack) {
        int room = 0;
        for (EquipmentSlot hand : hands()) {
            ItemStack held = golem.getItemBySlot(hand);
            if (held.isEmpty()) {
                room += stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(held, stack)) {
                room += held.getMaxStackSize() - held.getCount();
            }
        }
        return room;
    }

    @Override
    public boolean holds(ItemStack stack) {
        return !stack.isEmpty() && hands().stream().map(golem::getItemBySlot).anyMatch(held -> !held.isEmpty() && ItemStack.isSameItemSameComponents(held, stack));
    }

    @Override
    public List<ItemStack> contents() {
        return hands().stream().map(golem::getItemBySlot).toList();
    }
}
