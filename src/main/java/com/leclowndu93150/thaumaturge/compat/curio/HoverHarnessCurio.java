package com.leclowndu93150.thaumaturge.compat.curio;

import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.content.equipment.hover.HoverManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public final class HoverHarnessCurio implements ICurio {
    private final ItemStack stack;

    public HoverHarnessCurio(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        if (slotContext.entity() instanceof ServerPlayer player
                && stack.getItem() instanceof IHoverGear gear
                && !(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IHoverGear)) {
            HoverManager.tick(player, stack, gear);
        }
    }
}
