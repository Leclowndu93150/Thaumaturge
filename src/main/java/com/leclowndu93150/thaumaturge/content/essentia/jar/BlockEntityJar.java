package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.*;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.essentia.storage.SingleAspectEssentiaHost;
import com.leclowndu93150.thaumaturge.content.essentia.storage.SingleAspectStorage;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BlockEntityJar extends AbstractSyncedBlockEntity
        implements IEssentiaTransport, IAspectSource, SingleAspectEssentiaHost {
    public static final int CAPACITY = IEssentiaJar.DEFAULT_CAPACITY;

    public int capacity() {
        return getBlockState().getBlock() instanceof IEssentiaJar jar ? jar.jarCapacity() : CAPACITY;
    }

    private static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;

    private @Nullable ResourceKey<IAspect> aspect;
    private @Nullable ResourceKey<IAspect> aspectFilter;
    private int amount;
    private int tickCount;
    private final SingleAspectStorage storage = new SingleAspectStorage(this);
    private Direction facing = Direction.DOWN;
    private boolean braced;

    public BlockEntityJar(BlockPos pos, BlockState state) {
        this(TTBlockEntities.JAR.get(), pos, state);
    }

    protected BlockEntityJar(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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

    public void setBraced(boolean braced) {
        this.braced = braced;
        setChanged();
        syncToClient();
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

    public void clearAspectIfEmpty() {
        if (amount <= 0 && aspectFilter == null) {
            aspect = null;
            amount = 0;
            setChanged();
            syncToClient();
        }
    }

    public void emptyJar() {
        if (level != null && !level.isClientSide() && amount > 0 && aspectFilter == null) {
            AuraHelper.addFlux(level, getBlockPos(), amount);
        }
        if (aspectFilter == null) {
            aspect = null;
        }
        boolean changed = amount > 0;
        amount = 0;
        if (changed) {
            contentsChanged();
        } else {
            setChanged();
            syncToClient();
        }
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
        setChanged();
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityJar jar) {
        jar.tickCount++;
        if (jar.tickCount % 5 != 0) return;
        if (!jar.shouldFillFromAbove()) return;
        jar.fillJar();
    }

    protected boolean shouldFillFromAbove() {
        return amount < capacity();
    }

    private void fillJar() {
        if (level == null) return;
        BlockPos above = getBlockPos().above();
        IEssentiaTransport ic = EssentiaFlowHandler.transport(level, above, Direction.DOWN);
        if (ic == null) return;
        if (!ic.canOutputTo(Direction.DOWN)) return;
        Holder<IAspect> ta = null;
        if (aspectFilter != null) {
            ta = EssentiaTransportHelper.resolve(level, aspectFilter);
        } else if (aspect != null && amount > 0) {
            ta = EssentiaTransportHelper.resolve(level, aspect);
        } else if (ic.getEssentiaAmount(Direction.DOWN) > 0
                && ic.getSuctionAmount(Direction.DOWN) < getSuctionAmount(Direction.UP)
                && getSuctionAmount(Direction.UP) >= ic.getMinimumSuction()) {
            ta = ic.getEssentiaType(Direction.DOWN);
        }
        if (ta == null) return;
        if (ic.getSuctionAmount(Direction.DOWN) >= getSuctionAmount(Direction.UP)) return;
        int taken = ic.takeEssentia(ta, 1, Direction.DOWN);
        if (taken <= 0) return;
        ResourceKey<IAspect> key = ta.unwrapKey().orElse(null);
        if (key == null) return;
        doAddToContainer(key, taken);
    }

    protected int doAddToContainer(ResourceKey<IAspect> incoming, int requested) {
        if (requested == 0) return 0;
        if (aspectFilter != null && !aspectFilter.equals(incoming)) return requested;
        if (amount < capacity() && incoming.equals(aspect) || amount == 0) {
            aspect = incoming;
            int added = Math.min(requested, capacity() - amount);
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
        return storage.view(side);
    }

    private void contentsChanged() {
        storage.markChanged();
        setChanged();
        syncToClient();
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {}

    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        ResourceKey<IAspect> target = aspectFilter != null ? aspectFilter : aspect;
        return target == null ? null : EssentiaTransportHelper.resolve(level, target);
    }

    @Override
    public int getSuctionAmount(Direction face) {
        if (amount >= capacity()) return 0;
        return aspectFilter != null ? 64 : 32;
    }

    @Override
    public int getMinimumSuction() {
        return aspectFilter != null ? 64 : 32;
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
        if (!canInputFrom(face)) return 0;
        ResourceKey<IAspect> key = aspect == null ? null : aspect.unwrapKey().orElse(null);
        if (key == null) return 0;
        return amount - doAddToContainer(key, amount);
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
        braced = input.getBoolean("Braced");
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (aspect != null) TTNbt.store(output, "Aspect", ASPECT_KEY_CODEC, registries, aspect);
        if (aspectFilter != null) TTNbt.store(output, "AspectFilter", ASPECT_KEY_CODEC, registries, aspectFilter);
        output.putInt("Amount", amount);
        TTNbt.store(output, "Facing", Direction.CODEC, registries, facing);
        output.putBoolean("Braced", braced);
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
                amount = Math.min(first.amount(), capacity());
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
            amount = Math.min(first.amount(), capacity());
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

    @Override
    public boolean isBlocked() {
        return braced;
    }

    @Override
    public int storageInsertLimit(int requested) {
        return Math.min(requested, capacity() - amount);
    }

    @Override
    public void setStorageContents(@Nullable ResourceKey<IAspect> aspect, int amount) {
        this.aspect = aspect;
        this.amount = amount;
    }

    @Override
    public void onStorageCommitted() {
        setChanged();
        syncToClient();
    }
}
