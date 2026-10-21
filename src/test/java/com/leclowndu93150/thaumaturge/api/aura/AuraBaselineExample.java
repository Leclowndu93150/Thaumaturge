package com.leclowndu93150.thaumaturge.api.aura;

import java.util.function.Predicate;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Compile-only consumer of baseline recalculation and transactional payment. */
final class AuraBaselineExample {
    private AuraBaselineExample() {}

    /**
     * Uses a caller-persisted sequence, retained across retries. Payment must stage its changes
     * in the supplied transaction and return false when the full cost cannot be paid.
     */
    static boolean recalculate(AuraBaseline.Context context, long sequence, Predicate<TransactionContext> payment) {
        var quote = AuraBaseline.quote(context);
        if (quote.isEmpty()) {
            return false;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            AuraBaseline.Result result = AuraBaseline.recalculate(context, quote.get(), sequence, transaction);
            if (result == AuraBaseline.Result.ALREADY_APPLIED) {
                return true;
            }
            if (result != AuraBaseline.Result.APPLIED || !payment.test(transaction)) {
                return false;
            }
            transaction.commit();
            return true;
        }
    }
}
