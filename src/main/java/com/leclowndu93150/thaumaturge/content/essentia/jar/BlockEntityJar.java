package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.*;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.serialization.TCNbt;
import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BlockEntityJar extends BlockEntity implements IEssentiaTransport, IAspectSource {
    public static final int CAPACITY = IEssentiaJar.DEFAULT_CAPACITY;

    public int capacity() {
        return getBlockState().getBlock() instanceof IEssentiaJar jar ? jar.jarCapacity() : CAPACITY;
    }

    private static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;

    private @Nullable ResourceKey<IAspect> aspect;
    private @Nullable ResourceKey<IAspect> aspectFilter;
    private int amount;
    private int tickCount;
    private long contentRevision;
    private final IEssentiaStorage[] storageViews = new IEssentiaStorage[Direction.values().length];
    private Direction facing = Direction.DOWN;
    private boolean braced;

    public BlockEntityJar(BlockPos pos, BlockState state) {
        this(TCBlockEntities.JAR.get(), pos, state);
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

    protected void syncToClient() {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
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

    protected int storageInsertLimit(Holder<IAspect> aspect, int requested) {
        return Math.min(requested, capacity() - amount);
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
        aspect = TCNbt.read(input, "Aspect", ASPECT_KEY_CODEC, registries).orElse(null);
        aspectFilter =
                TCNbt.read(input, "AspectFilter", ASPECT_KEY_CODEC, registries).orElse(null);
        amount = input.getInt("Amount");
        facing = TCNbt.read(input, "Facing", Direction.CODEC, registries).orElse(Direction.DOWN);
        braced = input.getBoolean("Braced");
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (aspect != null) TCNbt.store(output, "Aspect", ASPECT_KEY_CODEC, registries, aspect);
        if (aspectFilter != null) TCNbt.store(output, "AspectFilter", ASPECT_KEY_CODEC, registries, aspectFilter);
        output.putInt("Amount", amount);
        TCNbt.store(output, "Facing", Direction.CODEC, registries, facing);
        output.putBoolean("Braced", braced);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        {
            CompoundTag tagvalueoutput = new CompoundTag();
            saveAdditional(tagvalueoutput, registries);
            nbt.merge(tagvalueoutput);
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (level != null && aspect != null && amount > 0) {
            EssentiaList contents = getEssentiaContents(level.registryAccess());
            if (!contents.isEmpty()) {
                builder.set(TCDataComponents.ESSENTIA_CONTENTS.get(), contents);
            }
        }
        if (aspectFilter != null) {
            builder.set(TCDataComponents.ASPECT_FILTER.get(), aspectFilter);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        EssentiaList contents = input.get(TCDataComponents.ESSENTIA_CONTENTS.get());
        if (contents != null && !contents.isEmpty()) {
            AspectInstance first = contents.contents().entries().get(0);
            ResourceKey<IAspect> key = first.aspect().unwrapKey().orElse(null);
            if (key != null) {
                aspect = key;
                amount = Math.min(first.amount(), capacity());
            }
        }
        ResourceKey<IAspect> filter = input.get(TCDataComponents.ASPECT_FILTER.get());
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
    public boolean doesContainerAccept(Holder<IAspect> aspect) {
        return aspectFilter == null || aspectFilter.equals(aspect.getKey());
    }

    @Override
    public int addToContainer(Holder<IAspect> aspect, int amount) {
        return doAddToContainer(aspect.getKey(), amount);
    }

    @Override
    public boolean takeFromContainer(Holder<IAspect> aspect, int amount) {
        return doTakeFromContainer(aspect.getKey(), amount);
    }

    @Override
    public boolean doesContainerContainAmount(Holder<IAspect> aspect, int amount) {
        return this.amount >= amount && Objects.equals(this.aspect, aspect.getKey());
    }

    @Override
    public int containerContains(Holder<IAspect> aspect) {
        return Objects.equals(this.aspect, aspect.getKey()) ? this.amount : 0;
    }

    @Override
    public boolean isBlocked() {
        return braced;
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
            if (amount <= 0 || !canInputFrom(side) || !doesContainerAccept(aspect)) return 0;
            if (BlockEntityJar.this.amount > 0 && !Objects.equals(BlockEntityJar.this.aspect, aspect.getKey()))
                return 0;
            int accepted = storageInsertLimit(aspect, amount);
            if (!simulate && accepted > 0) {
                accepted -= doAddToContainer(aspect.getKey(), accepted);
            }
            return accepted;
        }

        @Override
        public int extract(Holder<IAspect> aspect, int amount, boolean simulate) {
            if (amount <= 0 || !canOutputTo(side) || !Objects.equals(BlockEntityJar.this.aspect, aspect.getKey())) {
                return 0;
            }
            int extracted = Math.min(amount, BlockEntityJar.this.amount);
            if (!simulate && extracted > 0 && !doTakeFromContainer(aspect.getKey(), extracted)) return 0;
            return extracted;
        }

        @Override
        public long contentRevision() {
            return contentRevision;
        }
    }
}
