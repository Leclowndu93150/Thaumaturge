package com.leclowndu93150.thaumaturge.api.essentia;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Portable, storage-selected feedback for committed essentia item transfers. */
public final class EssentiaTransferFeedback {
    private EssentiaTransferFeedback() {}

    public static void playFill(Player player, ItemStack resultingStack, int transferred, boolean simulated) {
        play(player, resultingStack, transferred, simulated, IEssentiaItemStorage.TransferDirection.FILL);
    }

    public static void playDrain(Player player, ItemStack resultingStack, int transferred, boolean simulated) {
        play(player, resultingStack, transferred, simulated, IEssentiaItemStorage.TransferDirection.DRAIN);
    }

    private static void play(
            Player player,
            ItemStack resultingStack,
            int transferred,
            boolean simulated,
            IEssentiaItemStorage.TransferDirection direction) {
        if (simulated || transferred <= 0 || player.level().isClientSide()) return;
        IEssentiaItemStorage storage = resultingStack.getCapability(EssentiaCapabilities.ITEM_STORAGE);
        if (storage != null) storage.playTransferFeedback(player, direction);
    }
}
