package com.leclowndu93150.thaumaturge.api.essentia;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Plays an essentia item's own feedback after a committed transfer, so an addon moving essentia
 * in or out of a jar or phial sounds the way the item does in Thaumaturge.
 *
 * @since 1.0.0
 */
public final class EssentiaTransferFeedback {
    private EssentiaTransferFeedback() {}

    /**
     * Plays the feedback for an item that was filled. Does nothing on the client or when nothing
     * moved.
     *
     * @param player         the player the transfer was for
     * @param resultingStack the stack after the transfer
     * @param transferred    how much essentia moved
     */
    public static void playFill(Player player, ItemStack resultingStack, int transferred) {
        play(player, resultingStack, transferred, IEssentiaItemStorage.TransferDirection.FILL);
    }

    /**
     * Plays the feedback for an item that was drained. Does nothing on the client or when nothing
     * moved.
     *
     * @param player         the player the transfer was for
     * @param resultingStack the stack after the transfer
     * @param transferred    how much essentia moved
     */
    public static void playDrain(Player player, ItemStack resultingStack, int transferred) {
        play(player, resultingStack, transferred, IEssentiaItemStorage.TransferDirection.DRAIN);
    }

    private static void play(
            Player player,
            ItemStack resultingStack,
            int transferred,
            IEssentiaItemStorage.TransferDirection direction) {
        if (transferred <= 0 || player.level().isClientSide()) {
            return;
        }
        IEssentiaItemStorage storage = resultingStack.getCapability(EssentiaCapabilities.ITEM_STORAGE);
        if (storage != null) {
            storage.playTransferFeedback(player, direction);
        }
    }
}
