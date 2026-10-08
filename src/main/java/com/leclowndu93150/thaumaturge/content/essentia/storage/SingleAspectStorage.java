package com.leclowndu93150.thaumaturge.content.essentia.storage;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;

public final class SingleAspectStorage {
    private final SingleAspectEssentiaHost host;
    private final IEssentiaStorage[] views = new IEssentiaStorage[Direction.values().length];
    private long revision;

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
            ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
            if (amount <= 0 || key == null || !host.canInputFrom(side)) {
                return 0;
            }
            ResourceKey<IAspect> filter = host.aspectFilterKey();
            int current = host.amount();
            if (filter != null && !filter.equals(key) || current > 0 && !key.equals(host.aspectKey())) {
                return 0;
            }
            int accepted = Math.min(amount, host.storageInsertLimit(amount));
            if (accepted <= 0) {
                return 0;
            }
            int stored = Math.min(accepted, host.capacity() - current);
            if (!simulate) {
                ResourceKey<IAspect> previous = host.aspectKey();
                host.setStorageContents(key, current + stored);
                if (stored > 0 || !Objects.equals(previous, key)) {
                    revision++;
                    host.onStorageCommitted();
                }
                int voided = accepted - stored;
                if (voided > 0) {
                    host.onStorageVoided(voided);
                }
            }
            return accepted;
        }

        @Override
        public int extract(Holder<IAspect> aspect, int amount, boolean simulate) {
            ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
            int current = host.amount();
            if (amount <= 0
                    || key == null
                    || current <= 0
                    || !host.canOutputTo(side)
                    || !key.equals(host.aspectKey())) {
                return 0;
            }
            int taken = Math.min(amount, current);
            int left = current - taken;
            if (!simulate) {
                host.setStorageContents(left == 0 && host.aspectFilterKey() == null ? null : key, left);
                revision++;
                host.onStorageCommitted();
            }
            return taken;
        }

        @Override
        public long contentRevision() {
            return revision;
        }
    }
}
