package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.*;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityAlembic extends BlockEntity implements IEssentiaTransport, IAspectContainer {
    public static final int CAPACITY = 128;
    private static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;

    private @Nullable ResourceKey<IAspect> aspect;
    private @Nullable ResourceKey<IAspect> aspectFilter;
    private int amount;
    private int tickCount;
    private Direction facing = Direction.DOWN;

    public BlockEntityAlembic(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ALEMBIC.get(), pos, state);
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
        if (this.amount > 0)
            return;
        this.aspect = aspect;
        setChanged();
        syncToClient();
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
        this.aspect = null;
        this.amount = 0;
        setChanged();
        syncToClient();
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
                    if ((alembic.aspectFilter == null || Objects.equals(alembic.aspectFilter, aspectHolder.getKey())) && alembic.doAddToContainer(aspectHolder.getKey(), 1) == 0) {
                        return true;
                    }
                    deep++;
                }
            }

            BlockEntityAlembic alembic = (BlockEntityAlembic) be;
            if (alembic.amount > 0 && Objects.equals(alembic.aspect, aspectHolder.getKey()) && alembic.doAddToContainer(aspectHolder.getKey(), 1) == 0) {
                return true;
            }

            deep++;
        }
    }

    protected void syncToClient() {
        if (level == null || level.isClientSide())
            return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    protected int doAddToContainer(ResourceKey<IAspect> incoming, int requested) {
        if (requested == 0)
            return 0;
        if (aspectFilter != null && !aspectFilter.equals(incoming))
            return requested;
        if (amount < CAPACITY && incoming.equals(aspect) || amount == 0) {
            aspect = incoming;
            int added = Math.min(requested, CAPACITY - amount);
            amount += added;
            requested -= added;
            setChanged();
            syncToClient();
        }
        return requested;
    }

    protected boolean doTakeFromContainer(ResourceKey<IAspect> requested, int amt) {
        if (amount >= amt && requested.equals(aspect)) {
            amount -= amt;
            if (amount <= 0) {
                if (aspectFilter == null) {
                    aspect = null;
                }
                amount = 0;
            }
            setChanged();
            syncToClient();
            return true;
        }
        return false;
    }

    public AspectList getContents(HolderLookup.Provider registries) {
        if (aspect == null || amount <= 0)
            return AspectList.EMPTY;
        Holder<IAspect> holder = EssentiaTransportHelper.resolve(registries, aspect);
        if (holder == null)
            return AspectList.EMPTY;
        return AspectList.EMPTY.add(new AspectInstance(holder, amount));
    }

    public EssentiaList getEssentiaContents(HolderLookup.Provider registries) {
        AspectList contents = getContents(registries);
        return contents.isEmpty() ? EssentiaList.EMPTY : new EssentiaList(contents);
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
        if (!canOutputTo(face))
            return 0;
        ResourceKey<IAspect> key = aspect == null ? null : aspect.unwrapKey().orElse(null);
        if (key == null)
            return 0;
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspect = input.read("Aspect", ASPECT_KEY_CODEC).orElse(null);
        aspectFilter = input.read("AspectFilter", ASPECT_KEY_CODEC).orElse(null);
        amount = input.getIntOr("Amount", 0);
        facing = input.read("Facing", Direction.CODEC).orElse(Direction.DOWN);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspect != null)
            output.store("Aspect", ASPECT_KEY_CODEC, aspect);
        if (aspectFilter != null)
            output.store("AspectFilter", ASPECT_KEY_CODEC, aspectFilter);
        output.putInt("Amount", amount);
        output.store("Facing", Direction.CODEC, facing);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter$scopedcollector, registries);
            saveAdditional(tagvalueoutput);
            nbt.merge(tagvalueoutput.buildResult());
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
    protected void applyImplicitComponents(DataComponentGetter input) {
        super.applyImplicitComponents(input);
        EssentiaList contents = input.get(TCDataComponents.ESSENTIA_CONTENTS.get());
        if (contents != null && !contents.isEmpty()) {
            AspectInstance first = contents.contents().entries().get(0);
            ResourceKey<IAspect> key = first.aspect().unwrapKey().orElse(null);
            if (key != null) {
                aspect = key;
                amount = Math.min(first.amount(), CAPACITY);
            }
        }
        ResourceKey<IAspect> filter = input.get(TCDataComponents.ASPECT_FILTER.get());
        if (filter != null) {
            aspectFilter = filter;
            if (aspect == null)
                aspect = filter;
        }
    }

    @Override
    public AspectList getAspects() {
        if (amount() == 0)
            return AspectList.EMPTY;
        return AspectList.of(new AspectInstance(EssentiaTransportHelper.resolve(getLevel(), aspectKey()), amount()));
    }

    @Override
    public void setAspects(AspectList aspects) {
        if (aspects.isEmpty())
            return;
        AspectInstance first = aspects.entries().getFirst();
        ResourceKey<IAspect> key = first.aspect().unwrapKey().orElse(null);
        if (key != null) {
            aspect = key;
            amount = Math.min(first.amount(), CAPACITY);
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
        if (amount == 0)
            return amount;
        if ((this.amount < CAPACITY && Objects.equals(this.aspect, aspect.getKey())) || this.amount == 0) {
            this.aspect = aspect.getKey();
            int added = Math.min(amount, CAPACITY - this.amount);
            this.amount += added;
            amount -= added;
        }
        setChanged();
        syncToClient();
        return amount;
    }

    @Override
    public boolean takeFromContainer(Holder<IAspect> aspect, int amount) {
        if (this.amount >= amount && Objects.equals(this.aspect, aspect.getKey())) {
            this.amount -= amount;
            if (this.amount <= 0) {
                if (aspectFilter == null) {
                    this.aspect = null;
                }
                this.amount = 0;
            }
            setChanged();
            syncToClient();
            return true;
        }
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Holder<IAspect> aspect, int amount) {
        return this.amount >= amount && Objects.equals(this.aspect, aspect.getKey());
    }

    @Override
    public int containerContains(Holder<IAspect> aspect) {
        return Objects.equals(this.aspect, aspect.getKey()) ? this.amount : 0;
    }
}
