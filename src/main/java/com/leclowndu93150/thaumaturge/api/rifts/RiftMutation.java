package com.leclowndu93150.thaumaturge.api.rifts;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Short-lived server-thread reservation. Stage payment through {@link #transaction()}, then call
 * {@link #commit()}; closing without committing rolls back payment and releases the rift.
 * Do not retain across ticks or close the exposed transaction yourself.
 */
public interface RiftMutation extends AutoCloseable {
    RiftResult result();

    TransactionContext transaction();

    /** Rechecks the actor, delegation, entity, revision, and both protected volumes before committing. */
    RiftResult commit();

    @Override
    void close();
}
