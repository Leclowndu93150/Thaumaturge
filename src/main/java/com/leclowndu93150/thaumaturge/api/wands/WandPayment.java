package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Atomic payment from stored primal pools. Costs are nonnegative centivis determined by the server
 * mechanic. Casting and crafting use the normal cap, equipment and sceptre discounts, truncating
 * each final pool to whole centivis. Raw costs bypass discounts. Creative players pay normally.
 * Quotes expire after their server tick: execution revalidates the player, slot, storage, capacity
 * and current cost. The same slot object must be supplied for preview and execution.
 */
public final class WandPayment {
    private WandPayment() {}

    public enum CostKind {
        RAW, CASTING, CRAFTING
    }

    public interface Quote {
        Map<ResourceKey<IAspect>, Integer> costs();
    }

    public static Optional<Quote> quote(ServerPlayer player, WandSlot slot, Map<ResourceKey<IAspect>, Integer> costs, CostKind kind) {
        return WandAccess.bindings().quotePayment(player, slot, costs, kind);
    }

    public static boolean tryPay(ServerPlayer player, WandSlot slot, Quote quote, TransactionContext transaction) {
        return WandAccess.bindings().tryPay(player, slot, quote, transaction);
    }
}
