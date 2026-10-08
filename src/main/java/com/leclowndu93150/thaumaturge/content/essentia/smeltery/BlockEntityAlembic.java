package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.api.aspect.*;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BlockEntityAlembic extends AbstractSyncedBlockEntity implements IEssentiaTransport, IAspectContainer {
    public static final int CAPACITY = 128;
    private static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;

    private @Nullable ResourceKey<IAspect> aspect;
    private @Nullable ResourceKey<IAspect> aspectFilter;
    private int amount;
    private int tickCount;
    private long contentRevision;
    private final IEssentiaStorage[] storageViews = new IEssentiaStorage[Direction.values().length];
    private Direction facing = Direction.DOWN;

    public BlockEntityAlembic(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ALEMBIC.get(), pos, state);
    }

    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspect;
    }

    public @Nullable ResourceKey<IAspect> aspectFilterKey() {
        return aspectFilter;
    }

    public int amount() {
        return amount;
    }

    public void setAspectFilter(@Nullable ResourceKey<IAspect> filter) {
        this.aspectFilter = filter;
        setChanged();
        syncToClient();
    }

    public void setAspectFromLabel(@Nullable ResourceKey<IAspect> aspect) {
        if (this.amount > 0) return;
        this.aspect = aspect;
        setChanged();
        syncToClient();
    }

    public void setFacing(Direction facing) {
        if (this.facing == facing) {
            return;
        }
        this.facing = facing;
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        syncToClient();
    }

    public Direction facing() {
        return facing;
    }

    protected void clearAspect() {
        boolean changed = amount > 0;
        this.aspect = null;
        this.amount = 0;
        if (changed) {
            contentsChanged();
        } else {
            setChanged();
            syncToClient();
        }
    }

    protected static boolean processAlembics(Level level, BlockPos pos, Holder<IAspect> aspectHolder) {
        int deep = 1;
        while (true) {
            BlockEntity be = level.getBlockEntity(pos.above(deep));
            if (be == null || !(be instanceof BlockEntityAlembic)) {
                deep = 1;
                while (true) {
                    be = level.getBlockEntity(pos.above(deep));
                    if (be == null || !(be instanceof BlockEntityAlembic)) {
                        return false;
                    }
                    BlockEntityAlembic alembic = (BlockEntityAlembic) be;
                    if ((alembic.aspectFilter == null || Objects.equals(alembic.aspectFilter, aspectHolder.getKey()))
                            && alembic.doAddToContainer(aspectHolder.getKey(), 1) == 0) {
                        return true;
                    }
                    deep++;
                }
            }

            BlockEntityAlembic alembic = (BlockEntityAlembic) be;
            if (alembic.amount > 0
                    && Objects.equals(alembic.aspect, aspectHolder.getKey())
                    && alembic.doAddToContainer(aspectHolder.getKey(), 1) == 0) {
                return true;
            }

            deep++;
        }
    }

    protected int doAddToContainer(ResourceKey<IAspect> incoming, int requested) {
        if (requested == 0) return 0;
        if (aspectFilter != null && !aspectFilter.equals(incoming)) return requested;
        if (amount < CAPACITY && incoming.equals(aspect) || amount == 0) {
            aspect = incoming;
            int added = Math.min(requested, CAPACITY - amount);
            amount += added;
            requested -= added;
            if (added > 0) contentsChanged();
        }
        return requested;
    }

    protected boolean doTakeFromContainer(ResourceKey<IAspect> requested, int amt) {
        if (amt > 0 && amount >= amt && requested.equals(aspect)) {
            amount -= amt;
            if (amount <= 0) {
                if (aspectFilter == null) {
                    aspect = null;
                }
                amount = 0;
            }
            contentsChanged();
            return true;
        }
        return false;
    }

    public AspectList getContents(HolderLookup.Provider registries) {
        if (aspect == null || amount <= 0) return AspectList.EMPTY;
        Holder<IAspect> holder = EssentiaTransportHelper.resolve(registries, aspect);
        if (holder == null) return AspectList.EMPTY;
        return AspectList.EMPTY.add(new AspectInstance(holder, amount));
    }

    public EssentiaList getEssentiaContents(HolderLookup.Provider registries) {
        AspectList contents = getContents(registries);
        return contents.isEmpty() ? EssentiaList.EMPTY : new EssentiaList(contents);
    }

    public IEssentiaStorage storage(Direction side) {
        int index = side.ordinal();
        IEssentiaStorage view = storageViews[index];
        if (view == null) {
            view = new StorageView(side);
            storageViews[index] = view;
        }
        return view;
    }

    private void contentsChanged() {
        contentRevision++;
        setChanged();
        syncToClient();
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != facing && face != Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face != facing && face != Direction.DOWN;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {}

    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face)) return 0;
        ResourceKey<IAspect> key = aspect == null ? null : aspect.unwrapKey().orElse(null);
        if (key == null) return 0;
        return doTakeFromContainer(key, amount) ? amount : 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public Holder<IAspect> getEssentiaType(Direction face) {
        return aspect == null ? null : EssentiaTransportHelper.resolve(level, aspect);
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return amount;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        aspect = TTNbt.read(input, "Aspect", ASPECT_KEY_CODEC, registries).orElse(null);
        aspectFilter =
                TTNbt.read(input, "AspectFilter", ASPECT_KEY_CODEC, registries).orElse(null);
        amount = input.getInt("Amount");
        facing = TTNbt.read(input, "Facing", Direction.CODEC, registries).orElse(Direction.DOWN);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (aspect != null) TTNbt.store(output, "Aspect", ASPECT_KEY_CODEC, registries, aspect);
        if (aspectFilter != null) TTNbt.store(output, "AspectFilter", ASPECT_KEY_CODEC, registries, aspectFilter);
        output.putInt("Amount", amount);
        TTNbt.store(output, "Facing", Direction.CODEC, registries, facing);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (level != null && aspect != null && amount > 0) {
            EssentiaList contents = getEssentiaContents(level.registryAccess());
            if (!contents.isEmpty()) {
                builder.set(TTDataComponents.ESSENTIA_CONTENTS.get(), contents);
            }
        }
        if (aspectFilter != null) {
            builder.set(TTDataComponents.ASPECT_FILTER.get(), aspectFilter);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        EssentiaList contents = input.get(TTDataComponents.ESSENTIA_CONTENTS.get());
        if (contents != null && !contents.isEmpty()) {
            AspectInstance first = contents.contents().entries().get(0);
            ResourceKey<IAspect> key = first.aspect().unwrapKey().orElse(null);
            if (key != null) {
                aspect = key;
                amount = Math.min(first.amount(), CAPACITY);
            }
        }
        ResourceKey<IAspect> filter = input.get(TTDataComponents.ASPECT_FILTER.get());
        if (filter != null) {
            aspectFilter = filter;
            if (aspect == null) aspect = filter;
        }
    }

    @Override
    public AspectList getAspects() {
        if (amount() == 0) return AspectList.EMPTY;
        return AspectList.of(new AspectInstance(EssentiaTransportHelper.resolve(getLevel(), aspectKey()), amount()));
    }

    @Override
    public void setAspects(AspectList aspects) {
        if (aspects.isEmpty()) return;
        AspectInstance first = aspects.entries().getFirst();
        ResourceKey<IAspect> key = first.aspect().unwrapKey().orElse(null);
        if (key != null) {
            int previousAmount = amount;
            ResourceKey<IAspect> previousAspect = aspect;
            aspect = key;
            amount = Math.min(first.amount(), CAPACITY);
            if (amount != previousAmount || !Objects.equals(aspect, previousAspect)) {
                contentsChanged();
                return;
            }
        }
        setChanged();
        syncToClient();
    }

    @Override
    public boolean accepts(Holder<IAspect> aspect) {
        return aspectFilter == null || aspectFilter.equals(aspect.getKey());
    }

    @Override
    public int fill(Holder<IAspect> aspect, int amount) {
        return doAddToContainer(aspect.getKey(), amount);
    }

    @Override
    public boolean drain(Holder<IAspect> aspect, int amount) {
        return doTakeFromContainer(aspect.getKey(), amount);
    }

    @Override
    public boolean holds(Holder<IAspect> aspect, int amount) {
        return this.amount >= amount && Objects.equals(this.aspect, aspect.getKey());
    }

    @Override
    public int amountOf(Holder<IAspect> aspect) {
        return Objects.equals(this.aspect, aspect.getKey()) ? this.amount : 0;
    }

    private final class StorageView implements IEssentiaStorage {
        private final Direction side;

        private StorageView(Direction side) {
            this.side = side;
        }

        @Override
        public AspectList contents() {
            return getAspects();
        }

        @Override
        public int insert(Holder<IAspect> aspect, int amount, boolean simulate) {
            return 0;
        }

        @Override
        public int extract(Holder<IAspect> aspect, int amount, boolean simulate) {
            if (amount <= 0 || !canOutputTo(side) || !Objects.equals(BlockEntityAlembic.this.aspect, aspect.getKey())) {
                return 0;
            }
            int extracted = Math.min(amount, BlockEntityAlembic.this.amount);
            if (!simulate && extracted > 0 && !doTakeFromContainer(aspect.getKey(), extracted)) return 0;
            return extracted;
        }

        @Override
        public long contentRevision() {
            return contentRevision;
        }
    }
}
