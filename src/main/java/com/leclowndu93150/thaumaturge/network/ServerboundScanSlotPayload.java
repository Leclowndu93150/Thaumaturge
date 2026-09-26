package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundScanSlotPayload(int slot) implements CustomPacketPayload {
    public static final int SELF = -1;

    public static final Type<ServerboundScanSlotPayload> TYPE = new Type<>(TCIds.rl("scan_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundScanSlotPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerboundScanSlotPayload::slot,
            ServerboundScanSlotPayload::new);

    public static void handle(ServerboundScanSlotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            AbstractContainerMenu menu = player.containerMenu;
            if (!(menu.getCarried().getItem() instanceof ThaumometerItem)) {
                return;
            }
            if (payload.slot() == SELF) {
                ScanningManager.scanTheThing(player, player);
                return;
            }
            if (payload.slot() < 0 || payload.slot() >= menu.slots.size()) {
                return;
            }
            Slot slot = menu.getSlot(payload.slot());
            if (slot.hasItem() && slot.mayPickup(player) && !(slot instanceof ResultSlot)) {
                ScanningManager.scanTheThing(player, slot.getItem());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
