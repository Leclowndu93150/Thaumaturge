package com.leclowndu93150.thaumaturge.content.essentia.storage;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.api.transaction.SnapshotJournal;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;

public final class SingleAspectStorage {
    private final SingleAspectEssentiaHost host;
    private final IEssentiaStorage[] views = new IEssentiaStorage[Direction.values().length];
    private final StorageJournal journal = new StorageJournal();
    private long revision;
    private int deferredVoided;

    public SingleAspectStorage(SingleAspectEssentiaHost host) {
        this.host = host;
    }

    public IEssentiaStorage view(Direction side) {
        IEssentiaStorage view = views[side.ordinal()];
        if (view == null) {
            view = new View(side);
            views[side.ordinal()] = view;
        }
        return view;
    }

    public void markChanged() {
        revision++;
    }

    private final class View implements IEssentiaStorage {
        private final Direction side;

        private View(Direction side) {
            this.side = side;
        }

        @Override
        public AspectList contents() {
            return host.getAspects();
        }

        @Override
        public int insert(Holder<IAspect> aspect, int amount, boolean simulate) {
            if (simulate) {
                try (Transaction transaction = Transaction.openRoot()) {
                    return insert(aspect, amount, transaction);
                }
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int accepted = insert(aspect, amount, transaction);
                transaction.commit();
                return accepted;
            }
        }

        @Override
        public int insert(Holder<IAspect> aspect, int amount, TransactionContext transaction) {
            ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
            if (amount <= 0 || key == null || !host.receivesOn(side)) {
                return 0;
            }
            ResourceKey<IAspect> filter = host.aspectFilterKey();
            int current = host.amount();
            if ((filter != null && !filter.equals(key)) || (current > 0 && !key.equals(host.aspectKey()))) {
                return 0;
            }
            int accepted = Math.min(amount, host.storageInsertLimit(amount));
            if (accepted <= 0) {
                return 0;
            }
            int stored = Math.min(accepted, host.capacity() - current);
            int voided = accepted - stored;
            ResourceKey<IAspect> previous = host.aspectKey();
            if (stored > 0 || !Objects.equals(previous, key) || voided > 0) {
                journal.updateSnapshots(transaction);
                host.setStorageContents(key, current + stored);
                deferredVoided += voided;
            }
            return accepted;
        }

        @Override
        public int extract(Holder<IAspect> aspect, int amount, boolean simulate) {
            if (simulate) {
                try (Transaction transaction = Transaction.openRoot()) {
                    return extract(aspect, amount, transaction);
                }
            }
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = extract(aspect, amount, transaction);
                transaction.commit();
                return extracted;
            }
        }

        @Override
        public int extract(Holder<IAspect> aspect, int amount, TransactionContext transaction) {
            ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
            int current = host.amount();
            if (amount <= 0 || key == null || current <= 0 || !host.releasesOn(side) || !key.equals(host.aspectKey())) {
                return 0;
            }
            int taken = Math.min(amount, current);
            int left = current - taken;
            journal.updateSnapshots(transaction);
            host.setStorageContents(left == 0 && host.aspectFilterKey() == null ? null : key, left);
            return taken;
        }

        @Override
        public long contentRevision() {
            return revision;
        }
    }

    private final class StorageJournal extends SnapshotJournal<StorageSnapshot> {
        @Override
        protected StorageSnapshot createSnapshot() {
            return new StorageSnapshot(host.aspectKey(), host.amount(), revision, deferredVoided);
        }

        @Override
        protected void revertToSnapshot(StorageSnapshot snapshot) {
            host.setStorageContents(snapshot.aspect(), snapshot.amount());
            revision = snapshot.revision();
            deferredVoided = snapshot.deferredVoided();
        }

        @Override
        protected void onRootCommit(StorageSnapshot original) {
            boolean contentsChanged =
                    !Objects.equals(original.aspect(), host.aspectKey()) || original.amount() != host.amount();
            int voided = deferredVoided - original.deferredVoided();
            deferredVoided = original.deferredVoided();
            if (contentsChanged) {
                revision = original.revision() + 1;
                host.onStorageCommitted();
            }
            if (voided > 0) {
                host.onStorageVoided(voided);
            }
        }
    }

    private record StorageSnapshot(ResourceKey<IAspect> aspect, int amount, long revision, int deferredVoided) {}
}
