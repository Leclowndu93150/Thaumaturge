package com.leclowndu93150.thaumaturge.api.rifts;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Compile-only consumer using snapshots, reservations and authoritative transfer slots. */
final class RiftApiExample {
    private RiftApiExample() {}

    /** Payment must stage the complete cost in the reservation's transaction. */
    static RiftResult stabilize(RiftOperationContext context, UUID identity, float stability, Predicate<TransactionContext> payment) {
        var snapshot = RiftAccess.inspect(context.level(), identity);
        if (snapshot.isEmpty()) {
            return RiftResult.UNLOADED;
        }
        RiftSnapshot expected = snapshot.get();
        RiftChange change = new RiftChange(expected.position(), expected.size(), expected.seed(), stability, expected.collapsing());
        try (RiftMutation mutation = RiftAccess.prepareUpdate(context, expected, change)) {
            if (mutation.result() != RiftResult.SUCCESS) {
                return mutation.result();
            }
            if (!payment.test(mutation.transaction())) {
                return RiftResult.INSUFFICIENT_PAYMENT;
            }
            return mutation.commit();
        }
    }

    /** Prepares and consumes a capture quote in the same server tick. */
    static Optional<RiftResult> capture(RiftOperationContext context, UUID identity, int jarSlot, int wandSlot) {
        return RiftAccess.inspect(context.level(), identity).flatMap(snapshot -> RiftTransfers.prepareCapture(context, snapshot.identity(), snapshot.revision(), jarSlot, wandSlot))
                .map(quote -> RiftTransfers.tryCapture(context, quote));
    }

    static RiftResult release(RiftOperationContext context, int jarSlot, Vec3 destination) {
        return RiftTransfers.tryRelease(context, jarSlot, destination);
    }

    /** Retries interrupted delivery using the saved rift identity and the original owner context. */
    static RiftResult recover(RiftOperationContext context, UUID identity, int jarSlot) {
        return RiftTransfers.recover(context, identity, jarSlot);
    }
}
