package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundInventoryScanPayload(int containerId, int slotIndex, ItemStack target)
        implements CustomPacketPayload {
    public static final Type<ServerboundInventoryScanPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "inventory_scan"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundInventoryScanPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ServerboundInventoryScanPayload::containerId,
                    ByteBufCodecs.VAR_INT,
                    ServerboundInventoryScanPayload::slotIndex,
                    ItemStack.STREAM_CODEC,
                    ServerboundInventoryScanPayload::target,
                    ServerboundInventoryScanPayload::new);

    public static void handle(ServerboundInventoryScanPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (payload.target().isEmpty()) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (player.isCreative()) {
            if (!menu.getCarried().is(TCItems.THAUMOMETER.get()) || !inventoryContains(player, payload.target())) {
                return;
            }
            ScanningManager.scanTheThing(player, payload.target());
            return;
        }
        if (menu.containerId != payload.containerId()
                || payload.slotIndex() < 0
                || payload.slotIndex() >= menu.slots.size()
                || !menu.getCarried().is(TCItems.THAUMOMETER.get())) {
            return;
        }
        Slot slot = menu.getSlot(payload.slotIndex());
        if (slot instanceof ResultSlot
                || !slot.hasItem()
                || !slot.mayPickup(player)
                || !ItemStack.isSameItemSameComponents(slot.getItem(), payload.target())) {
            return;
        }
        ScanningManager.scanTheThing(player, slot.getItem());
    }

    private static boolean inventoryContains(Player player, ItemStack target) {
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            ItemStack stack = player.getInventory().getItem(index);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
